package com.viettel.vds.cdp.translator.enums;

import lombok.Getter;

@Getter
public enum JoinType {
    INNER_JOIN("INNER JOIN"),
    LEFT_JOIN("LEFT JOIN"),
    RIGHT_JOIN("RIGHT JOIN"),
    FULL_JOIN("FULL JOIN");

    private final String value;

    JoinType(String joinType) {
        this.value = joinType;
    }
}
