package com.viettel.vds.cdp.translator.services.models.tree_models;

import com.viettel.vds.cdp.translator.enums.LogicalOperator;
import com.viettel.vds.cdp.translator.model.combination.ClauseModel;
import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerkleTreeNode {

    private String id;
    private LogicalOperator logical;
    private List<ClauseModel<Object, BasicFieldModel>> clauses = new LinkedList<>();
    private HashSet<String> tables;
    private int score = 0;
    private boolean isLeaf = false;
    private boolean isMerged = false;

    @Override
    public String toString() {
        return String.format(
                "%s %n Node: %s, %ntable: %s, %nisMerged: %s",
                id,
                logical,
                String.join("\n", tables),
                isMerged
        );
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
