package com.viettel.vds.cdp.translator.services.models;

import com.viettel.vds.cdp.translator.model.SelectedTimeModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubQueryInfoRecord {
    private String tableName;
    private SelectedTimeModel selectedTime;
}
