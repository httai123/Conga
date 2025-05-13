package com.viettel.vds.cdp.translator.services.models.tree_models;

import com.viettel.vds.cdp.translator.enums.LogicalOperator;
import com.viettel.vds.cdp.translator.model.combination.ClauseModel;
import com.viettel.vds.cdp.translator.model.combination.FilterModel;
import com.viettel.vds.cdp.translator.services.helpers.RelationalClauseTranslator;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ToString(callSuper = true)
@Data
@NoArgsConstructor
public class SqlStringClauseTree {

    private LogicalOperator logical;
    private List<String> clauses;
    private List<SqlStringClauseTree> children;

    public static SqlStringClauseTree transFilterToTree(
            FilterModel<?> filter,
            RelationalClauseTranslator translatorClauseToString
    ) {
        ClauseModel[] clauseModels = filter.getClauses();
        SqlStringClauseTree root = new SqlStringClauseTree();
        root.setLogical(filter.getLogical());

        List<String> clauses = Arrays.stream(clauseModels)
                .map(translatorClauseToString::transClauseToString)
                .collect(Collectors.toList());
        root.setClauses(clauses);

        FilterModel<?>[] children = filter.getChildren();
        List<SqlStringClauseTree> childNodes = Arrays.stream(children)
                .map(f -> transFilterToTree(f, translatorClauseToString))
                .collect(Collectors.toList());
        root.setChildren(childNodes);

        return root;
    }

    private StringBuilder buildClause(
            List<StringBuilder> clauses,
            String delimiter
    ) {
        String result = String.join(delimiter, clauses);
        return new StringBuilder("(").append(result).append(")");
    }

    public StringBuilder buildWhereClauseRecursive() {
        Stream<StringBuilder> childResults = getChildren()
                .stream()
                .map(SqlStringClauseTree::buildWhereClauseRecursive)
                .map(ele -> String.format("%n%s%n", ele))
                .map(StringBuilder::new);
        Stream<StringBuilder> directResults = getClauses()
                .stream()
                .map(ele -> String.format("%s", ele))
                .map(StringBuilder::new);
        List<StringBuilder> allClauses = Stream.concat(directResults, childResults).collect(Collectors.toList());
        String delimiter = String.format(" %s%n", getLogical());
        return buildClause(allClauses, delimiter);
    }
}
