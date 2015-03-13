package ee.telekom.workflow.executor;

/**
 * Provides the execution logic for the 4 work unit types.
 * 
 * @author Christian Klock
 */
public interface WorkflowExecutor{

    void startWorkflow( long woinRefNum );

    void abortWorkflow( long woinRefNum );

    void completeWorkItem( long woinRefNum, long woitRefNum );

    void executeTask( long woinRefNum, long woitRefNum );

}