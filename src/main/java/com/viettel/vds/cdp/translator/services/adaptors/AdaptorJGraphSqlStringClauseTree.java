package com.viettel.vds.cdp.translator.services.adaptors;

import com.viettel.vds.cdp.translator.services.helpers.RelationalClauseTranslator;
import com.viettel.vds.cdp.translator.services.models.tree_models.MerkleTree;
import com.viettel.vds.cdp.translator.services.models.tree_models.MerkleTreeNode;
import com.viettel.vds.cdp.translator.services.models.tree_models.SqlStringClauseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdaptorJGraphSqlStringClauseTree {

    private AdaptorJGraphSqlStringClauseTree() {
        throw new IllegalStateException("Utility class");
    }

    public static SqlStringClauseTree convert(
            MerkleTree merkleTree,
            MerkleTreeNode start,
            RelationalClauseTranslator translatorClauseToString
    ) {
        ArrayList<MerkleTreeNode> directChild = new ArrayList<>();
        ArrayList<MerkleTreeNode> subTree = new ArrayList<>();
        merkleTree
                .outgoingEdgesOf(start)
                .stream()
                .map(merkleTree::getEdgeTarget)
                .forEach(node -> {
                    ArrayList<MerkleTreeNode> typeChild = node.isLeaf() ? directChild : subTree;
                    typeChild.add(node);
                });
        SqlStringClauseTree root = new SqlStringClauseTree();
        List<String> listClauses = Stream.concat(
                        start.getClauses().stream(),
                        directChild.stream().flatMap(node -> node.getClauses().stream())
                )
                .map(translatorClauseToString::transClauseToString)
                .collect(Collectors.toList());
        root.setClauses(listClauses);
        List<SqlStringClauseTree> listChildren = subTree
                .stream()
                .map(node -> convert(merkleTree, node, translatorClauseToString))
                .collect(Collectors.toList());
        root.setChildren(listChildren);
        root.setLogical(start.getLogical());
        return root;
    }
}
