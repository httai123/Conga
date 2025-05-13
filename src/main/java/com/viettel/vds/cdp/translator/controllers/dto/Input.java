package com.viettel.vds.cdp.translator.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.viettel.vds.cdp.translator.model.combination.AggModel;
import com.viettel.vds.cdp.translator.model.combination.FilterModel;
import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Input {

    private AggModel[] aggFields = new AggModel[0];
    private FilterModel<BasicFieldModel> filters = new FilterModel<>();
}
