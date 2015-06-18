package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface DslValidationBlock<Level> extends DslMainBlock<Level>{

    /**
     * Checks a required input value. 
     * 
     * Tests that the input variable's value is set in the environment (possibly also to <code>null</code>)
     * and that this value is assignable to the defined type. 
     * 
     * NB! Calls to one of the overloaded validateInputVariable methods are only possible at the beginning of the workflow definition.
     * 
     * @param id node id
     * @param variable variable name
     * @param type type that the variable's value must be assignable to.
     */
    public DslValidationBlock<Level> validateInputVariable( int id, String variable, Class<?> type );

    /**
     * Like {@link #validateInputVariable(int, String, Class, boolean, Object)} with a default value of <code>null</code>
     */
    public DslValidationBlock<Level> validateInputVariable( int id, String variable, Class<?> type, boolean isRequired );

    /**
     * Checks a required or optional input value.
     * 
     * For a required input variable, it checks that the variable's value is set in the environment (possibly also to <code>null</code>)
     * and that this value is assignable to the defined type. 
     * For an optional input variable, it initialises the value to a default, if it is not yet set in the environment and ensures that
     * its value is assignable to the defined type. 
     * 
     * NB! Calls to one of the overloaded validateInputVariable methods are only possible at the beginning of the workflow definition.
     * 
     * @param id node id
     * @param variable variable name
     * @param type type that the variable's value must be assignable to
     * @param isRequired whether the variable is a required or an optional input
     * @param defaultValue a default value if the optional input value is not given
     */
    public DslValidationBlock<Level> validateInputVariable( int id, String variable, Class<?> type, boolean isRequired, Object defaultValue );

}
