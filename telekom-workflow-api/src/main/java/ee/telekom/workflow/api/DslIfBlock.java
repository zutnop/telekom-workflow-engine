package ee.telekom.workflow.api;

/**
 * DSL for workflow definitions.
 *
 * @author Erko Hansar
 * @author Christian Klock
 *
 * @see WorkflowDefinition
 */
public interface DslIfBlock<Level> extends DslBlock<DslIfBlock<Level>>{

    /**
     * If the given condition evaluates to true, then executes the branch content and ignores the other elseIf and else branches, otherwise evaluates the 
     * following elseIf or else condition.
     * 
     * @param condition expression language condition ("EL_EXPRESSION")
     */
    DslIfBlock<Level> elseIf( String condition );

    /**
     * The default execution branch for the cases when _if and elseIf branches evaluated all to <code>false</code>.
     */
    DslElseBlock<Level> else_();

    /**
     * Block end tag, closes the previously started if-elseIf-else-endIf block.
     */
    Level endIf();

}
