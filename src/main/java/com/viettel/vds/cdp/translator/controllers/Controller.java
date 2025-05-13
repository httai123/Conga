package com.viettel.vds.cdp.translator.controllers;

import com.viettel.vds.cdp.translator.controllers.dto.Input;
import com.viettel.vds.cdp.translator.services.AParseQuery;
import com.viettel.vds.cdp.translator.services.FactoryParseQuery;
import com.viettel.vds.cdp.translator.services.models.tree_models.MerkleTree;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Controller {

    public String translator(Input input) {
        AParseQuery<MerkleTree> parseQuery = new FactoryParseQuery(input).getParseMerkleTreeQueryNormal();
        return parseQuery.getNormalQuery();
    }


    public String basicRulesetToSQL(Input input) {
        AParseQuery<MerkleTree> parseQuery = new FactoryParseQuery(input).getParseMerkleTreeQueryNormal();
        return parseQuery.getQueryCandidate();
    }

    public String translateAndDraw(Input input) {
        Path rsPath = Paths.get(
                System.getProperty("user.dir"),
                "src",
                "main",
                "resources"
        );
        AParseQuery<MerkleTree> parseQuery = new FactoryParseQuery(input).getParseMerkleTreeQueryNormal();
        MerkleTree tree = parseQuery.getPlanQueryTree();
        tree.toImage(rsPath + "/test_raw.png");
        String sql = parseQuery.getNormalQuery();
        tree.toImage(rsPath + "/test.png");
        return sql;
    }
}
