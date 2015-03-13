package ee.telekom.workflow.graph;

import ee.telekom.workflow.core.workitem.WorkItem;

/**
 * A {@link Node} corresponds to an action in a workflow definition. E.g. it may
 * perform some function. In some cases it may not complete immediately, but
 * enter a wait state. At some point it will be completed and the execution can
 * continue.
 * 
 * <p>
 * 
 * Every node may have multiple outgoing {@link Transition}s. When a node is
 * completed, it may pick which transition(s) to proceed trough.
 * 
 * <p>
 * 
 * Note, that node implementations may not contain any {@link GraphInstance} 
 * specific execution state. If a node implementation requires such state, this
 * state either must reside in the {@link Token} implementation or in the 
 * graph instance's {@link Environment}.
 */
public interface Node{

    /**
     * Returns the node's unique id. The id must be unique to the containing
     * {@link Graph}.
     * 
     * @return the node's unique id
     */
    int getId();

    /**
     * Returns the node's name. A node may have a name describing it within the
     * workflow.
     * 
     * @return the node's name
     */
    String getName();

    /**
     * Performs node specific logic. Either from the execute method, or later
     * from the outside, the {@link GraphEngineFacade#complete(WorkItem)} method
     * must be called to continue executing the {@link GraphInstance}.
     * 
     * @param engine
     *            the {@link GraphEngine} which is performing the execution
     * @param token
     *            the {@link Token} which is currently executing in this node
     */
    void execute( GraphEngine engine, Token token );

    /**
     * Called when the {@link GraphEngine} cancels the execution of a
     * {@link Token}, that is currently pointing to this node and is in a wait
     * state.
     * 
     * @param engine
     *            the {@link GraphEngine} which is performing the execution
     * @param token
     *            the {@link Token} pointing to this node
     */
    void cancel( GraphEngine engine, Token token );

    /**
     * When a token execution on a node completes and returns control back to
     * the engine, then the engine mandates the node to store the node execution
     * result into the graph instance's environment.
     * 
     * @param environment
     *            the environment to store the result in
     * @param result
     *            the result to be stored
     */
    void store( Environment environment, Object result );

}
