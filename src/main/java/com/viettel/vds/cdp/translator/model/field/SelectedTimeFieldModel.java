package com.viettel.vds.cdp.translator.model.field;

import com.viettel.vds.cdp.translator.model.SelectedTimeModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SelectedTimeFieldModel extends FieldModel {
    private SelectedTimeModel selectedTime;
}
