package ee.telekom.workflow.facade.model;

/**
 * Enumeration of workflow instance statuses.
 * 
 * The given statuses are not the same statuses as used by the engine internally and the one provided by
 * {@link WorkflowInstanceState#getStatus()}. The given statuses summarize the internal statuses to 
 * hide internal details and give an easier meaning. 
 * 
 * @author Christian Klock
 *
 */
public enum WorkflowInstanceFacadeStatus{
    /**
     * The workflow instance is active and therefore subject to execution.
     */
    ACTIVE,
    /**
     * The workflow instance is in an error status. It's execution is halted until the error is resolved.
     */
    ERROR,
    /**
     * The workflow instance execution is suspended.
     */
    SUSPENDED,
    /**
     * The workflow instance is in a final state. It is aborted.
     */
    ABORTED,
    /**
     * The workflow instance is in a final state. It is fully executed.
     */
    EXECUTED;
}