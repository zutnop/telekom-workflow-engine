package ee.telekom.workflow.facade;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.core.abort.AbortService;
import ee.telekom.workflow.core.common.UnexpectedStatusException;
import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.error.ExecutionError;
import ee.telekom.workflow.core.error.ExecutionErrorDao;
import ee.telekom.workflow.core.lock.LockService;
import ee.telekom.workflow.core.retry.RetryService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.core.workitem.WorkItemDao;
import ee.telekom.workflow.executor.GraphEngineFactory;
import ee.telekom.workflow.facade.model.CreateWorkflowInstance;
import ee.telekom.workflow.facade.model.ExecutionErrorState;
import ee.telekom.workflow.facade.model.SearchWorkflowInstances;
import ee.telekom.workflow.facade.model.WorkItemState;
import ee.telekom.workflow.facade.model.WorkflowInstanceFacadeStatus;
import ee.telekom.workflow.facade.model.WorkflowInstanceState;
import ee.telekom.workflow.facade.util.StatusUtil;
import ee.telekom.workflow.facade.workflowinstance.WorkflowInstanceStateDao;
import ee.telekom.workflow.facade.workflowinstance.WorkflowStatusCount;
import ee.telekom.workflow.facade.workitem.WorkItemStateDao;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.WorkItemStatus;
import ee.telekom.workflow.util.JsonUtil;

/*
 * Note that each workflow instance is associated to a particular cluster and that only each 
 * cluster may manipulate only its own workflow instances. This behaviour is enforced by the 
 * SQL queries in {@link WorkflowInstanceStateDao} and {@link WorkItemStateDao}. All the other
 * services and DAOs do not explicitly test this. (Actually they rely on the fact, that internally
 * the engine never manipulates "foreign" workflow instances.) So, for these services and DAOs we 
 * need to explicitly validate the clusterName field.
 */
@Service
@Transactional
public class WorkflowEngineFacadeImpl implements WorkflowEngineFacade{

    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private AbortService abortService;
    @Autowired
    private RetryService retryService;
    @Autowired
    private WorkflowInstanceStateDao workflowInstanceStateDao;

    @Autowired
    private WorkItemDao workItemDao;
    @Autowired
    private WorkItemStateDao workItemStateDao;

    @Autowired
    private ExecutionErrorDao executionErrorDao;

    @Autowired
    private GraphEngineFactory graphEngineFactory;
    @Autowired
    private LockService lockService;
    @Autowired
    private WorkflowEngineConfiguration config;

    @Override
    public void createWorkflowInstance( CreateWorkflowInstance request ){
        WorkflowInstance woin = workflowInstanceService.create( request.getWorkflowName(), request.getWorkflowVersion(), request.getArguments(),
                request.getLabel1(), request.getLabel2() );
        request.setRefNum( woin.getRefNum() );
    }

    @Override
    public void createWorkflowInstances( List<CreateWorkflowInstance> requests ){
        for( CreateWorkflowInstance request : requests ){
            createWorkflowInstance( request );
        }
    }

    @Override
    public void abortWorkflowInstance( long woinRefNum ){
        validateClusterNameOfWorkflowInstance( woinRefNum );
        validateCurrentlyNotExecuting( woinRefNum );
        abortService.abort( woinRefNum );
    }

    @Override
    public void suspendWorkflowInstance( long woinRefNum ){
        validateClusterNameOfWorkflowInstance( woinRefNum );
        validateCurrentlyNotExecuting( woinRefNum );
        workflowInstanceService.suspend( woinRefNum );
    }

    @Override
    public void resumeWorkflowInstance( long woinRefNum ){
        validateClusterNameOfWorkflowInstance( woinRefNum );
        workflowInstanceService.resume( woinRefNum );
    }

    @Override
    public void retryWorkflowInstance( long woinRefNum ){
        validateClusterNameOfWorkflowInstance( woinRefNum );
        retryService.retry( woinRefNum );
    }

    @Override
    public void sendSignalToWorkflowInstance( long woinRefNum, String signal, Object argument ){
        String jsonResult = JsonUtil.serialize( argument, true );
        workItemStateDao.updateStatusAndResultByInstanceAndSignal(
                woinRefNum,
                signal,
                WorkItemStatus.EXECUTED,
                WorkItemStatus.NEW,
                jsonResult );
    }

    @Override
    public void sendSignalToWorkItem( long woitRefNum, String signal, Object argument ){
        String jsonResult = JsonUtil.serialize( argument, true );
        workItemStateDao.updateStatusAndResultByWorkItemAndSignal(
                woitRefNum,
                signal,
                WorkItemStatus.EXECUTED,
                WorkItemStatus.NEW,
                jsonResult );
    }

    @Override
    public void sendSignalByLabel1( String label1, String signal, Object argument ){
        String jsonResult = JsonUtil.serialize( argument, true );
        workItemStateDao.updateStatusAndResultByLabel1AndSignal(
                label1,
                signal,
                WorkItemStatus.EXECUTED,
                WorkItemStatus.NEW,
                jsonResult );
    }

