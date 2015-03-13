package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface DslMainBlock<Level> extends DslBlock<DslMainBlock<Level>>{

    /**
     * Must be called once as the last method on the factory.
     */
    void end();

}
