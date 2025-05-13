package com.viettel.vds.cdp.translator.services;

import com.viettel.vds.cdp.translator.enums.FrequentlyAggregate;
import com.viettel.vds.cdp.translator.enums.StorageField;
import com.viettel.vds.cdp.translator.enums.TimeRangeType;
import com.viettel.vds.cdp.translator.exceptions.SQLTranslatorException;
import com.viettel.vds.cdp.translator.model.SelectedTimeModel;
import com.viettel.vds.cdp.translator.model.combination.AggModel;
import com.viettel.vds.cdp.translator.model.field.SelectedTimeFieldModel;
import com.viettel.vds.cdp.translator.services.helpers.QueryClauseTranslator;
import com.viettel.vds.cdp.translator.services.helpers.RelationalClauseTranslator;
import com.viettel.vds.cdp.translator.services.helpers.SqlTemplate;
import com.viettel.vds.cdp.translator.services.models.MetadataRecord;
import com.viettel.vds.cdp.translator.services.models.SubQueryContentRecord;
import com.viettel.vds.cdp.translator.services.models.SubQueryInfoRecord;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AParseQuery<T> {

    protected final RelationalClauseTranslator transFieldUsingMethodByMap;
    protected final RelationalClauseTranslator transFieldUsingMethodByFieldModel;
    protected final MetadataRecord data;
    protected final String userIdField = StorageField.ID.getValue();
    protected final List<SubQueryContentRecord> subQueryInfoRecord =
            new ArrayList<>();
    @Getter
    public T planQueryTree;

    AParseQuery(MetadataRecord data) {
        this.data = data;
        transFieldUsingMethodByMap =
                RelationalClauseTranslator.usingTransFieldByIdMapping(
                        data.getMapFieldIdWithSubQuery(),
                        data.getMapFieldIdWithNewName()
                );
        transFieldUsingMethodByFieldModel =
                RelationalClauseTranslator.usingSelectedTransFieldMethodByFieldModel();
    }

    protected List<String> getNameOfListSelectedTimeSubquery() {
        HashSet<String> mapWithoutAgg = new HashSet<>(data.getMapSubQueryWithFieldIds().keySet());
        data.getMapAggAliasWithSubQuery().values().forEach(mapWithoutAgg::remove);
        return new ArrayList<>(mapWithoutAgg);
    }

    protected SubQueryInfoRecord getSubqueryInfoByName(String name) {
        HashSet<String> listFieldIds = Optional.ofNullable(
                data.getMapSubQueryWithFieldIds().get(name)
        ).orElseThrow(() ->
                SQLTranslatorException.notFoundInMetadata("mapSubQueryWithFieldIds", name)
        );
        String fieldId = listFieldIds.iterator().next();
        SelectedTimeFieldModel firstField = (SelectedTimeFieldModel) (data
                .getMapIdWithConfig()
                .get(fieldId));
        return new SubQueryInfoRecord(
                firstField.getTableName(),
                firstField.getSelectedTime()
        );
    }

    private Collection<SelectedTimeFieldModel> getUniqueFieldMustSelectInSubquery(
            String subName
    ) {
        return data
                .getMapSubQueryWithFieldIds()
                .get(subName)
                .stream()
                .map(data.getMapIdWithConfig()::get)
                .collect(
                        Collectors.toMap(
                                field -> ((SelectedTimeFieldModel) field).getFieldName(),
                                SelectedTimeFieldModel.class::cast,
                                (existing, replacement) -> existing, // Handle duplicates if necessary
                                HashMap::new // Supplier for the new HashMap
                        )
                )
                .values();
    }

    protected List<SubQueryContentRecord> buildBasicSubQuery() {
        AggModel[] aggModels = data.getInput().getAggFields();
        Function<AggModel, SubQueryContentRecord> transAggSubquery = QueryClauseTranslator.transAggSubquery(
                transFieldUsingMethodByFieldModel
        );
        Stream<SubQueryContentRecord> aggSubs = Arrays.stream(aggModels).map(transAggSubquery);
        List<String> listNames = getNameOfListSelectedTimeSubquery();
        Stream<SubQueryContentRecord> selectedTimeSubs = listNames
                .stream()
                .map(this::transSelectedTimeSubquery);
        return Stream.concat(aggSubs, selectedTimeSubs).collect(Collectors.toList());
    }

    private String parseTimeLimit(
            boolean isAbsTime,
            int time,
            FrequentlyAggregate frequency,
            int interval,
            boolean isStartDay,
            boolean couldGreaterThanCur
    ) {
        String timeString = isAbsTime ? String.valueOf(time) : frequency.getSqlCurrentTime();
        int actualInterval = isAbsTime ? interval : interval - time;
        FrequentlyAggregate.ToSqlStringOptions options = new FrequentlyAggregate.ToSqlStringOptions(isStartDay, couldGreaterThanCur);
        return frequency.getSqlStringWrapper(timeString, actualInterval, options);
    }

    private String getWindowCondition(
            boolean isAbsTime,
            FrequentlyAggregate frequency,
            int[] timeRanges
    ) {
        int diff = isAbsTime ? frequency.calDif(timeRanges[0], timeRanges[1]) : frequency.calDifRela(timeRanges[0], timeRanges[1]);
        if (diff <= 0)
            throw SQLTranslatorException.timeRangeInValid(timeRanges);
        String itemsString = Stream.iterate(0, n -> n + 1)
                .limit(diff)
                .map(n -> parseTimeLimit(isAbsTime, timeRanges[1], frequency, n, false, n == 0))
                .collect(Collectors.joining(",\n\t"));
        return SqlTemplate.IN_OP
                .replace("$COL", StorageField.PARTITION_TIME.getValue())
                .replace("$VALUES", itemsString);
    }

    private String[] formatSelectedTime(SelectedTimeModel selectedTime) {
        int[] timeRanges = selectedTime.getTimeRange();
        String[] results = new String[3];
        int timeRangesLength = timeRanges.length;
        boolean haveLimitUpper = timeRangesLength > 1;
        FrequentlyAggregate frequency = selectedTime.getFrequency();
        boolean isAbsTime = TimeRangeType.ABS.equals(selectedTime.getTimeRangeType());
        for (int i = 0; i < timeRangesLength; i++) {
            results[i] = parseTimeLimit(
                    isAbsTime,
                    timeRanges[i],
                    frequency, 0,
                    (i % 2 == 0) && haveLimitUpper,
                    !FrequentlyAggregate.DAILY.equals(frequency)
            );
        }
        boolean isDaily = FrequentlyAggregate.DAILY.equals(selectedTime.getFrequency());
        results[2] = !haveLimitUpper || isDaily
                ? "TRUE"
                : getWindowCondition(isAbsTime, selectedTime.getFrequency(), timeRanges);
        return results;
    }

    protected SubQueryContentRecord transSelectedTimeSubquery(String subName) {
        SubQueryInfoRecord subInfo = getSubqueryInfoByName(subName);
        Collection<SelectedTimeFieldModel> uniqueField = getUniqueFieldMustSelectInSubquery(subName);
        String selectClause = QueryClauseTranslator.transSelectInSelectedTimeSubquery(
                uniqueField,
                data.getMapFieldIdWithNewName()
        );
        int[] timeRanges = subInfo.getSelectedTime().getTimeRange();
        boolean isTimeRanges = timeRanges.length > 1;
        String[] formatedTimes = formatSelectedTime(subInfo.getSelectedTime());

        String baseQuery = isTimeRanges
                ? SqlTemplate.TR_BASIC_QUERY
                : SqlTemplate.ST_BASIC_QUERY;
        String subquery = baseQuery
                .replace("$PROJECTION", selectClause)
                .replace("$TABLE", subInfo.getTableName())
                .replace("$PARTITION_NAME", StorageField.PARTITION_TIME.getValue())
                .replace("$LIMIT_ABOVE", String.valueOf(formatedTimes[0]));
        if (isTimeRanges) {
            subquery = subquery.replace(
                    "$LIMIT_UPPER",
                    String.valueOf(formatedTimes[1])).replace(
                    "$WINDOW_CONDITION",
                    formatedTimes[2]
            );
        }
        return new SubQueryContentRecord(subquery, subName);
    }

    public abstract void transformTreeForm();

    public abstract void buildSubquery();

    public abstract String buildFinalQuery();

    public abstract String buildQueryGetNumber();

    public String getNormalQuery() {
        transformTreeForm();
        buildSubquery();
        return buildFinalQuery();
    }

    public String getQueryCandidate() {
        transformTreeForm();
        buildSubquery();
        return buildQueryGetNumber();
    }
}
