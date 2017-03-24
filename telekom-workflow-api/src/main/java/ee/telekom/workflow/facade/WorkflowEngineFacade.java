package ee.telekom.workflow.facade;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ee.telekom.workflow.facade.model.CreateWorkflowInstance;
import ee.telekom.workflow.facade.model.ExecutionErrorState;
import ee.telekom.workflow.facade.model.SearchWorkflowInstances;
import ee.telekom.workflow.facade.model.WorkItemState;
import ee.telekom.workflow.facade.model.WorkflowInstanceFacadeStatus;
import ee.telekom.workflow.facade.model.WorkflowInstanceState;
import ee.telekom.workflow.listener.WorkflowEngineHazelcastStartupListener;

/**
 * Provides a facade for the workflow engine.
 *
 * External components (web front ends, REST interfaces, ...) are obliged to use
 * an implementation of this facade to manage workflows.
 *
 * @author Christian Klock
 */
public interface WorkflowEngineFacade{

    /**
     * Creates a new workflow instance as described by the request and sets
     * the request's refNum field to the newly created workflow instance's refNum.
     *
     * A valid request specifies at least the workflow name. All other fields are
     * optional.
     */
    void createWorkflowInstance( CreateWorkflowInstance request );

    /**
     * Creates a new workflow instance for each request in the given list and sets
     * each request's refNum field to the newly created workflow instance's refNum.
     *
     * A valid request specifies at least the workflow name. All other fields are
     * optional.
     */
    void createWorkflowInstances( List<CreateWorkflowInstance> requests );

    /**
     * Aborts the workflow instance with the given refNum.
     * May be only called on workflow instance's which are not in EXECUTED or ABORTED status.
     */
    void abortWorkflowInstance( long woinRefNum );

    /**
     * Suspends the workflow instance with the given refNum.
     * May be only called on workflow instance in EXECUTING status.
     */
    void suspendWorkflowInstance( long woinRefNum );

    /**
     * Resumes the workflow instance with the given refNum.
     * May be only called on workflow instance in SUSPENDED status.
     */
    void resumeWorkflowInstance( long woinRefNum );

    /**
     * Retries the workflow instance's last action (that has failed).
     * May be only called on workflow instances in STARTING_ERROR, EXECUTING_ERROR or ABORTING_ERROR.
     */
    void retryWorkflowInstance( long woinRefNum );

    /**
     * Sends the signal to all matching signal work items of the given workflow instance using the given argument.
     */
    void sendSignalToWorkflowInstance( long woinRefNum, String signal, Object argument );

    /**
     * Sends the signal to the given signal work item using the given argument.
     */
    void sendSignalToWorkItem( long woitRefNum, String signal, Object argument );

    /**
     * Sends the signal to all matching signal work items of the any workflow instance with the given label1 using the given argument.
     */
    void sendSignalByLabel1( String label1, String signal, Object argument );

    /**
     * Sends the signal to all matching signal work items of the any workflow instance with the given label1 and label2 using the given argument.
     * NB! This method tests for a match in label1 and label2. I.e. a null value for label2 will
     * be tested as label2 IS NULL.
     */
    void sendSignalByLabels( String label1, String label2, String signal, Object argument );

    /**
     * Sets the given timer work item's due date to the current date.
     */
    void skipTimer( long woitRefNum );

    /**
     * Assigns the given human task work item to the given user.
     */
    boolean assignHumanTask( long woitRefNum, String user );

    /**
     * Submits the given task work item with the given result.
     */
    void submitTask( long woitRefNum, Object result );

    /**
     * Submits the given human task work item with the given result.
     */
    void submitHumanTask( long woitRefNum, Object result );

    /**
     * Finds a workflow instance with the given refNum using the given hint whether the
     * workflow instance is active.
     * If the hint is <code>false</code> the workflow instance is searched in the
     * archive table. Otherwise, it is first searched in the engine's main table
     * and if not found there in the archive table.
     */
    WorkflowInstanceState findWorkflowInstance( long woinRefNum, Boolean isActive );

