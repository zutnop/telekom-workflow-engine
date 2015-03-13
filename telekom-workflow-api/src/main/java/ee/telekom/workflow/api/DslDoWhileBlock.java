package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface DslDoWhileBlock<Level> extends DslBlock<DslDoWhileBlock<Level>>{

    /**
     * Block end tag, checks the condition result, if true, then executes the block content again, if false, then ends the block.
     * 
     * @param id node id
     * @param condition expression language condition ("EL_EXPRESSION")
     */
    Level doWhile( int id, String condition );

}
