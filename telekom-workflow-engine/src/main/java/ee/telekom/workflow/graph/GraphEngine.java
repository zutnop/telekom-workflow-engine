package ee.telekom.workflow.graph;

import java.util.Date;
import java.util.Map;

import ee.telekom.workflow.api.AutoRecovery;
import ee.telekom.workflow.core.workitem.WorkItem;

/**
 * A {@link GraphEngine} is the interface that is internally used used to
 * communicated with a graph engine.
 */
public interface GraphEngine extends GraphEngineFacade{

    /**
     * Adds a new, active token to the given instance pointing to the given node
     * with the given parent.
     * 
     * @param instance
     *            the instance to add to
     * @param node
     *            the node that the new token will point to
     * @param parent
     *            the new token's parent token
     * @return the new token
     */
    Token addToken( GraphInstance instance, Node node, Token parent );

    /**
     * Adds a new {@link SignalItem} to the given instance which is associated
     * to the given token. The {@link SignalItem} is awaiting a signal with the
     * given name.
     * 
     * @param instance
     *            the instance to add the new signal item to
     * @param token
     *            the token to associate the signal item with
     * @param signal
     *            the signal's name to wait for
     * @return the new signal item
     */
    GraphWorkItem addSignalItem( GraphInstance instance, Token token, String signal );

    /**
     * Adds a new {@link TimerItem} to the given instance which is associated to
     * the given token. The {@link TimerItem} is awaiting the given dueDate.
     * 
     * @param instance
     *            the instance to add the new timer item to
     * @param token
     *            the token to associate the timer item with
     * @param dueDate
     *            the due date to wait for
     * @return the new timer item
     */
    GraphWorkItem addTimerItem( GraphInstance instance, Token token, Date dueDate );

    /**
     * Adds a new {@link TaskItem} to the given instance which is associated to
     * the given token. The {@link TaskItem} is awaiting external execution.
     * 
     * @param instance
     *            the instance to add the new task item to
     * @param token
     *            the token to associate the task item with
     * @param bean
     *            the bean to call a method on
     * @param method
     *            the method to call to
     * @param arguments
     *            the method call's arguments
     * @return the new task item
     */
    GraphWorkItem addTaskItem( GraphInstance instance, Token token, String bean,
                               String method, AutoRecovery autoRecovery, Object[] arguments );

    /**
     * Adds a new {@link HumanTaskItem} to the given instance which is
     * associated to the given token. The {@link HumanTaskItem} is awaiting
     * external resolution.
     * 
     * @param instance
     *            the instance to add the new human task item to
     * @param token
     *            the token to associate the human task item with
     * @param role
     *            the role a user must be assigned to resolve the given task
     * @param user
     *            the user the task is assigned to or <code>null</code> if the
     *            assignment is role based.
     * @param arguments
     *            the task arguments
     * @return the new human task item
     */
    GraphWorkItem addHumanTaskItem( GraphInstance instance, Token token, String role,
                                    String user, Map<String, Object> arguments );

    /**
     * If existent, cancels the active work item associated with the given
     * token.
     */
    void cancelWorkItem( Token token );

    /**
     * A shorthand for {@link #complete(Token, Object, String)} using a the
     * default transition name {@link Transition#DEFAULT_TRANSITION_NAME}.
     */
    void complete( Token token, Object result );

    /**
     * Finishes the execution of the given {@link Token}'s current {@link Node}
     * on the associated {@link GraphInstance}. The method will take the
     * required steps to merge the given result into the graph instance's
     * {@link Environment} and to continue the token's execution along the
     * {@link Transition} with the given name.
     * 
     * <p>
     * 
     * If a call to {@link Node#execute(GraphEngine, Token)} does not contain a
     * call to this method, then the token's execution halts at that point. This
     * is generally referred to as a wait state. This occurs, for example, if
     * the action represented by that node must be done by a human or some
     * external system.
     * 
     * <br/>
     * 
     * When the external system has determined that the {@link Node} has
     * completed its work, it should invoke
     * {@link GraphEngineFacade#complete(WorkItem)} to continue executing the
     * graph instance.
     * 
     * 
     * @param token
     *            The token to continue execution on
     * @param result
     *            The result of the node execution
     * @param transitionName
     *            The transition's name along which to continue execution
     * 
     */
    void complete( Token token, Object result, String transitionName );

    /**
     * Cancels the given token. I.e. marks the token as inactive an call's the
     * {@link Node#cancel(GraphEngine, Token)} on the node that this token is
     * currently pointing to.
     */
    void cancel( Token token );

    /**
     * Marks the given token and all direct parent tokens active that do not
     * have any further active child tokens.
     */
    void terminate( Token token );

}