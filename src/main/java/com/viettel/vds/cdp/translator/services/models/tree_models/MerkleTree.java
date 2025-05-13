package com.viettel.vds.cdp.translator.services.models.tree_models;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.util.mxCellRenderer;
import com.viettel.vds.cdp.translator.enums.LogicalOperator;
import com.viettel.vds.cdp.translator.model.combination.ClauseModel;
import com.viettel.vds.cdp.translator.model.combination.FilterModel;
import com.viettel.vds.cdp.translator.model.field.BasicFieldModel;
import com.viettel.vds.cdp.translator.services.helpers.InputHelperStream;
import com.viettel.vds.cdp.translator.utils.IdSetter;
import lombok.EqualsAndHashCode;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class BFSSkipMergedNodes
        extends BreadthFirstIterator<MerkleTreeNode, EmptyEdge> {

    private final Deque<MerkleTreeNode> queue;

    private BFSSkipMergedNodes(
            Graph<MerkleTreeNode, EmptyEdge> graph,
            MerkleTreeNode starNode
    ) {
        super(graph, starNode);
        queue = new ArrayDeque<>();
    }

    static BFSSkipMergedNodes fatoryBFSSkipMergedNodes(
            MerkleTree tree,
            MerkleTreeNode startNode
    ) {
        SimpleDirectedGraph<
                MerkleTreeNode,
                EmptyEdge
                > singleDirectedGraph = tree;
        return new BFSSkipMergedNodes(singleDirectedGraph, startNode);
    }

    @Override
    protected void encounterVertex(MerkleTreeNode vertex, EmptyEdge edge) {
        super.encounterVertex(vertex, edge);
        MerkleTreeNode parent = this.getParent(vertex);
        if (parent == null || !parent.isMerged()) {
            queue.add(vertex);
        }
    }

    @Override
    protected MerkleTreeNode provideNextVertex() {
        return this.queue.removeFirst();
    }

    @Override
    protected boolean isConnectedComponentExhausted() {
        return this.queue.isEmpty();
    }
}

@EqualsAndHashCode(callSuper = true)
public class MerkleTree extends SimpleDirectedGraph<MerkleTreeNode, EmptyEdge> {

    private final transient IdSetter idSetter;
    private transient MerkleTreeNode root;

    public MerkleTree() {
        super(EmptyEdge.class);
        idSetter = new IdSetter(true, null);
    }

    public MerkleTree(IdSetter idSetter) {
        super(EmptyEdge.class);
        this.idSetter = idSetter;
    }

    public void addVertexNewId(MerkleTreeNode node) {
        String id = idSetter.createId();
        node.setId(id);
        super.addVertex(node);
    }

    public MerkleTree createCompressTree() {
        MerkleTree newTree = new MerkleTree(idSetter);
        MerkleTreeNode treeNode = getRoot();
        BFSSkipMergedNodes bfs = BFSSkipMergedNodes.fatoryBFSSkipMergedNodes(this, treeNode);
        while (bfs.hasNext()) {
            MerkleTreeNode vertex = bfs.next();
            newTree.addVertex(vertex);
        }
        for (MerkleTreeNode vertex : newTree.vertexSet()) {
            for (EmptyEdge edge : incomingEdgesOf(vertex)) {
                MerkleTreeNode parent = getEdgeSource(edge);
                newTree.addEdge(parent, vertex);
            }
        }
        return newTree;
    }

    public MerkleTree getBranch(MerkleTreeNode node) {
        MerkleTree newTree = new MerkleTree(idSetter);
        processBranch(node, newTree);
        return newTree;
    }

    private void processBranch(MerkleTreeNode node, MerkleTree tree) {
        tree.addVertex(node);
        outgoingEdgesOf(node)
                .stream()
                .map(this::getEdgeTarget)
                .forEach(child -> {
                    tree.addVertex(child);
                    tree.addEdge(node, child);
                    processBranch(child, tree);
                });
    }

