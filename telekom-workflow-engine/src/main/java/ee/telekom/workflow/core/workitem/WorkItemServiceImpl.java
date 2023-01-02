package ee.telekom.workflow.core.workitem;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.core.common.UnexpectedStatusException;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.graph.WorkItemStatus;
import ee.telekom.workflow.util.NoStackTraceException;

@Service
@Transactional
public class WorkItemServiceImpl implements WorkItemService{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private WorkItemDao dao;

    @Override
    public WorkItem find( long refNum ){
        return dao.findByRefNum( refNum );
    }

    @Override
    public List<WorkItem> findActiveByWoinRefNum( long woinRefNum ){
        return dao.findActiveByWoinRefNum( woinRefNum );
    }

    @Override
    public WorkItem findActiveByWoinRefNumAndTokenId( long woinRefNum, int tokenId ){
        return dao.findActiveByWoinRefNumAndTokenId( woinRefNum, tokenId );
    }

    @Override
    public void markExecuting( long refNum ){
        updateStatus( refNum, WorkItemStatus.EXECUTING, WorkItemStatus.NEW );
    }

    @Override
    public void markExecutedAndSaveResult( long refNum, String result ){
        updateStatusAndResult( refNum, WorkItemStatus.EXECUTED, WorkItemStatus.EXECUTING, result );
    }

    @Override
    public void markCompleting( long refNum ){
        // signals, tasks and human tasks have an execution step. timer's don't. therefore we allow transitions from NEW and EXECUTED.
        Collection<WorkItemStatus> expectedStatuses = Arrays.asList(
                WorkItemStatus.NEW,
                WorkItemStatus.EXECUTED );
        updateStatus( refNum, WorkItemStatus.COMPLETING, expectedStatuses );
    }

    @Override
    public void markCompleted( long refNum ){
        updateStatus( refNum, WorkItemStatus.COMPLETED, WorkItemStatus.COMPLETING );
    }

    @Override
    public void markCancelled( long refNum ){
        updateStatus( refNum, WorkItemStatus.CANCELLED, Arrays.asList( WorkItemStatus.NEW, WorkItemStatus.EXECUTED ) );
    }

    @Override
    public void handleExecutingError( long woinRefNum, long woitRefNum, Exception exception ){
        workflowInstanceService.handleCompleteError( woinRefNum, woitRefNum, exception );
        Collection<WorkItemStatus> expectedStatuses = Arrays.asList(
                WorkItemStatus.NEW,
                WorkItemStatus.EXECUTING,
                WorkItemStatus.EXECUTED );
        updateStatus( woitRefNum, WorkItemStatus.EXECUTING_ERROR, expectedStatuses );
    }

    @Override
    public void handleCompletingError( long woinRefNum, long woitRefNum, Exception exception ){
        workflowInstanceService.handleCompleteError( woinRefNum, woitRefNum, exception );
        Collection<WorkItemStatus> expectedStatuses = Arrays.asList(
                WorkItemStatus.EXECUTED,
                WorkItemStatus.COMPLETING,
                WorkItemStatus.COMPLETED );
        updateStatus( woitRefNum, WorkItemStatus.COMPLETING_ERROR, expectedStatuses );
    }

    @Override
    public void rewindAfterError( long refNum ) throws UnexpectedStatusException{
        WorkItem woit = find( refNum );
        if( WorkItemStatus.EXECUTING_ERROR.equals( woit.getStatus() ) ){
            markNewAfterExecutingError( refNum );
        }
        else if( WorkItemStatus.COMPLETING_ERROR.equals( woit.getStatus() ) ){
            if( WorkItemType.TIMER.equals( woit.getType() ) ){
                markNewAfterCompletingError( refNum );
            }
            else{
                markExecutedAfterCompletingError( refNum );
            }
        }
        else{
            throw new UnexpectedStatusException( Arrays.asList(
                    WorkItemStatus.EXECUTING_ERROR,
                    WorkItemStatus.COMPLETING_ERROR ) );
        }
    }

