package com.viettel.vds.cdp.translator.services.helpers;

import com.viettel.vds.cdp.translator.model.combination.ClauseModel;
import com.viettel.vds.cdp.translator.model.combination.FilterModel;
import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;

import java.util.Arrays;
import java.util.stream.Stream;

public class InputHelperStream {

    private InputHelperStream() {
        throw new IllegalStateException("Utility class");
    }

    public static Stream<BasicFieldModel> createFieldStream(
            FilterModel<BasicFieldModel> filter
    ) {
        Stream<BasicFieldModel> curStream = Arrays.stream(filter.getClauses())
                .flatMap(clause -> Arrays.stream(clause.getSum().getProductions()))
                .flatMap(product -> Arrays.stream(product.getFields()));

        Stream<BasicFieldModel> childStream = Arrays.stream(filter.getChildren())
                .flatMap(InputHelperStream::createFieldStream);

        return Stream.concat(curStream, childStream);
    }

    public static Stream<ClauseModel<Object, BasicFieldModel>> createClauseStream(
            FilterModel<BasicFieldModel> filter
    ) {
        Stream<ClauseModel<Object, BasicFieldModel>> curStream = Arrays.stream(filter.getClauses());
        Stream<ClauseModel<Object, BasicFieldModel>> childStream = Arrays.stream(filter.getChildren())
                .flatMap(InputHelperStream::createClauseStream);

        return Stream.concat(curStream, childStream);
    }

    public static Stream<BasicFieldModel> createFieldInClauseStream(
            ClauseModel<?, BasicFieldModel> clause
    ) {
        return Arrays.stream(clause.getSum().getProductions()).flatMap(product ->
                Arrays.stream(product.getFields())
        );
    }
}
