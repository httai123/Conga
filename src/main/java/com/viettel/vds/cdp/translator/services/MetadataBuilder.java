package com.viettel.vds.cdp.translator.services;

import com.viettel.vds.cdp.translator.controllers.dto.Input;
import com.viettel.vds.cdp.translator.model.SelectedTimeModel;
import com.viettel.vds.cdp.translator.model.combination.AggModel;
import com.viettel.vds.cdp.translator.model.combination.FilterModel;
import com.viettel.vds.cdp.translator.model.field.AggFieldModel;
import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;
import com.viettel.vds.cdp.translator.model.field.SelectedTimeFieldModel;
import com.viettel.vds.cdp.translator.services.helpers.InputHelperStream;
import com.viettel.vds.cdp.translator.services.models.MetadataRecord;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MetadataBuilder {

    private final AggModel[] aggModels;
    private final FilterModel<BasicFieldModel> filter;
    protected HashMap<String, Object> mapIdWithConfig;
    private HashMap<String, String> mapAggAliasWithSubQuery;
    private HashMap<String, String> mapIdWithNewName;
    private HashMap<String, String> mapFieldIdWithSubQuery;
    private HashMap<String, HashSet<String>> mapNewNameWithFieldIds;
    private HashMap<String, HashSet<String>> mapSubQueryWithFieldIds;

    public MetadataBuilder(Input input) {
        aggModels = input.getAggFields();
        filter = input.getFilters();
    }

    public MetadataBuilder createMapAggAliasWithSubQuery() {
        mapAggAliasWithSubQuery = new HashMap<>();
        for (AggModel aggModel : aggModels) {
            mapAggAliasWithSubQuery.put(aggModel.getAlias(), aggModel.getId());
        }
        return this;
    }

    private String getFieldAlias(BasicFieldModel fieldModel) {
        if (fieldModel instanceof AggFieldModel) {
            return mapAggAliasWithSubQuery.get(fieldModel.getFieldName());
        }
        String tableName = getSubQueryName(fieldModel);
        if (fieldModel instanceof SelectedTimeFieldModel) {
            return fieldModel.getFieldName();
        }
        return tableName;
    }

    private String getDateName(SelectedTimeModel selectedTime) {
        int[] listTimeRanges = selectedTime.getTimeRange();
        return Arrays.stream(listTimeRanges)
                .mapToObj(val -> {
                    if (val < 0) {
                        return "neg_" + Math.abs(val);
                    }
                    return String.valueOf(val);
                }).collect(Collectors.joining("_"));
    }

    private String getTypeDateName(SelectedTimeModel selectedTime) {
        LinkedList<String> token = new LinkedList<>();
        token.push(selectedTime.getTimeRangeType().toString());
        token.push(selectedTime.getFrequency().toString());
        if (selectedTime.getTimeRange().length > 1) {
            token.push(selectedTime.getLogicalQuantifier().toString());
        }
        return String.join(
                "_",
                token
        );
    }

    private String getSubQueryName(BasicFieldModel fieldModel) {
        if (fieldModel instanceof AggFieldModel) {
            return mapAggAliasWithSubQuery.get(fieldModel.getFieldName());
        }
        if (fieldModel instanceof SelectedTimeFieldModel) {
            SelectedTimeFieldModel selectedTimeFieldModel = ((SelectedTimeFieldModel) fieldModel);
            String tableName = selectedTimeFieldModel.getTableName();
            SelectedTimeModel selectedTime = selectedTimeFieldModel.getSelectedTime();
            String dateName = getDateName(selectedTime);
            String dateTypeName = getTypeDateName(selectedTime);
            LinkedList<String> nameTokens = new LinkedList<>();
            nameTokens.add(tableName);
            nameTokens.add(dateTypeName);
            nameTokens.add(dateName);
            boolean isTimeRange = selectedTime.getTimeRange().length > 1;
            if (isTimeRange) nameTokens.add(fieldModel.getId());
            return String.join("_", nameTokens);
        }
        throw new IllegalArgumentException(
                "FieldModel is not instance of AggFieldModel or SelectedTimeFieldModel"
        );
    }

    private void set2WayMapping(
            BiConsumer<String, String> setMap11,
            BiConsumer<String, HashSet<String>> setMap1n,
            Function<String, HashSet<String>> getMap1n,
            Function<BasicFieldModel, String> namingFunction
    ) {
        InputHelperStream.createFieldStream(filter).forEach(fieldModel -> {
            String fieldId = fieldModel.getId();
            String name = namingFunction.apply(fieldModel);
            setMap11.accept(fieldId, name);
            HashSet<String> listIds = getMap1n.apply(name);
            if (listIds == null) {
                listIds = new HashSet<>();
                setMap1n.accept(name, listIds);
            }
            listIds.add(fieldId);
        });
    }

    public MetadataBuilder createMapFieldIdWithNewName() {
        mapIdWithNewName = new HashMap<>();
        mapNewNameWithFieldIds = new HashMap<>();
        set2WayMapping(
                mapIdWithNewName::put,
                mapNewNameWithFieldIds::put,
                mapNewNameWithFieldIds::get,
                this::getFieldAlias
        );
        return this;
    }

    public MetadataBuilder createMapFieldIdWithSubQuery() {
        mapFieldIdWithSubQuery = new HashMap<>();
        mapSubQueryWithFieldIds = new HashMap<>();
        set2WayMapping(
                mapFieldIdWithSubQuery::put,
                mapSubQueryWithFieldIds::put,
                mapSubQueryWithFieldIds::get,
                this::getSubQueryName
        );
        return this;
    }

    public MetadataBuilder createMapIdWithConfig() {
        mapIdWithConfig = new HashMap<>();
        InputHelperStream.createFieldStream(filter).forEach(fieldModel -> {
            String fieldId = fieldModel.getId();
            mapIdWithConfig.put(fieldId, fieldModel);
        });
        InputHelperStream.createClauseStream(filter).forEach(clauseModel -> {
            String clauseId = clauseModel.getId();
            mapIdWithConfig.put(clauseId, clauseModel);
        });
        return this;
    }

    public MetadataRecord build() {
        return new MetadataRecord(
                new Input(aggModels, filter),
                mapAggAliasWithSubQuery,
                mapIdWithNewName,
                mapFieldIdWithSubQuery,
                mapSubQueryWithFieldIds,
                mapNewNameWithFieldIds,
                mapIdWithConfig
        );
    }
}
