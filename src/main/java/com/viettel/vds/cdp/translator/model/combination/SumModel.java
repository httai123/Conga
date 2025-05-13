package com.viettel.vds.cdp.translator.model.combination;

import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SumModel<T extends BasicFieldModel> {

    @SuppressWarnings("unchecked")
    private ProductModel<T>[] productions = new ProductModel[0];

    private int bias = 0;
    private String id = null;

    public Stream<String> getListTables() {
        ProductModel[] listProductions = this.getProductions();
        return Arrays.stream(listProductions).flatMap(ProductModel::getListTables);
    }
}