    private void markNewAfterExecutingError( long woitRefNum ) throws UnexpectedStatusException{
        updateStatus( woitRefNum, WorkItemStatus.NEW, WorkItemStatus.EXECUTING_ERROR );
    }

    private void markNewAfterCompletingError( long woitRefNum ) throws UnexpectedStatusException{
        updateStatus( woitRefNum, WorkItemStatus.NEW, WorkItemStatus.COMPLETING_ERROR );
    }

    private void markExecutedAfterCompletingError( long woitRefNum ) throws UnexpectedStatusException{
        updateStatus( woitRefNum, WorkItemStatus.EXECUTED, WorkItemStatus.COMPLETING_ERROR );
    }

    @Override
    public void recoverExecuting( String nodeName ) throws UnexpectedStatusException{
        int recovered = 0;
        int notRecovered = 0;
        Collection<WorkItem> workItems = dao.findByNodeNameAndStatus( nodeName, WorkItemStatus.EXECUTING );
        for( WorkItem woit : workItems ){
            if( woit.isAutoRetryOnRecovery() ){
                updateStatus( woit.getRefNum(), WorkItemStatus.NEW, WorkItemStatus.EXECUTING );
                workflowInstanceService.unlock( woit.getWoinRefNum() );
                recovered++;
            }
            else{
                Exception exception = new NoStackTraceException(
                        "Non-task work items, that are associated to a failed node, are not automatically recovered. Please recover manually!" );
                handleExecutingError( woit.getWoinRefNum(), woit.getRefNum(), exception );
                notRecovered++;
            }
        }
        String logMsg = "Recovered " + recovered + " and failed to recover " + notRecovered + " executing work item(s) for node " + nodeName;
        if( notRecovered > 0 ){
            log.error(logMsg);
        }
        else{
            log.info(logMsg);
        }
    }

    @Override
    public void recoverCompleting( String nodeName ) throws UnexpectedStatusException{
        Collection<WorkItem> workItems = dao.findByNodeNameAndStatus( nodeName, WorkItemStatus.COMPLETING );
        for( WorkItem woit : workItems ){
            if( WorkItemType.TIMER.equals( woit.getType() ) ){
                updateStatus( woit.getRefNum(), WorkItemStatus.NEW, WorkItemStatus.COMPLETING );
            }
            else{
                updateStatus( woit.getRefNum(), WorkItemStatus.EXECUTED, WorkItemStatus.COMPLETING );
            }
            workflowInstanceService.unlock( woit.getWoinRefNum() );
        }
        log.info( "Recovered {} completing work items for node {}", workItems.size(), nodeName );
    }

    private void updateStatus( long refNum, WorkItemStatus newStatus, WorkItemStatus expectedStatus ) throws UnexpectedStatusException{
        updateStatus( refNum, newStatus, Collections.singletonList( expectedStatus ) );
    }

    private void updateStatus( long refNum, WorkItemStatus newStatus, Collection<WorkItemStatus> expectedStatuses ) throws UnexpectedStatusException{
        boolean updatedFailed = !dao.updateStatus( refNum, newStatus, expectedStatuses );
        if( updatedFailed ){
            throw new UnexpectedStatusException( expectedStatuses );
        }
        else{
            log.debug( "Updated the status of workflow item {} to {}", refNum, newStatus );
        }
    }

    private void updateStatusAndResult( long refNum, WorkItemStatus newStatus, WorkItemStatus expectedStatus, String result ) throws UnexpectedStatusException{
        boolean updatedFailed = !dao.updateStatusAndResult( refNum, newStatus, expectedStatus, result );
        if( updatedFailed ){
            throw new UnexpectedStatusException( expectedStatus );
        }
        else{
            log.debug( "Updated the status of workflow item {} to {} and submitted result", refNum, newStatus );
        }
    }
}
