package com.viettel.vds.cdp.translator.controllers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.viettel.vds.cdp.translator.model.SelectedTimeModel;
import com.viettel.vds.cdp.translator.model.field.AggFieldModel;
import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;
import com.viettel.vds.cdp.translator.model.field.FieldModel;
import com.viettel.vds.cdp.translator.model.field.SelectedTimeFieldModel;

import java.io.IOException;

public class FieldModelDeserialize extends JsonDeserializer<BasicFieldModel> {

    private static final String TABLE_NAME_KEY = "tableName";
    private static final String IS_AGG_KEY = "isAgg";
    private static final String SELECTED_TIME_KEY = "selectedTime";
    private BasicFieldModel model;

    private void setAttrModel(JsonNode node) {
        model.setInverse(node.has("inverse") && node.get("inverse").asBoolean());
        model.setFieldName(
                node.has("fieldName") ? node.get("fieldName").asText() : ""
        );
    }

    private void setFieldModel(JsonNode node) {
        ((FieldModel) model).setTableName(
                node.has(TABLE_NAME_KEY) ? node.get(TABLE_NAME_KEY).asText() : ""
        );
        setAttrModel(node);
    }

    private void setDatetimeFieldModel(
            JsonNode node,
            DeserializationContext ctxt
    ) throws IOException {
        SelectedTimeModel selectedTime = ctxt.readTreeAsValue(
                node.get(SELECTED_TIME_KEY),
                SelectedTimeModel.class
        );
        ((SelectedTimeFieldModel) model).setSelectedTime(
                node.has(SELECTED_TIME_KEY) ? selectedTime : new SelectedTimeModel()
        );

        setFieldModel(node);
    }

    private void setAggFieldModel(JsonNode node) {
        ((AggFieldModel) model).setAgg(
                node.has(IS_AGG_KEY) && node.get(IS_AGG_KEY).asBoolean()
        );
        setAttrModel(node);
    }

    @Override
    public BasicFieldModel deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        boolean isAgg = node.has(IS_AGG_KEY) && node.get(IS_AGG_KEY).asBoolean();
        if (isAgg) {
            model = new AggFieldModel();
            setAggFieldModel(node);
            return model;
        }
        String tableName = node.has(TABLE_NAME_KEY)
                ? node.get(TABLE_NAME_KEY).asText()
                : "";
        if (node.has(SELECTED_TIME_KEY) && !tableName.isEmpty()) {
            model = new SelectedTimeFieldModel();
            setDatetimeFieldModel(node, ctxt);
            return model;
        }
        if (!tableName.isEmpty()) {
            model = new FieldModel();
            setFieldModel(node);
        }

        return model;
    }


}
