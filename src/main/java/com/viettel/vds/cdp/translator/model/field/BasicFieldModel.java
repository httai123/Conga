package com.viettel.vds.cdp.translator.model.field;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.viettel.vds.cdp.translator.controllers.FieldModelDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonDeserialize(using = FieldModelDeserialize.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasicFieldModel {

    private boolean inverse;
    private String fieldName;
    private String id;
}