    public MerkleTreeNode getRoot() {
        if (root != null) {
            return root;
        }
        root = this.vertexSet()
                .stream()
                .filter(node -> this.inDegreeOf(node) == 0)
                .findFirst()
                .orElse(null);
        return root;
    }

    public MerkleTreeNode transFilterToTree(
            FilterModel<BasicFieldModel> filter,
            UnaryOperator<String> funcGetSubqueryNameForField
    ) {
        Stream<MerkleTreeNode> subChild = Arrays.stream(filter.getChildren())
                .map(f -> transFilterToTree(f, funcGetSubqueryNameForField));

        LogicalOperator logical = filter.getLogical();

        Stream<MerkleTreeNode> directedChild = Arrays.stream(filter.getClauses())
                .map(clause -> initLeafNode(clause, funcGetSubqueryNameForField));

        return createRootClauseNode(directedChild, subChild, logical);
    }

    public boolean pruneTreeByDfs(MerkleTreeNode root) {
        Optional<Boolean> isPruneChild =
                this.outgoingEdgesOf(root)
                        .stream()
                        .map(this::getEdgeTarget)
                        .filter(child -> !child.isLeaf() && !child.isMerged())
                        .map(this::pruneTreeByDfs)
                        .reduce((a, b) -> a || b);
        HashMap<HashSet<String>, List<MerkleTreeNode>> mapsetTableWithSetNode = new HashMap<>();
        HashSet<MerkleTreeNode> childNodes = new HashSet<>(
                this.outgoingEdgesOf(root).stream().map(this::getEdgeTarget).collect(Collectors.toList())
        );
        boolean isPruned = false;

        for (MerkleTreeNode node : childNodes) {
            isPruned |= processPruneNode(mapsetTableWithSetNode, node, root);
        }

        removeWrapper(root);

        return isPruneChild.orElse(false) || isPruned;
    }

    private void removeWrapper(MerkleTreeNode root) {
        int outDegree = this.outDegreeOf(root);
        if (outDegree != 1) {
            return;
        }
        MerkleTreeNode child = this.outgoingEdgesOf(root)
                .stream()
                .map(this::getEdgeTarget)
                .findFirst()
                .orElse(null);
        if (child == null) {
            return;
        }
        boolean isMerged = child.isMerged();
        if (!isMerged) {
            return;
        }
        // remove child and add all child's children to root
        this.outgoingEdgesOf(child)
                .stream()
                .map(this::getEdgeTarget)
                .forEach(wrappedNode -> this.addEdge(root, wrappedNode));
        this.removeVertex(child);
        root.setMerged(true);
    }

    public List<MerkleTreeNode> getLeafNodes() {
        List<MerkleTreeNode> leafNodes = new ArrayList<>();
        for (MerkleTreeNode node : this.vertexSet()) {
            int outDegree = this.outDegreeOf(node);
            if (outDegree == 0) {
                leafNodes.add(node);
            }
        }
        return leafNodes;
    }

    private MerkleTreeNode initLeafNode(
            ClauseModel<Object, BasicFieldModel> clause,
            UnaryOperator<String> funcGetSubqueryName
    ) {
        List<String> tableName = InputHelperStream.createFieldInClauseStream(clause)
                .map(BasicFieldModel::getId)
                .map(funcGetSubqueryName)
                .collect(Collectors.toList());
        HashSet<String> childTables = new HashSet<>(tableName);
        InputHelperStream.createFieldInClauseStream(clause)
                .map(field -> funcGetSubqueryName.apply(field.getId()))
                .forEach(childTables::add);
        MerkleTreeNode node = new MerkleTreeNode();
        node.setTables(childTables);
        List<ClauseModel<Object, BasicFieldModel>> clauses = new ArrayList<>();
        clauses.add(clause);
        node.setClauses(clauses);
        this.addVertexNewId(node);
        node.setLeaf(true);
        return node;
    }

