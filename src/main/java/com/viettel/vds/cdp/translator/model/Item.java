package com.viettel.vds.cdp.translator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {
    public String dataType;
    public String code;
    public String name;
    public String sql_operator;
    public String sql_condition_mapping;
}
