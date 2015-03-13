package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface DslBlock<Level> extends DslExpression<Level>{

    /**
     * Set an environment variable with the given name. The value comes from the next immediate node (value, call, callAsync, humanTask, waitSignal).
     * 
     * @param name name of the environment variable which is going to be set
     */
    DslVariable<Level> variable( String name );

    /**
     * Set environment variable with the given names. The value comes from the next immediate node (value, call, callAsync, humanTask, waitSignal). Used
     * when the value returning node return a <code>Map</code> result and we want to store one or more key-value pairs from this <code>Map</code> directly 
     * into the environment (instead of storing the whole <code>Map</code> under a new variable name and then accessing the key-value pairs as subfields 
     * inside this <code>Map</code> value).
     * <p>
     * If the returned <code>Map</code> contains 3 keys: x, y and z, then the call <code>.variables( "a=x", "b=y" )</code> should be read as: take 
     * <code>x</code> from method call result map and store it into environment as <code>a</code>, and take <code>y</code> from method call result map and 
     * store it into environment as <code>b</code>, and ignore <code>z</code>.  
     * 
     * @param nameMappings desired mappings of the environment variable: new_environment_variable_name=returned_map_key_name
     */
    DslVariable<Level> variables( String... nameMappings );

    /**
     * Creates a new workflow instance based.
     * 
     * @param id node id
     * @param workflowName the workflow's name to be created ("STRING_WORKFLOW_NAME" or "${EL_EXPRESSION}")
     * @param workflowVersion the workflow's version to be created or <code>null</code> for the latest
     * @param label1 first label ("STRING_LABEL" or "${EL_EXPRESSION}")
     * @param label2 second label ("STRING_LABEL" or "${EL_EXPRESSION}")
     */
    DslAttribute<Level> createInstance( int id, String workflowName, Integer workflowVersion, String label1, String label2 );

    /**
     * Wait until a defined time period has passed.
     * <p>
     * ### WAIT STATE ###
     * 
     * @param id node id
     * @param due either a constant Long value of milliseconds or an EL expression which returns the milliseconds ("LONG_VALUE" or "${EL_EXPRESSION}")
     */
    Level waitTimer( int id, String due );

    /**
     * Wait until a defined date has been reached
     * <p>
     * ### WAIT STATE ###
     * 
     * @param id node id
     * @param due an EL expression which returns the date ("${EL_EXPRESSION}")
     */
    Level waitUntilDate( int id, String due );

    /**
     * While the given condition is true, execute the block content and then check again.
     * <p>
     * Block start tag.
     * 
     * @param id node id
     * @param condition expression language condition ("EL_EXPRESSION")
     */
    DslWhileDoBlock<Level> whileDo( int id, String condition );

    /**
     * Start tag for a do-while block. Execution continues into the block content. 
     */
    DslDoWhileBlock<Level> doWhile();

    /**
     * Start tag for if-elseIf-else-endIf block. If the given condition evaluates to true, then executes the branch content and ignores the other elseIf 
     * and else branches, otherwise evaluates the following elseIf or else condition.
     * <p>
     * The if-elseIf-else-endIf block executes only one of it's branches.
     * 
     * @param id node id
     * @param condition expression language condition ("EL_EXPRESSION")
     */
    DslIfBlock<Level> if_( int id, String condition );

    /**
     * Start tag for splitting the execution into multiple parallel branches.
     * 
     * @param id node id
     */
    DslSplit<Level> split( int id );

}
