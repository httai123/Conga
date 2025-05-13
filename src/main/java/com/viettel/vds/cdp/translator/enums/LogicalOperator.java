package com.viettel.vds.cdp.translator.enums;

import lombok.Getter;

@Getter
public enum LogicalOperator {
    AND("AND"),
    OR("OR");

    private final String value;

    LogicalOperator(String value) {
        this.value = value;
    }
}
