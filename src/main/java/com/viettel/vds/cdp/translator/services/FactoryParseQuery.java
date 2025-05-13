package com.viettel.vds.cdp.translator.services;

import com.viettel.vds.cdp.translator.controllers.dto.Input;
import com.viettel.vds.cdp.translator.services.models.MetadataRecord;
import com.viettel.vds.cdp.translator.utils.IdSetterForInput;

public class FactoryParseQuery {
    private final MetadataRecord metadata;

    public FactoryParseQuery(Input input) {
        new IdSetterForInput().setId(input);
        metadata = new MetadataBuilder(input)
                .createMapAggAliasWithSubQuery()
                .createMapFieldIdWithNewName()
                .createMapFieldIdWithSubQuery()
                .createMapIdWithConfig()
                .build();
    }

    public ParseMerkleTreeQueryNormal getParseMerkleTreeQueryNormal() {
        return new ParseMerkleTreeQueryNormal(metadata);
    }
}
