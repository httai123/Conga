package com.viettel.vds.cdp.translator.services;

import com.viettel.vds.cdp.translator.enums.FrequentlyAggregate;
import com.viettel.vds.cdp.translator.enums.LogicalOperator;
import com.viettel.vds.cdp.translator.enums.StorageField;
import com.viettel.vds.cdp.translator.enums.TimeRangeType;
import com.viettel.vds.cdp.translator.model.SelectedTimeModel;
import com.viettel.vds.cdp.translator.services.adaptors.AdaptorJGraphSqlStringClauseTree;
import com.viettel.vds.cdp.translator.services.helpers.QueryClauseTranslator;
import com.viettel.vds.cdp.translator.services.helpers.SqlTemplate;
import com.viettel.vds.cdp.translator.services.models.MetadataRecord;
import com.viettel.vds.cdp.translator.services.models.SubQueryContentRecord;
import com.viettel.vds.cdp.translator.services.models.tree_models.EmptyEdge;
import com.viettel.vds.cdp.translator.services.models.tree_models.MerkleTree;
import com.viettel.vds.cdp.translator.services.models.tree_models.MerkleTreeNode;
import com.viettel.vds.cdp.translator.services.models.tree_models.SqlStringClauseTree;
import com.viettel.vds.cdp.translator.utils.TraversalListenerHaveDefault;
import lombok.Getter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ParseMerkleTreeQueryNormal extends AParseQuery<MerkleTree> {

    @Getter
    protected final LinkedList<MerkleTree> componentTrees = new LinkedList<>();

    protected final LinkedList<SubQueryContentRecord> componentSubqueries =
            new LinkedList<>();
    protected MerkleTree compressTree;

    public ParseMerkleTreeQueryNormal(MetadataRecord data) {
        super(data);
        UnaryOperator<String> funcGetSubqueryName = id ->
                Optional.ofNullable(data.getMapFieldIdWithSubQuery().get(id)).orElse(id);
        planQueryTree = new MerkleTree();
        planQueryTree.transFilterToTree(
                data.getInput().getFilters(),
                funcGetSubqueryName
        );
    }

    @Override
    public void transformTreeForm() {
        planQueryTree.pruneTreeByDfs(planQueryTree.getRoot());
        compressTree = planQueryTree.createCompressTree();
        compressTree
                .getLeafNodes()
                .forEach(leaf -> {
                    MerkleTree branch = planQueryTree.getBranch(leaf);
                    componentTrees.add(branch);
                });
    }

    @Override
    public void buildSubquery() {
        List<SubQueryContentRecord> basicSubQuery = buildBasicSubQuery();
        subQueryInfoRecord.addAll(basicSubQuery);
        List<SubQueryContentRecord> subqueries = componentTrees
                .stream()
                .map(this::buildComponentSubquery)
                .collect(Collectors.toList());
        subQueryInfoRecord.addAll(subqueries);
        componentSubqueries.addAll(subqueries);
    }

    private String getGroupByFromProjection(String projection) {
        String[] projections = projection.split(",");
        // remove word after AS
        // concat with comma
        return Arrays.stream(projections)
                .map(pro -> pro.split("AS")[0].trim())
                .collect(Collectors.joining());
    }

    private SubQueryContentRecord buildComponentSubquery(MerkleTree tree) {
        SqlStringClauseTree sqlStringTree = AdaptorJGraphSqlStringClauseTree.convert(
                tree,
                tree.getRoot(),
                transFieldUsingMethodByMap
        );
        StringBuilder condition = sqlStringTree.buildWhereClauseRecursive();
        List<String> listSubQueryName = new ArrayList<>(tree.getRoot().getTables());
        List<String> joinCols = new ArrayList<>();
        joinCols.add(StorageField.ID.getValue());
        String[] joinResults = QueryClauseTranslator.transNaturalJoin(
                listSubQueryName,
                joinCols
        );
        SelectedTimeModel selectedTime = getSubqueryInfoByName(
                listSubQueryName.get(0)
        ).getSelectedTime();
        int[] timeRange = selectedTime.getTimeRange();
        boolean isTimeRange = selectedTime.getTimeRange().length > 1;
        String sqlTemplate = isTimeRange
                ? SqlTemplate.WINDOW_COMPONENT_QUERY
                : SqlTemplate.COMPONENT_QUERY;
        String body = sqlTemplate
                .replace("$PROJECTION", joinResults[0])
                .replace("$SRC", joinResults[1])
                .replace("$FILTER", condition);
        boolean isAbsTime = TimeRangeType.ABS.equals(selectedTime.getTimeRangeType());
        if (isTimeRange) {
            FrequentlyAggregate frequency = selectedTime
                    .getFrequency();
            int difTime = isAbsTime ? frequency.calDif(timeRange[0], timeRange[1]) : frequency.calDifRela(timeRange[0], timeRange[1]);
            String groupBy = getGroupByFromProjection(joinResults[0]);
            int candidate;
            switch (selectedTime.getLogicalQuantifier()) {
                case ALL:
                    candidate = difTime;
                    break;
                case EXISTS:
                    candidate = 1;
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected value: " + selectedTime.getLogicalQuantifier());
            }
            body = body.replace("$CANDIDATE", String.valueOf(candidate)).replace(
                    "$GROUP_BY",
                    groupBy
            );
        }

        return new SubQueryContentRecord(body, tree.getRoot().getId());
    }

    private void buildHierarchicalTreeQuery() {
        DepthFirstIterator<MerkleTreeNode, EmptyEdge> traverser = new DepthFirstIterator<>(
                compressTree,
                compressTree.getRoot()
        );
        traverser.addTraversalListener(
                new TraversalListenerHaveDefault<MerkleTreeNode, EmptyEdge>() {
                    @Override
                    public void vertexFinished(VertexTraversalEvent<MerkleTreeNode> e) {
                        MerkleTreeNode node = e.getVertex();
                        List<MerkleTreeNode> children = compressTree
                                .outgoingEdgesOf(node)
                                .stream()
                                .map(compressTree::getEdgeTarget)
                                .collect(Collectors.toList());
                        if (children.isEmpty()) {
                            return;
                        }
                        LogicalOperator logical = node.getLogical();
                        List<String> childQueries = children
                                .stream()
                                .map(MerkleTreeNode::getId)
                                .map(sub -> "SELECT msisdn FROM " + sub)
                                .collect(Collectors.toList());
                        String joinType = logical.equals(LogicalOperator.AND)
                                ? "INTERSECT"
                                : "UNION";
                        String body = String.join("\n\t" + joinType + "\n\t", childQueries);
                        String query = "SELECT msisdn FROM (\n" +
                                "\t" + body + ") AS t";
                        subQueryInfoRecord.add(new SubQueryContentRecord(query, node.getId()));
                    }
                }
        );
        traverser.forEachRemaining(vertex -> {
        });
    }

    @Override
    public String buildFinalQuery() {
        buildHierarchicalTreeQuery();
        int lastIdx = subQueryInfoRecord.size() - 1;
        SubQueryContentRecord lastQuery = subQueryInfoRecord.remove(lastIdx);
        String ctaClause = QueryClauseTranslator.transCtaClause(subQueryInfoRecord);
        return SqlTemplate.HAVE_CTA_QUERY.replace("$CTE", ctaClause).replace(
                "$MAIN_QUERY",
                lastQuery.getBody()
        );
    }

    @Override
    public String buildQueryGetNumber() {
        buildHierarchicalTreeQuery();
        int lastIdx = subQueryInfoRecord.size() - 1;
        String lastQuery = subQueryInfoRecord.get(lastIdx).getAlias();
        String queryBody = "SELECT COUNT(*)\n" +
                "FROM " + lastQuery;
        String ctaClause = QueryClauseTranslator.transCtaClause(subQueryInfoRecord);
        return SqlTemplate.HAVE_CTA_QUERY.replace("$CTE", ctaClause).replace(
                "$MAIN_QUERY",
                queryBody
        );
    }
}
