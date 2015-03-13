package ee.telekom.workflow.graph;

import java.util.List;

import ee.telekom.workflow.core.workitem.WorkItem;

/**
 * A {@link GraphInstance} represents an executing instance of a {@link Graph}.
 */
public interface GraphInstance{

    /**
     * A {@link GraphInstance} may be assigned an external id to associate it
     * with externally persisted state, buisness data, etc.
     * 
     * @return the graph instance's external id
     */
    Long getExternalId();

    /**
     * @return the graph being executed
     */
    Graph getGraph();

    /**
     * @return the graph instance's {@link Environment}
     */
    Environment getEnvironment();

    /**
     * @return the list of all tokens (both active and inactive) in the graph
     *         instance
     */
    List<Token> getTokens();

    /**
     * @return the list of all active tokens in the graph instance
     */
    List<Token> getActiveTokens();

    /**
     * {@link Token}s are created with a tree-like parent-child relation-ship.
     * Each token has exactly one parent and may have zero to many child tokens.
     * Child tokens are mainly created at fork nodes which produce 1-N outgoing
     * tokens. When a token is cancelled, all it's child tokens are cancelled as
     * well.
     * 
     * @return the list of all active child tokens of the given parent token
     */
    List<Token> getActiveChildTokens( Token parent );

    /**
     * Adds a token to the graph instance.
     * 
     * @param token
     *            the token to be added
     */
    void addToken( Token token );

    /**
     * A {@link GraphInstance} is completed if it does not contain any active
     * tokens.
     * 
     * @return true, if the graph instance is completed.
     */
    boolean isCompleted();

    /**
     * A {@link Token} has a graph instance specific id. It is a graph
     * instance's task to provide a sequence for such id numbers.
     * 
     * @return a new token id, as used when creating a new token
     */
    int nextTokenId();

    /**
     * When a token execution reaches a wait state, it produces a
     * {@link WorkItem} which describes the work that needs to be completed in
     * order to continue the token's execution.
     * <p>
     * This node is not intended to be called directly from a node's
     * {@link Node#execute(GraphEngine, Token)} method. They should rather call
     * methods such as
     * {@link GraphEngine#addSignalItem(GraphInstance, Token, String)} which
     * also notify {@link GraphWorkItemEventListener}s in addition to calling this
     * method.
     * 
     * @param workItem
     *            the work item to be added to the instance
     */
    void addWorkItem( GraphWorkItem workItem );

    /**
     * A token can be associated with 0 or 1 active work items. A work item is
     * considered active if its status is neither
     * {@link WorkItemStatus#COMPLETED} nor {@link WorkItemStatus#CANCELLED}.
     * 
     * @param token
     *            the token of which to find the active work item
     * @return the active work item associated with the given token or
     *         <code>null</code> if no such work item exists
     */
    GraphWorkItem getActiveWorkItem( Token token );

    /**
     * @return a list of all work items (both active and inactive) in the graph
     *         instance
     */
    List<GraphWorkItem> getWorkItems();

    /**
     * Adds the given event into the instance's history log. 
     * @param event information on an execution event
     */
    void addToHistory( String event );

    /**
     * Returns the instance's history log.
     * @return the instance's history log.
     */
    String getHistory();

    /**
     * A {@link GraphInstance} keeps a queue of tokens for which the engine
     * should attempt to proceed execution. This method peeks the top of this
     * queue, i.e. it returns the queue's head but does not remove it.
     * 
     * @return the token to continue execution with or <code>null</code> if
     *         currently no token can be executed
     */
    Token getFirstFromExecutionQueue();

    /**
     * Adds a token to the execution queue.
     * 
     * @param token
     *            the token to be added
     */
    void addToExecutionQueue( Token token );

    /**
     * Removes the head of the execution queue.
     */
    void removeFirstFromExecutionQueue();

}