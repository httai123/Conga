package com.viettel.vds.cdp.translator.services.models;

import com.viettel.vds.cdp.translator.controllers.dto.Input;
import com.viettel.vds.model.mongo.cdp.RulesetOperators;
import lombok.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class MetadataRecord {
    @Setter
    private static Map<String, RulesetOperators> mapOperator;
    private Input input;
    private HashMap<String, String> mapAggAliasWithSubQuery;
    private HashMap<String, String> mapFieldIdWithNewName;
    private HashMap<String, String> mapFieldIdWithSubQuery;
    private HashMap<String, HashSet<String>> mapSubQueryWithFieldIds;
    private HashMap<String, HashSet<String>> mapNewNameWithFieldIds;
    private HashMap<String, Object> mapIdWithConfig;
}
