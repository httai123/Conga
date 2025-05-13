package com.viettel.vds.cdp.translator.services.helpers;

import com.viettel.vds.cdp.translator.exceptions.SQLTranslatorException;
import com.viettel.vds.cdp.translator.model.combination.ClauseModel;
import com.viettel.vds.cdp.translator.model.combination.ProductModel;
import com.viettel.vds.cdp.translator.model.combination.SumModel;
import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;
import com.viettel.vds.cdp.translator.model.field.FieldModel;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RelationalClauseTranslator {

    private final Function<BasicFieldModel, String> transFieldToString;

    private RelationalClauseTranslator(
            Function<BasicFieldModel, String> transField
    ) {
        transFieldToString = transField;
    }

    /**
     * Creates a RelationalClauseTranslator and field name is translated using the
     * provided maps.
     *
     * @param mapFieldIdWithSubQuery a map where the key is the field ID and the
     *                               value is the sub-query associated with the
     *                               field ID.
     * @param mapFieldIdWithNewName  a map where the key is the field ID and the
     *                               value is the new name associated with the field
     *                               ID.
     * @return a new instance of RelationalClauseTranslator.
     * @throws SQLTranslatorException if a field ID is not found in the provided
     *                                maps.
     */
    public static RelationalClauseTranslator usingTransFieldByIdMapping(
            Map<String, String> mapFieldIdWithSubQuery,
            Map<String, String> mapFieldIdWithNewName
    ) {
        Function<BasicFieldModel, String> transField = field -> {
            String fieldId = field.getId();
            String tableName = Optional.ofNullable(
                    mapFieldIdWithSubQuery.get(fieldId)
            ).orElseThrow(() ->
                    SQLTranslatorException.notFoundInMetadata(
                            "mapFieldIdWithSubQuery",
                            fieldId
                    )
            );
            String fieldName = Optional.ofNullable(
                    mapFieldIdWithNewName.get(fieldId)
            ).orElseThrow(() ->
                    SQLTranslatorException.notFoundInMetadata(
                            "mapFieldIdWithNewName",
                            fieldId
                    )
            );
            String baseString = tableName + "." + fieldName;
            return parseInverse(field.isInverse(), baseString);
        };
        return new RelationalClauseTranslator(transField);
    }

    private static String parseInverse(boolean isInverse, String fieldName) {
        if (isInverse) {
            return "1/" + fieldName;
        } else {
            return fieldName;
        }
    }

    /**
     * Creates a RelationalClauseTranslator and field name is translated using the
     * provided field model.
     *
     * @return a new instance of RelationalClauseTranslator.
     * @throws TypeNotPresentException if the field is not an instance of
     *                                 FieldModel.
     */
    public static RelationalClauseTranslator usingSelectedTransFieldMethodByFieldModel() {
        Function<BasicFieldModel, String> transField = field -> {
            if (field instanceof FieldModel) {
                FieldModel fieldModel = (FieldModel) field;
                String tableName = fieldModel.getTableName();
                String baseString = tableName + "." + fieldModel.getFieldName();
                return parseInverse(fieldModel.isInverse(), baseString);
            }
            throw new TypeNotPresentException(field.getClass().getName(), null);
        };
        return new RelationalClauseTranslator(transField);
    }

    public String transClauseToString(ClauseModel<?, ?> clause) {
        String result = transSumToString(clause.getSum());
        return SqlTemplate.RELATIONAL_CLAUSE
                .replace("$FIELD", result)
                .replace("$OP", clause.getOperator())
                .replace("$VALUE", clause.getDataType().getValue(clause.getValue()));
    }

    public String transSumToString(SumModel<?> sum) {
        ProductModel<?>[] productions = sum.getProductions();
        int bias = sum.getBias();
        String main = appendStream(productions, "+", this::transProductToString);
        return (bias != 0 ? String.format("%s + ", bias) : "") +
                main;
    }

    public String transProductToString(ProductModel<?> product) {
        int weight = product.getW();
        BasicFieldModel[] fields = product.getFields();
        String main = appendStream(fields, "*", transFieldToString);
        return (weight != 1 ? String.format("%s * ", weight) : "") +
                main;
    }

    private <T> String appendStream(
            T[] elements,
            String delimiter,
            Function<T, String> mapper
    ) {
        return Arrays.stream(elements).map(mapper).collect(Collectors.joining(delimiter));
    }
}
