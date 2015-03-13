package ee.telekom.workflow.listener;

/**
 * Provides a listener that is notified when workflow instance lifecycle events occur.
 *  
 * @author Christian Klock
 */
public interface WorkflowInstanceEventListener{

    /**
     * Called before a workflow instance is started.
     * 
     * @param event
     *            information on the instance being started
     */
    void onStarting( WorkflowInstanceEvent event );

    /**
     * Called after a workflow instance was aborted.
     * 
     * @param event 
     *            information on the aborted instance
     */
    void onAborted( WorkflowInstanceEvent event );

    /**
     * Called after a workflow instance's execution finishes.
     * 
     * @param event
     *            information on the completed instance
     */
    void onExecuted( WorkflowInstanceEvent event );

}
