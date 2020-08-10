package ee.telekom.workflow.executor.lifecycle;

/**
 * Service that provides logic to take appropriate actions in order to 
 * recover persisted "broken" workflow instance and work item states after a node failure.
 */
public interface HealthCheckService{

    /**
     * Called regularly to test whether there are any FAILED nodes and to take
     * appropriate actions to recover any potential damage by a node failure.
     */
    void healFailedNodes();

    /**
     * Called regularly to test whether there are any workflow instances that seem
     * to be stuck in the locked state and log and error to draw attention to this.
     */
    void checkForStuckWorkflows();

}
