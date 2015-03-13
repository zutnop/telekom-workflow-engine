package ee.telekom.workflow.graph;

import ee.telekom.workflow.core.workitem.WorkItem;

/**
 * Listener interface for events in the life cycle of a {@link WorkItem}.
 */
public interface GraphWorkItemEventListener{

    /**
     * Called after a new work item was created
     * 
     * @param instance
     *            the newly created work item
     */
    void onCreated( GraphWorkItem workItem );

    /**
     * Called after a work item was cancelled
     * 
     * @param instance
     *            the cancelled work item
     */
    void onCancelled( GraphWorkItem workItem );

    /**
     * Called after a work item was completed
     * 
     * @param instance
     *            the completed work item
     */
    void onCompleted( GraphWorkItem workItem );

}
