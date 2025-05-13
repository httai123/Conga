package com.viettel.vds.cdp.translator.model.combination;

import com.viettel.vds.cdp.translator.enums.LogicalOperator;
import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterModel<F extends BasicFieldModel> {

    private LogicalOperator logical;

    @SuppressWarnings("unchecked")
    private ClauseModel<Object, F>[] clauses = new ClauseModel[0];

    @SuppressWarnings("unchecked")
    private FilterModel<F>[] children = new FilterModel[0];

    public Stream<String> getListTables() {
        ClauseModel<?, ?>[] processClauses = this.getClauses();
        FilterModel<?>[] processChildren = this.getChildren();
        return Stream.concat(
                Arrays.stream(processClauses).flatMap(clause ->
                        clause.getSum().getListTables()
                ),
                Arrays.stream(processChildren).flatMap(FilterModel::getListTables)
        );
    }
}
