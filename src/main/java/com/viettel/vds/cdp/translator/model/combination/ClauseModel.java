package com.viettel.vds.cdp.translator.model.combination;

import com.viettel.vds.cdp.translator.enums.DataType;
import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClauseModel<V, F extends BasicFieldModel> {

    private SumModel<F> sum;
    private String operator;
    private DataType dataType;
    private V value;
    private String id;
}
