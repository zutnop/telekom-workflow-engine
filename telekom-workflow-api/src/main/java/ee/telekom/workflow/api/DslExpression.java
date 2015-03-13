package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface DslExpression<Level> {

    /**
     * Synchronous bean method call. Can be defined as a stand-alone node, or next to a <code>variable/variables</code> node to save the returned object.
     * 
     * @param id node id
     * @param beanName bean name (from Spring application context)
     * @param methodName method name to call
     * @param arguments method arguments (any java object or "${EL_EXPRESSION}")
     */
    Level call( int id, String beanName, String methodName, Object... arguments );

    /**
     * Asynchronous bean method call. Can be defined as a stand-alone node, or next to a <code>variable/variables</code> node to save the returned object.
     * <p>
     * ### WAIT STATE ###
     * 
     * @param id node id
     * @param beanName bean name (from Spring application context)
     * @param methodName method name to call
     * @param arguments method arguments (any java object or "${EL_EXPRESSION}")
     */
    Level callAsync( int id, String beanName, String methodName, Object... arguments );

    /**
     * Create a human task for given role/assignee. Can be defined as a stand-alone node, or next to a <code>variable/variables</code> node to save the returned object.
     * Arguments may be provided by a a withArgument call.
     * <p>
     * NB! It's mandatory to fill either the roleName or assignee or both fields!
     * <p>
     * ### WAIT STATE ###
     * 
     * @param id node id
     * @param roleName assign to a role
     * @param assignee assign to a person
     */
    DslAttribute<Level> humanTask( int id, String roleName, String assignee );

    /**
     * Wait until a signal matching the given type is received. Can be defined as a stand-alone node, or next to a <code>variable/variables</code> node to save the signal argument.
     * <p>
     * ### WAIT STATE ###
     * 
     * @param id node id
     * @param type matching signal type
     */
    Level waitSignal( int id, String type );

}
