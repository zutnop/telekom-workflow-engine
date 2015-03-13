package ee.telekom.workflow.executor.plugin;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphRepository;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.listener.HumanTaskEventListener;
import ee.telekom.workflow.listener.WorkflowInstanceEventListener;

/**
 * Provides plugin hooks for the workflow engine.
 * 
 * @author Christian Klock
 */
public interface WorkflowEnginePlugin{

    /**
     * Lifecycle method that is called in order to give the plugin a chance for custom initialization.
     * <p>
     * Is always called before any other method (except {@link #isStarted()}.
     * <p>
     * Will not throw an Exception if an error occurs. Use {@link #isStarted()} after a call to this
     * method in order to verify that the plug-in started successfully.
     */
    void start();

    /**
     * Lifecycle method that is called in order to give the plugin a chance for a clean shut-down.
     * <p>
     * It is guaranteed that none of the other methods (except {@link #start()} for a restart or 
     * {@link #isStarted()}) is called after the plugin is stopped.
     */
    void stop();

    /**
     * Returns whether the engine has successfully started.
     */
    boolean isStarted();

    /**
     * Returns the repository of graphs provided by the plugin. Returns null, if the plugin is not started.
     * @return
     */
    GraphRepository getGraphRepository();

    /**
     * Returns the bean with the given name as provided by the plugin.
     */
    Object getBean( String name );

    /**
     * Reloads all workflow definitions in the plugin's graph repository.
     */
    void reloadWorkflowDefinitions();

    /**
     * Notifies all {@link WorkflowInstanceEventListener}'s provided by the plugin that a graph instance was created.
     */
    void onWorkflowInstanceCreated( GraphInstance instance );

    /**
     * Notifies all {@link WorkflowInstanceEventListener}'s provided by the plugin that a workflow instance execution completed.
     */
    void onWorkflowInstanceCompleted( GraphInstance instance );

    /**
     * Notifies all {@link WorkflowInstanceEventListener}'s provided by the plugin that a workflow instance was aborted.
     */
    void onWorkflowInstanceAborted( GraphInstance instance );

    /**
     * Notifies all {@link HumanTaskEventListener}'s provided by the plugin that a human task work item was created.
     */
    void onHumanTaskCreated( GraphWorkItem workItem );

    /**
     * Notifies all {@link HumanTaskEventListener}'s provided by the plugin that a human task work item was completed.
     */
    void onHumanTaskCompleted( GraphWorkItem workItem );

    /**
     * Notifies all {@link HumanTaskEventListener}'s provided by the plugin that a human task work item was aborted.
     */
    void onHumanTaskCancelled( GraphWorkItem workItem );

    /**
     * Notifies all {@link HumanTaskEventListener}'s provided by the plugin that a human task work item was aborted.
     * <p>
     * This method is called rather than {@link #onHumanTaskCancelled(GraphWorkItem)}, if the associates workflow
     * instance's workflow definition is not provided by the plugin anymore. 
     */
    void onHumanTaskCancelled( WorkflowInstance workflowInstance, WorkItem workItem );

}
