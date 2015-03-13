package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface WorkflowFactory{

    /**
     * Must be called once as the first method on the factory. Initiates the main branch starting point.  
     */
    DslValidationBlock<WorkflowFactory> start();

}
