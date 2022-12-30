package ee.telekom.workflow.api;

import java.time.Duration;

import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.core.GraphImpl;

/**
 * Builds a {@link Graph} based on a {@link Tree} of {@link Row}s. 
 */
public class GraphBuilder{

    private NodeBuilder nodeBuilder = new NodeBuilder();
    private TransitionBuilder transitionBuilder = new TransitionBuilder();
    private GraphImpl graph;
    private Tree<Row> root;

    public GraphBuilder( String name, int version, boolean keepHistory, Duration archiveDuration, Tree<Row> root ){
        this.graph = new GraphImpl( name, version, keepHistory, archiveDuration );
        this.root = root;
    }

    /**
     * Builds the {@link Graph} based on the tree. The depth's of a node in the tree equals the level of indentation of
     * a row in the typical formating. For instance, the following definition has a number of rows with depth 1 and one row
     * with depth 2.
     * <pre>
     *   .start()
     *   .if_(1,"condition)
     *     .value("account").call(2,"accountService","findAccount","{accountId}");
     *   .endIf()
     *   .end()
     * </pre>
     * <ol>
     * <li>Traverses the tree in pre-order and creates a {@link Node} for every row. There are few rows such as START that actually don't produce a node.  
     * <li>Creates transitions based on the row's main element's type.
     * </ol>
     * @return
     */
    public Graph build(){
        boolean isStartNode = true;
        for( Tree<Row> current = root.findPreOrderNext(); current != null; current = current.findPreOrderNext() ){
            Node node = nodeBuilder.createNode( current );
            if( node == null ){
                // The current element does not produce a node. E.g. START, END, ELSE_IF, ELSE, BRANCH
                continue;
            }
            current.getContent().setNode( node );
            if( isStartNode ){
                graph.setStartNode( node );
                isStartNode = false;
            }
            else{
                graph.addNode( node );
            }
        }

        for( Tree<Row> current = root.findPreOrderNext(); current != null; current = current.findPreOrderNext() ){
            transitionBuilder.createTransitions( graph, current );
        }

        return graph;
    }

}