    @Override
    public void sendSignalByLabels( String label1, String label2, String signal, Object argument ){
        String jsonResult = JsonUtil.serialize( argument, true );
        workItemStateDao.updateStatusAndResultByLabelsAndSignal(
                label1,
                label2,
                signal,
                WorkItemStatus.EXECUTED,
                WorkItemStatus.NEW,
                jsonResult );
    }

    @Override
    public void skipTimer( long woitRefNum ){
        workItemStateDao.updateDueDate( woitRefNum, new Date(), WorkItemStatus.NEW );
    }

    @Override
    public boolean assignHumanTask( long woitRefNum, String user ){
        return workItemStateDao.updateUserName( woitRefNum, WorkItemStatus.NEW, user );
    }

    @Override
    public void submitTask( long woitRefNum, Object result ){
        validateClusterNameOfWorkItem( woitRefNum );
        String jsonResult = JsonUtil.serialize( result, true );
        WorkItemStatus expectedStatus = WorkItemStatus.NEW;
        boolean updateFailed = !workItemDao.updateStatusAndResult( woitRefNum, WorkItemStatus.EXECUTED, expectedStatus, jsonResult );
        if( updateFailed ){
            throw new UnexpectedStatusException( expectedStatus );
        }
    }

    @Override
    public void submitHumanTask( long woitRefNum, Object result ){
        validateClusterNameOfWorkItem( woitRefNum );
        String jsonResult = JsonUtil.serialize( result, true );
        WorkItemStatus expectedStatus = WorkItemStatus.NEW;
        boolean updateFailed = !workItemDao.updateStatusAndResult( woitRefNum, WorkItemStatus.EXECUTED, expectedStatus, jsonResult );
        if( updateFailed ){
            throw new UnexpectedStatusException( expectedStatus );
        }
    }

    @Override
    public WorkflowInstanceState findWorkflowInstance( long woinRefNum, Boolean isActive ){
        WorkflowInstanceState result;
        if( isActive == null || isActive ){
            result = workflowInstanceStateDao.find( woinRefNum, true );
            if( result == null ){
                result = workflowInstanceStateDao.find( woinRefNum, false );
            }
        }
        else{
            result = workflowInstanceStateDao.find( woinRefNum, false );
        }
        return result;
    }

    @Override
    public List<WorkflowInstanceState> findWorkflowInstancesByLabel1( String label1, boolean activeOnly ){
        return workflowInstanceStateDao.findByLabel1( label1, activeOnly );
    }

    @Override
    public List<WorkflowInstanceState> findWorkflowInstancesByLabels( String label1, String label2, boolean activeOnly ){
        return workflowInstanceStateDao.findByLabels( label1, label2, activeOnly );
    }

    @Override
    public List<WorkflowInstanceState> findWorkflowInstances( SearchWorkflowInstances request ){
        return workflowInstanceStateDao.find( request );
    }

    @Override
    public WorkItemState findWorkItem( long woitRefNum, Boolean isInstanceActive ){
        WorkItemState result;
        if( isInstanceActive == null || isInstanceActive ){
            result = workItemStateDao.find( woitRefNum, true );
            if( result == null ){
                result = workItemStateDao.find( woitRefNum, false );
            }
        }
        else{
            result = workItemStateDao.find( woitRefNum, false );
        }
        return result;
    }

    @Override
    public WorkItemState findActiveWorkItemByTokenId( long woinRefNum, int tokenId ){
        return workItemStateDao.findActive( woinRefNum, tokenId );
    }

    @Override
    public List<WorkItemState> findWorkItems( long woinRefNum, Boolean isInstanceActive ){
        List<WorkItemState> result;
        if( isInstanceActive == null || isInstanceActive ){
            result = workItemStateDao.findByWoinRefNum( woinRefNum, true );
            if( result.isEmpty() ){
                result = workItemStateDao.findByWoinRefNum( woinRefNum, false );
            }
        }
        else{
            result = workItemStateDao.findByWoinRefNum( woinRefNum, false );
        }
        return result;
    }

    @Override
    public List<WorkItemState> findActiveHumanTasksByRole( String role ){
        return workItemStateDao.findActiveByRole( role );
    }

    @Override
    public List<WorkItemState> findActiveHumanTasksByUser( String user ){
        return workItemStateDao.findActiveByUser( user );
    }

    @Override
    public List<WorkItemState> findActiveHumanTasksByRoleAndUser( String role, String user ){
        return workItemStateDao.findActiveByRoleAndUser( role, user );
    }

