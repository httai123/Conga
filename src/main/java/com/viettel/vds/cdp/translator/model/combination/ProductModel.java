package com.viettel.vds.cdp.translator.model.combination;

import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;
import com.viettel.vds.cdp.translator.model.field.FieldModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductModel<T extends BasicFieldModel> {

    @SuppressWarnings("unchecked")
    private T[] fields = (T[]) new BasicFieldModel[0];

    private int w = 1;
    private String id = null;

    public Stream<String> getListTables() {
        return Arrays.stream(fields).map(field -> {
            if (field instanceof FieldModel) {
                FieldModel fieldModel = (FieldModel) field;
                return fieldModel.getTableName();
            } else {
                throw new TypeNotPresentException(field.getClass().getName(), null);
            }
        });
    }
}
