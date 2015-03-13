package ee.telekom.workflow.core.retry;

public interface RetryService{

    /**
     * If the execution of a workflow (starting or aborting it, executing a task or completing a work item)
     * fails, then the error is stored in the database and the instance remains locked.
     * 
     * This method prepare the workflow instance for retrying the failed excecution.
     */
    void retry( long woinRefNum );

}
