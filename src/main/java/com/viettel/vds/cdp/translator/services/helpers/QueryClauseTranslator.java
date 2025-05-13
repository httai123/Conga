package com.viettel.vds.cdp.translator.services.helpers;

import com.viettel.vds.cdp.translator.enums.JoinType;
import com.viettel.vds.cdp.translator.enums.LogicalOperator;
import com.viettel.vds.cdp.translator.enums.StorageField;
import com.viettel.vds.cdp.translator.model.combination.AggModel;
import com.viettel.vds.cdp.translator.model.combination.FilterModel;
import com.viettel.vds.cdp.translator.model.field.SelectedTimeFieldModel;
import com.viettel.vds.cdp.translator.services.models.SubQueryContentRecord;
import com.viettel.vds.cdp.translator.services.models.tree_models.SqlStringClauseTree;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryClauseTranslator {

    protected static final List<String> defaultJoin = Arrays.asList(
            "msisdn",
            "PARTITION_DATE"
    );

    // Private constructor to prevent instantiation
    private QueryClauseTranslator() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated"
        );
    }

    private static String transNaturalJoin(
            String table1,
            String table2,
            List<String> joinCols,
            JoinType joinType
    ) {
        List<String> joinConditions = joinCols
                .stream()
                .map(col -> String.format("%s.%s = %s.%s", table1, col, table2, col))
                .collect(Collectors.toList());
        SqlStringClauseTree clauseTree = new SqlStringClauseTree();
        clauseTree.setClauses(joinConditions);
        clauseTree.setLogical(LogicalOperator.AND);
        clauseTree.setChildren(Collections.emptyList());
        StringBuilder joinClause = clauseTree.buildWhereClauseRecursive();
        String joinTypeValue = Optional.ofNullable(joinType)
                .orElse(JoinType.FULL_JOIN)
                .getValue();
        return String.format("%s %s ON %s %n", joinTypeValue, table2, joinClause);
    }

    public static String[] transNaturalJoin(
            List<String> tables,
            List<String> joinCols
    ) {
        return transNaturalJoin(tables, joinCols, JoinType.FULL_JOIN);
    }

    public static String[] transNaturalJoin(
            List<String> tables,
            List<String> joinCols,
            JoinType joinType
    ) {
        if (tables.isEmpty()) {
            throw new IllegalArgumentException("Tables must have at least 1 element");
        }
        List<String> processJoinCols = Optional.ofNullable(joinCols).orElse(defaultJoin);
        Iterator<String> iter = tables.iterator();
        String preItem = iter.next();
        StringBuilder sb = new StringBuilder().append(String.format("%s %n", preItem));
        while (iter.hasNext()) {
            String nextItem = iter.next();
            String clause = transNaturalJoin(
                    preItem,
                    nextItem,
                    processJoinCols,
                    joinType
            );
            sb.append(clause);
            preItem = nextItem;
        }
        String fromClause = sb.toString();
        // remove the last join clause is \n
        fromClause = fromClause.substring(0, fromClause.length() - 1);
        String selectClause = joinType.equals(JoinType.FULL_JOIN)
                ? transSelectFullJoinItems(tables.stream(), processJoinCols.stream())
                : transSelectInnerJoinItems(tables.get(0), processJoinCols.stream());
        return new String[]{selectClause, fromClause};
    }

    private static String transSelectFullJoinItems(
            Stream<String> tables,
            Stream<String> joinCols
    ) {
        List<String> listSelectCol = joinCols
                .map(col -> transSelectFullJoinItem(tables, col))
                .collect(Collectors.toList());
        return String.join(", ", listSelectCol);
    }

    private static String transSelectInnerJoinItems(
            String table,
            Stream<String> joinCols
    ) {
        List<String> listCol = joinCols
                .map(col -> String.format("%s.%s AS %s", table, col, col))
                .collect(Collectors.toList());
        return String.join(", ", listCol);
    }

    private static String transSelectFullJoinItem(
            Stream<String> tables,
            String joinCol
    ) {
        List<String> listCols = tables
                .map(table -> String.format("%s.%s", table, joinCol))
                .collect(Collectors.toList());
        if (listCols.size() == 1) {
            return String.format("%s AS %s", listCols.get(0), joinCol);
        }
        String listColsString = String.join(", ", listCols);
        return String.format("COALESCE(%s) AS %s", listColsString, joinCol);
    }

    public static String transWhereClause(
            FilterModel<?> filter,
            RelationalClauseTranslator translatorClauseToString
    ) {
        SqlStringClauseTree tree = SqlStringClauseTree.transFilterToTree(
                filter,
                translatorClauseToString
        );
        return String.format("%s%n", tree.buildWhereClauseRecursive());
    }

    private static String transSubQueryInfoRecordToString(
            SubQueryContentRecord subQueryInfoRecord
    ) {
        return SqlTemplate.SUBQUERY_IN_CTA.replace(
                "$ALIAS",
                subQueryInfoRecord.getAlias()
        ).replace("$QUERY", subQueryInfoRecord.getBody());
    }

    public static String transCtaClause(
            List<SubQueryContentRecord> subQueryInfos
    ) {
        return subQueryInfos
                .stream()
                .map(QueryClauseTranslator::transSubQueryInfoRecordToString).collect(Collectors.joining(", "));
    }

    public static Function<AggModel, SubQueryContentRecord> transAggSubquery(
            RelationalClauseTranslator translatorClauseToString
    ) {
        return aggModel -> {
            List<String> listTable = Stream.concat(
                            aggModel.getSum().getListTables(),
                            aggModel.getFilter().getListTables()
                    )
                    .distinct()
                    .collect(Collectors.toList());
            String[] joinResults = QueryClauseTranslator.transNaturalJoin(listTable, null);
            String whereClause = QueryClauseTranslator.transWhereClause(
                    aggModel.getFilter(),
                    translatorClauseToString
            );
            String sumClause = translatorClauseToString.transSumToString(
                    aggModel.getSum()
            );
            String body = SqlTemplate.AGG_QUERY.replace("$PROJECTION", joinResults[0])
                    .replace("$AGG_FUNC", aggModel.getAggType())
                    .replace("$AGG_COL", sumClause)
                    .replace("$AGG_ALIAS", aggModel.getId())
                    .replace("$SRC", joinResults[1])
                    .replace("$FILTER", whereClause);
            return new SubQueryContentRecord(body, aggModel.getId());
        };
    }

    public static String transSelectInSelectedTimeSubquery(
            Collection<SelectedTimeFieldModel> listUniqueField,
            Map<String, String> mapFieldIdWithNewName
    ) {
        List<String> listFields = new LinkedList<>();
        listFields.add(StorageField.ID.getValue());
        listUniqueField
                .stream()
                .map(fieldModel -> {
                    String originName = fieldModel.getFieldName();
                    String alias = mapFieldIdWithNewName.get(fieldModel.getId());
                    return String.format("%s AS %s", originName, alias);
                })
                .forEach(listFields::add);
        return String.join(", ", listFields);
    }
}
