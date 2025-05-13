package com.viettel.vds.cdp.translator.enums;

import lombok.Getter;

@Getter
public enum StorageField {
    ID("msisdn"),
    PARTITION_TIME("PARTITION_DATE");

    private final String value;

    StorageField(String value) {
        this.value = value;
    }
}
