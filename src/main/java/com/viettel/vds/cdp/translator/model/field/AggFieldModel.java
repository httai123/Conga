package com.viettel.vds.cdp.translator.model.field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class AggFieldModel extends BasicFieldModel {

    private boolean isAgg = true;
}