    private MerkleTreeNode createRootClauseNode(
            Stream<MerkleTreeNode> directedChild,
            Stream<MerkleTreeNode> subChild,
            LogicalOperator logical
    ) {
        MerkleTreeNode rootNode = new MerkleTreeNode();
        HashSet<String> tables = new HashSet<>();
        rootNode.setTables(tables);
        rootNode.setLogical(logical);
        this.addVertexNewId(rootNode);
        Stream.concat(directedChild, subChild).forEach(child -> {
            tables.addAll(child.getTables());
            this.addEdge(rootNode, child);
        });
        return rootNode;
    }

    private boolean getWouldMerge(
            List<MerkleTreeNode> subsetNodes,
            MerkleTreeNode node1
    ) {
        for (MerkleTreeNode node2 : subsetNodes) {
            if (isContainAllTables(node1, node2)) {
                return true;
            }
        }
        return false;
    }

    private boolean isContainAllTables(
            MerkleTreeNode node1,
            MerkleTreeNode node2
    ) {
        HashSet<String> tables1 = node1.getTables();
        HashSet<String> tables2 = node2.getTables();
        boolean isNode1ContainNode2 =
                tables1.containsAll(tables2) && isMergeOrLeaf(node1);
        boolean isNode2ContainNode1 =
                tables2.containsAll(tables1) && isMergeOrLeaf(node2);
        return isNode1ContainNode2 || isNode2ContainNode1;
    }

    private boolean isMergeOrLeaf(MerkleTreeNode node) {
        return node.isMerged() || node.isLeaf();
    }

    private boolean processPruneNode(
            Map<HashSet<String>, List<MerkleTreeNode>> mapsetTableWithSetNode,
            MerkleTreeNode node,
            MerkleTreeNode root
    ) {
        HashSet<String> tables = node.getTables();
        boolean isPruned = false;
        List<Map.Entry<HashSet<String>, List<MerkleTreeNode>>> listEntryMap = new ArrayList<>(mapsetTableWithSetNode.entrySet());
        for (Map.Entry<HashSet<String>, List<MerkleTreeNode>> entry : listEntryMap) {
            List<MerkleTreeNode> subsetNodes = new ArrayList<>(entry.getValue());
            boolean wouldMerge = getWouldMerge(subsetNodes, node);
            if (!wouldMerge) {
                continue;
            }
            subsetNodes.add(node);
            HashSet<String> setupTable = new HashSet<>(tables);
            setupTable.addAll(entry.getKey());
            MerkleTreeNode mergedNode = getMergedNode(
                    subsetNodes,
                    setupTable,
                    root
            );
            mapsetTableWithSetNode.remove(entry.getKey());
            mapsetTableWithSetNode.put(setupTable, Collections.singletonList(mergedNode));
            isPruned = true;
        }

        if (!isPruned) {
            mapsetTableWithSetNode.computeIfAbsent(tables, k -> new ArrayList<>()).add(node);
        }

        return isPruned;
    }

    private MerkleTreeNode getMergedNode(
            List<MerkleTreeNode> subsetNodes,
            HashSet<String> tables,
            MerkleTreeNode root
    ) {
        MerkleTreeNode mergedNode = new MerkleTreeNode(
                null,
                root.getLogical(),
                new ArrayList<>(),
                tables,
                0,
                false,
                true
        );
        this.addVertexNewId(mergedNode);
        this.addEdge(root, mergedNode);
        subsetNodes.forEach(node -> {
            this.removeEdge(root, node);
            this.addEdge(mergedNode, node);
        });
        return mergedNode;
    }

    public void toImage(String path) {
        String finalPath = Optional.ofNullable(path).orElse("graph.png");
        JGraphXAdapter<MerkleTreeNode, EmptyEdge> graphAdapter = new JGraphXAdapter<>(this);

        mxCompactTreeLayout layout = new mxCompactTreeLayout(graphAdapter);
        layout.setHorizontal(false); // Hiển thị cây theo chiều dọc
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image = mxCellRenderer.createBufferedImage(
                graphAdapter,
                null,
                2,
                Color.WHITE,
                true,
                null
        );

        File imgFile = new File(finalPath);
        try {
            ImageIO.write(image, "PNG", imgFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
