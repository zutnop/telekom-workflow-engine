package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface DslElseBlock<Level> extends DslBlock<DslElseBlock<Level>>{

    /**
     * Block end tag, closes the previously started if-elseIf-else-endIf block.
     */
    Level endIf();

}
