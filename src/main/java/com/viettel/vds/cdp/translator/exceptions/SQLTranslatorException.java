package com.viettel.vds.cdp.translator.exceptions;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SQLTranslatorException extends RuntimeException {

    public SQLTranslatorException(String message) {
        super(message);
    }

    public SQLTranslatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public static SQLTranslatorException notFoundInMetadata(
            String metadata,
            String key
    ) {
        return new SQLTranslatorException(
                String.format("Key %s not found in metadata %s", key, metadata)
        );
    }

    public static SQLTranslatorException timeRangeInValid(int[] timeRange) {
        return new SQLTranslatorException("Time range is invalid: " + Arrays.stream(timeRange)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", "))
        );
    }

    public static SQLTranslatorException invalidOperator(String operator) {
        return new SQLTranslatorException("Invalid operator: " + operator);
    }
}
