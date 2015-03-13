package ee.telekom.workflow.core.abort;

import ee.telekom.workflow.core.common.UnexpectedStatusException;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;

/**
 * Provides a service to request a workflow instance to become aborted.
 * 
 * @author Christian Klock
 */
public interface AbortService{

    /**
     * Requests a workflow instance to become aborted.
     * <p>
     * This means, that its status is set to {@link WorkflowInstanceStatus#ABORT}.
     * Furthermore, if an error is associated with the instance, the error is deleted
     * and the instance is unlocked.
     * 
     * @throws UnexpectedStatusException if changing the instance status does not work, e.g. because the instance is already EXECUTED.
     */
    void abort( long woinRefNum );

}
