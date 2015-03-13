package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface DslWhileDoBlock<Level> extends DslBlock<DslWhileDoBlock<Level>>{

    /**
     * Block end tag, closes the previously started whileDo block. 
     */
    Level whileDo();

}