    @Override
    public ExecutionErrorState findExecutionError( long woinRefNum ){
        validateClusterNameOfWorkflowInstance( woinRefNum );
        ExecutionError error = executionErrorDao.findByWoinRefNum( woinRefNum );
        if( error == null ){
            return null;
        }
        else{
            ExecutionErrorState result = new ExecutionErrorState();
            result.setRefNum( error.getRefNum() );
            result.setWoinRefNum( error.getWoinRefNum() );
            result.setWoitRefNum( error.getWoitRefNum() );
            result.setErrorText( error.getErrorText() );
            result.setErrorDetails( error.getErrorDetails() );
            return result;
        }
    }

    @Override
    public Map<Long, Date> getNextActiveTimerDueDates( List<Long> woinRefNums ){
        return workItemStateDao.findNextActiveTimerDueDates( woinRefNums );
    }

    @Override
    public Set<Long> getWorkflowInstancesWithActiveHumanTask( List<Long> woinRefNums ){
        return workItemStateDao.findHasActiveHumanTask( woinRefNums );
    }

    @Override
    public Set<String> getDeployedWorkflowNames(){
        Set<String> result = new TreeSet<>( String.CASE_INSENSITIVE_ORDER );
        for( Graph graph : graphEngineFactory.getGraphs() ){
            result.add( graph.getName() );
        }
        return result;
    }

    @Override
    public Set<String> getKnownWorkflowNames(){
        Set<String> result = new TreeSet<>( String.CASE_INSENSITIVE_ORDER );
        result.addAll( getDeployedWorkflowNames() );
        result.addAll( workflowInstanceStateDao.findWorkflowNamesWithInstances() );
        return result;
    }

    @Override
    public Map<String, Map<WorkflowInstanceFacadeStatus, Integer>> getWorkflowStatistics(){
        Map<String, Map<WorkflowInstanceFacadeStatus, Integer>> statistics = createMapWithExistingGraphNames();
        List<WorkflowStatusCount> workflowStatusCounts = workflowInstanceStateDao.findWorklowStatusCount();
        for( WorkflowStatusCount workflowStatusCount : workflowStatusCounts ){
            Map<WorkflowInstanceFacadeStatus, Integer> workflowStatistics = statistics.get( workflowStatusCount.getWorkflowName() );
            if( workflowStatistics == null ){
                workflowStatistics = new HashMap<>();
                statistics.put( workflowStatusCount.getWorkflowName(), workflowStatistics );
            }
            WorkflowInstanceFacadeStatus status = StatusUtil.toFacade( workflowStatusCount.getStatus() );
            Integer count = workflowStatistics.get( status );
            if( count == null ){
                count = 0;
            }
            count = count + workflowStatusCount.getCount();
            workflowStatistics.put( status, count );
        }

        return statistics;
    }

    private Map<String, Map<WorkflowInstanceFacadeStatus, Integer>> createMapWithExistingGraphNames(){
        Map<String, Map<WorkflowInstanceFacadeStatus, Integer>> groupedByWorkflowNameAndStatus = new TreeMap<>();
        for( Graph graph : graphEngineFactory.getGraphs() ){
            groupedByWorkflowNameAndStatus.put( graph.getName(), new HashMap<WorkflowInstanceFacadeStatus, Integer>() );
        }
        return groupedByWorkflowNameAndStatus;
    }

    @Override
    public boolean isNodeInMasterRole(){
        return lockService.refreshOwnLock();
    }

    // Please see the class level documentation for a reasoning on this method.
    private void validateClusterNameOfWorkflowInstance( long woinRefNum ){
        WorkflowInstance woin = workflowInstanceService.find( woinRefNum );
        String clusterName = config.getClusterName();
        if( !ObjectUtils.equals( clusterName, woin.getClusterName() ) ){
            throw new IllegalArgumentException( "The workflow instance " + woinRefNum + " is not executed by this cluster " + clusterName );
        }
    }

    // Please see the class level documentation for a reasoning on this method.
    private void validateClusterNameOfWorkItem( long woitRefNum ){
        WorkItem woit = workItemDao.findByRefNum( woitRefNum );
        validateClusterNameOfWorkflowInstance( woit.getWoinRefNum() );
    }

    private void validateCurrentlyNotExecuting( long woinRefNum ) {
        WorkflowInstance woin = workflowInstanceService.find( woinRefNum );
        if ( !woin.isLocked() ) {
            return;
        }
        if ( WorkflowInstanceStatus.STARTING.equals( woin.getStatus() )
            || WorkflowInstanceStatus.ABORTING.equals( woin.getStatus() ) ) {
            throw new UnexpectedStatusException( "The workflow instance is currently executing" );
        }
        List<WorkItem> woits = workItemDao.findActiveByWoinRefNum( woinRefNum );
        for (WorkItem woit : woits) {
            if ( WorkItemStatus.EXECUTING.equals( woit.getStatus() )
                || WorkItemStatus.COMPLETING.equals( woit.getStatus() ) ) {
                throw new UnexpectedStatusException( "The workflow instance is currently executing" );
            }
        }
    }

}