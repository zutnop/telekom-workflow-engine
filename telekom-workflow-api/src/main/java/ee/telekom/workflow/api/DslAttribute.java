package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface DslAttribute<Level> {

    /**
     * Adds an argument to the preceding node.
     * @param name the arguments name
     * @param value the arguments value (any java object or "${EL_EXPRESSION}")
     */
    DslAttribute<Level> withAttribute( String name, Object value );

    /**
     * Block end tag, closing the iteration of attributes.
     * @return
     */
    Level done();

}
