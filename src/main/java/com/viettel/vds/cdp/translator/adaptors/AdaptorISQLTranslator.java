package com.viettel.vds.cdp.translator.adaptors;


import com.viettel.vds.cdp.translator.ISQLTranslator;
import com.viettel.vds.cdp.translator.controllers.Controller;
import com.viettel.vds.cdp.translator.controllers.dto.Input;
import com.viettel.vds.cdp.translator.enums.*;
import com.viettel.vds.cdp.translator.exceptions.SQLTranslatorException;
import com.viettel.vds.cdp.translator.model.SelectedTimeModel;
import com.viettel.vds.cdp.translator.model.combination.*;
import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;
import com.viettel.vds.cdp.translator.model.field.SelectedTimeFieldModel;
import com.viettel.vds.entities.cdp.AttributeRule;
import com.viettel.vds.entities.cdp.Ruleset;
import com.viettel.vds.entities.cdp.TimeRange;
import com.viettel.vds.model.mongo.cdp.RulesetOperators;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdaptorISQLTranslator implements ISQLTranslator {
    private final Controller controller;
    private final Map<String, RulesetOperators> mapOp;

    public AdaptorISQLTranslator(Controller controller, Map<String, RulesetOperators> mapOp) {
        this.controller = controller;
        this.mapOp = mapOp;
    }

    private SelectedTimeModel getSelectedTime(AttributeRule attr) {
        TimeRange attrTimeRange = attr.getTimeRange();
        SelectedTimeModel selectedTime = new SelectedTimeModel();
        selectedTime.setTimeRange(attrTimeRange.getValue());
        selectedTime.setFrequency(FrequentlyAggregate.valueOf(attrTimeRange.getFrequency().toUpperCase()));
        selectedTime.setTimeRangeType(TimeRangeType.valueOf(attrTimeRange.getFrequencyType().toUpperCase()));
        selectedTime.setLogicalQuantifier(LogicalQuantifier.valueOf(attr.getLogicalQuantifier().toUpperCase()));
        return selectedTime;
    }

    private SelectedTimeFieldModel convertAttrToFieldModel(AttributeRule attr) {
        SelectedTimeModel selectedTime = getSelectedTime(attr);
        SelectedTimeFieldModel selectedTimeField = new SelectedTimeFieldModel();
        selectedTimeField.setFieldName(attr.getAttribute());
        selectedTimeField.setTableName(attr.getTable());
        selectedTimeField.setSelectedTime(selectedTime);
        return selectedTimeField;
    }

    private ProductModel<SelectedTimeFieldModel> wrapAttrByProduct(SelectedTimeFieldModel fieldModel) {
        SelectedTimeFieldModel[] arrFieldModel = new SelectedTimeFieldModel[1];
        arrFieldModel[0] = fieldModel;
        ProductModel<SelectedTimeFieldModel> productionModel = new ProductModel<>();
        productionModel.setFields(arrFieldModel);
        return productionModel;
    }

    private SumModel<SelectedTimeFieldModel> wrapProductBySum(ProductModel<SelectedTimeFieldModel> productionModel) {
        ProductModel[] arrProductionModel = new ProductModel[1];
        arrProductionModel[0] = productionModel;
        SumModel<SelectedTimeFieldModel> sumModel = new SumModel<>();
        sumModel.setProductions(arrProductionModel);
        return sumModel;
    }

    private ClauseModel<String, SelectedTimeFieldModel> convertAttrToClauseModel(AttributeRule attr) {
        SelectedTimeFieldModel fieldModel = convertAttrToFieldModel(attr);
        ProductModel<SelectedTimeFieldModel> productionModel = wrapAttrByProduct(fieldModel);
        SumModel<SelectedTimeFieldModel> sumModel = wrapProductBySum(productionModel);
        ClauseModel<String, SelectedTimeFieldModel> clauseModel = new ClauseModel<>(
        );
        clauseModel.setSum(sumModel);
        List<Object> attrValues = Optional.ofNullable(attr.getValue()).orElse(Stream.of().collect(Collectors.toList()));
        String attrValue = "";
        if (!attrValues.isEmpty()) {
            attrValue = attrValues.get(0).toString();
        }
        clauseModel.setValue(attrValue);
        String operatorString;
        String opCode = attr.getOperator().getCode();
        RulesetOperators attrOp = mapOp.get(opCode);
        if (attrOp == null) {
            throw SQLTranslatorException.invalidOperator(opCode);
        }
        operatorString = attrOp.getSqlOperator();
        clauseModel.setOperator(operatorString);
        clauseModel.setDataType(DataType.valueOf(attr.getOperator().getDataType().toUpperCase()));
        return clauseModel;
    }

    FilterModel<BasicFieldModel> convertRuleset(Ruleset ruleset) {
        FilterModel<BasicFieldModel> filter = new FilterModel<>();
        String logical = ruleset.getLogical().toUpperCase();
        filter.setLogical(LogicalOperator.valueOf(logical));

        List<AttributeRule> attrRules = Optional.ofNullable(ruleset.getAttributeRules()).orElse(new LinkedList<>());
        ClauseModel[] clauses = attrRules.stream()
                .map(this::convertAttrToClauseModel)
                .toArray(ClauseModel[]::new);
        filter.setClauses(clauses);

        List<Ruleset> autoRules = Optional.ofNullable(ruleset.getAutoRules()).orElse(new LinkedList<>());
        FilterModel[] children = autoRules.stream()
                .map(this::convertRuleset)
                .toArray(FilterModel[]::new);
        filter.setChildren(children);

        return filter;
    }

    @Override
    public String translate(Ruleset ruleset) {
        FilterModel<BasicFieldModel> filter = convertRuleset(ruleset);
        Input input = new Input(new AggModel[0], filter);
        return controller.translator(input);
    }

    @Override
    public String basicRulesetToSQL(Ruleset ruleset) {
        FilterModel<BasicFieldModel> filter = convertRuleset(ruleset);
        Input input = new Input(new AggModel[0], filter);
        return controller.basicRulesetToSQL(input);
    }

    public String translateAndDraw(Ruleset ruleset) {
        FilterModel<BasicFieldModel> filter = convertRuleset(ruleset);
        Input input = new Input(new AggModel[0], filter);
        return controller.translateAndDraw(input);
    }
}