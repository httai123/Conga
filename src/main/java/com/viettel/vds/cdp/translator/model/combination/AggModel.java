package com.viettel.vds.cdp.translator.model.combination;

import com.viettel.vds.cdp.translator.model.field.FieldModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggModel {

    private String aggType;
    private SumModel<FieldModel> sum;
    private FilterModel<FieldModel> filter;
    private int dateBlockAbove;
    private int dateBlockBelow;
    private String alias;
    private String id;
}
