package com.viettel.vds.cdp.translator.utils;

import com.viettel.vds.cdp.translator.controllers.dto.Input;
import com.viettel.vds.cdp.translator.model.combination.AggModel;
import com.viettel.vds.cdp.translator.model.combination.ClauseModel;
import com.viettel.vds.cdp.translator.model.combination.FilterModel;
import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;
import com.viettel.vds.cdp.translator.services.helpers.InputHelperStream;

import java.util.Arrays;
import java.util.stream.Stream;

public class IdSetterForInput extends IdSetter {

    public IdSetterForInput() {
        super(true, null);
    }

    private void setIdForAggFields(Input input) {
        for (AggModel aggModel : input.getAggFields()) {
            String id = createId();
            aggModel.setId(id);
        }
        Arrays.stream(input.getAggFields()).forEach(aggModel ->
                aggModel.setId(createId())
        );
    }

    private void setIdForAttr(FilterModel<BasicFieldModel> filterMode) {
        Stream<BasicFieldModel> fieldStream = InputHelperStream.createFieldStream(
                filterMode
        );
        fieldStream.forEach(field -> field.setId(createId()));
    }

    private void setIdForClause(FilterModel<BasicFieldModel> filterModel) {
        Stream<ClauseModel<Object, BasicFieldModel>> clauseStream =
                InputHelperStream.createClauseStream(filterModel);
        clauseStream.forEach(clause -> clause.setId(createId()));
    }

    public void setId(Input input) {
        setIdForAggFields(input);
        setIdForClause(input.getFilters());
        setIdForAttr(input.getFilters());
    }
}
