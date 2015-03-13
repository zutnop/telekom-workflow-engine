package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface DslSplit<Level> {

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

}
