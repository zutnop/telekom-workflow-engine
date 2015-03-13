package ee.telekom.workflow.executor.marshall;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphWorkItem;

/**
 * Provides a load and save service to persist a {@link GraphInstance}.
 * <p>
 * The repository converts between the persistent artifacts ({@link WorkflowInstance} and
 * {@link WorkItem}) and the non-persistent artifacts ({@link GraphInstance} and
 * {@link GraphWorkItem}).
 * 
 * @author Christian Klock
 */
public interface GraphInstanceRepository{

    /**
     * Loads the {@link GraphInstance} identified by the given workflow instance refNum.
     */
    GraphInstance load( long woinRefNum );

    /**
     * Saves the {@link GraphInstance} and therefore updates workflow instance and work item fields.
     * <p>
     * If the graph instance execution is completed, updates the workflow instance status to the
     * given completeStatus. This way, we can choose between EXECUTED and ABORTED which are
     * indistinguishable from the {@link GraphInstance} point of view. 
     */
    void save( GraphInstance graphInstance, WorkflowInstanceStatus completeStatus );

}