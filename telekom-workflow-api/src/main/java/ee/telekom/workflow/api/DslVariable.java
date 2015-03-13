package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface DslVariable<Level> extends DslExpression<Level>{

    /**
     * Store the given constant value or expression result into the environment under the variable name from the previous node:
     * <code>.variable( "name" ).value( 1, "Heli Kopter" )</code>
     * <p>
     * NB! There is a Gotcha here: DSL parse time vs. workflow instance run time. All the java objects (for example new Date()) are created
     * at the DSL parse time (server startup) vs. all the EL expressions (for example "${NOW}") are evaluated at the runtime when
     * this workflow node is processed. So when you are doing <code>.variable( "name" ).value( 1, new Date() )</code> then you are
     * actually getting the server startup date value there, not the runtime moment, when this instance node 1 was processed. So you
     * should use only constant values (like strings, numbers, enums etc.) as java objects, and EL expressions for creating runtime
     * dynamic objects.
     * 
     * @param id node id 
     * @param value variable value (any java object (only suitable for constants, see NB!) or "${EL_EXPRESSION}")
     */
    Level value( int id, Object value );

}