    /**
     * Finds any workflow instance with the given label1. Hereby, activeOnly determines
     * whether or not the archive table is included in the search.
     */
    List<WorkflowInstanceState> findWorkflowInstancesByLabel1( String label1, boolean activeOnly );

    /**
     * Finds any workflow instance with the given label1. Hereby, activeOnly determines
     * whether or not the archive table is included in the search.
     * NB! This method tests for a match in label1 and label2. I.e. a null value for label2 will
     * be tested as label2 IS NULL.
     */
    List<WorkflowInstanceState> findWorkflowInstancesByLabels( String label1, String label2, boolean activeOnly );

    /**
     * Finds any workflow instance that satisfied the complex search request.
     */
    List<WorkflowInstanceState> findWorkflowInstances( SearchWorkflowInstances request );

    /**
     * Finds the work item with the given refNum using the given hint describing whether
     * the associated workflow instance is active or not.
     * If the hint is <code>false</code> the work item is searched in the
     * archive table. Otherwise, it is first searched in the engine's main table
     * and if not found there in the archive table.
     */
    WorkItemState findWorkItem( long woitRefNum, Boolean isInstanceActive );

    /**
     * Finds the work item of the given workflow instance that is associated
     * with the given token. Returns <code>null</code> if no such work item exists.
     */
    WorkItemState findActiveWorkItemByTokenId( long woinRefNum, int tokenId );

    /**
     * Returns all the given workflow instance's work items (active and non-active)
     * using the given  work items that are associated with
     * the given hint whether the workflow instance is active or not.
     * If the hint is <code>false</code> the work item is searched in the
     * archive table. Otherwise, it is first searched in the engine's main table
     * and if not found there in the archive table.
     */
    List<WorkItemState> findWorkItems( long woinRefNum, Boolean isInstanceActive );

    /**
     * Finds any active human task work item assigned to the given role.
     */
    List<WorkItemState> findActiveHumanTasksByRole( String role );

    /**
     * Finds any active human task work item assigned to the given user.
     */
    List<WorkItemState> findActiveHumanTasksByUser( String user );

    /**
     * Finds any active human task work item assigned to the given role and user.
     */
    List<WorkItemState> findActiveHumanTasksByRoleAndUser( String role, String user );

    /**
     * Finds the workflow instance's execution error or returns <code>null</code> if
     * the instance is not in an ERROR status.
     */
    ExecutionErrorState findExecutionError( long woinRefNum );

    /**
     * Finds the next active timer work item's due date for every workflow instance in given list.
     * The maps key is the workflow instance's refNum and the value the instance's next
     * active timer work item's due date. The map contains only entries for those workflow
     * instances that are currently waiting for at least 1 one timer to become due.
     */
    Map<Long, Date> getNextActiveTimerDueDates( List<Long> woinRefNums );

    /**
     * Finds those workflow instances that are currently waiting for at least 1 human task
     * to become submitted.
     */
    Set<Long> getWorkflowInstancesWithActiveHumanTask( List<Long> woinRefNums );

    /**
     * Returns the set of those workflow names that are currently deployed to the engine.
     */
    Set<String> getDeployedWorkflowNames();

    /**
     * Returns
     *   the set of those workflow names that are currently deployed to the engine
     *   PLUSS
     *   the set of those workflow names that are found in the engine's database.
     */
    Set<String> getKnownWorkflowNames();

    /**
     * Returns a map that describes workflow statistics.
     * The outer map's key is a workflow's name and the associated value is a
     * map of that holds the number of workflow instances by status.
     */
    Map<String, Map<WorkflowInstanceFacadeStatus, Integer>> getWorkflowStatistics();

    /**
     * Returns whether the engine deployment at hand is in the master role within its cluster.
     */
    boolean isNodeInMasterRole();

    /**
     * Registers listener that will be notified when engine Hazelcast instance has been started.
     */
    void registerHazelcastStartupListener( WorkflowEngineHazelcastStartupListener listener );

}
