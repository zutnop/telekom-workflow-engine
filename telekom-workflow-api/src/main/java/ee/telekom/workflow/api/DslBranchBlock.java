package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface DslBranchBlock<Level> extends DslBlock<DslBranchBlock<Level>>{

    /**
     * Used inside split branches to cancel all other ongoing branches in this block, but the calling branch continues with it's normal operations.
     * 
     * @param id node id
     */
    DslBranchBlock<Level> escalate( int id );

    /**
     * Add a branch (parallel execution flow) into the active split.
     */
    DslBranchBlock<Level> branch();

    /**
     * Add a conditional branch (parallel execution flow) into the active split. Executed only if the condition evaluates to true.
     * 
     * @param condition expression language condition ("EL_EXPRESSION")
     */
    DslBranchBlock<Level> branch( String condition );

    /**
     * Block end tag, listens for the FIRST arriving branch, immediately cancels all the other ongoing branches in this block and continues execution
     * from after the block. 
     */
    Level joinFirst();

    /**
     * Block end tag, listens until ALL started branches have arrived and then continues execution from after the block. 
     */
    Level joinAll();

}
