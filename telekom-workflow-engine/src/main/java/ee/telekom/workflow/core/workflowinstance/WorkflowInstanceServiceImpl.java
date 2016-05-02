package ee.telekom.workflow.core.workflowinstance;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.core.common.UnexpectedStatusException;
import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.error.ExecutionErrorService;
import ee.telekom.workflow.executor.marshall.Marshaller;

@Service
@Transactional
public class WorkflowInstanceServiceImpl implements WorkflowInstanceService{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private WorkflowInstanceDao dao;
    @Autowired
    private ExecutionErrorService executionErrorService;
    @Autowired
    private WorkflowEngineConfiguration config;

    @Override
    public WorkflowInstance create( String workflowName, Integer workflowVersion, Map<String, Object> arguments, String label1, String label2 ){
        WorkflowInstance woin = new WorkflowInstance();
        woin.setWorkflowName( workflowName );
        woin.setWorkflowVersion( workflowVersion );
        woin.setAttributes( Marshaller.serializeAttributes( arguments ) );
        woin.setLabel1( StringUtils.trimToNull( label1 ) );
        woin.setLabel2( StringUtils.trimToNull( label2 ) );
        woin.setClusterName( config.getClusterName() );
        woin.setLocked( false );
        woin.setStatus( WorkflowInstanceStatus.NEW );
        dao.create( woin );
        log.info( "Created workflow instance {}", woin.getRefNum() );
        return woin;
    }

    @Override
    public WorkflowInstance find( long refNum ){
        return dao.findByRefNum( refNum );
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void markStarting( long refNum ) throws UnexpectedStatusException{
        updateStatus( refNum, WorkflowInstanceStatus.STARTING, WorkflowInstanceStatus.NEW );
    }

    @Override
    public void markExecuting( long refNum ) throws UnexpectedStatusException{
        updateStatus( refNum, WorkflowInstanceStatus.EXECUTING, WorkflowInstanceStatus.STARTING );
    }

    @Override
    public void markExecuted( long refNum ) throws UnexpectedStatusException{
        updateStatus( refNum, WorkflowInstanceStatus.EXECUTED, WorkflowInstanceStatus.EXECUTING );
    }

    @Override
    public void markAbort( long refNum ) throws UnexpectedStatusException{
        Collection<WorkflowInstanceStatus> expectedStatuses = Arrays.asList(
                WorkflowInstanceStatus.NEW,
                WorkflowInstanceStatus.STARTING,
                WorkflowInstanceStatus.STARTING_ERROR,
                WorkflowInstanceStatus.EXECUTING,
                WorkflowInstanceStatus.EXECUTING_ERROR,
                WorkflowInstanceStatus.SUSPENDED );
        updateStatus( refNum, WorkflowInstanceStatus.ABORT, expectedStatuses );
    }

    @Override
    public void markAborting( long refNum ) throws UnexpectedStatusException{
        updateStatus( refNum, WorkflowInstanceStatus.ABORTING, WorkflowInstanceStatus.ABORT );
    }

    @Override
    public void markAborted( long refNum ) throws UnexpectedStatusException{
        updateStatus( refNum, WorkflowInstanceStatus.ABORTED, WorkflowInstanceStatus.ABORTING );
    }

    @Override
    public void assertIsExecuting( long refNum ) throws UnexpectedStatusException{
        WorkflowInstanceStatus status = dao.findStatusByRefNum( refNum );
        if ( !WorkflowInstanceStatus.EXECUTING.equals( status ) ){
            throw new UnexpectedStatusException( WorkflowInstanceStatus.EXECUTING );
        }
    }

    @Override
    public void rewindAfterError( long refNum ) throws UnexpectedStatusException{
        WorkflowInstance woin = find( refNum );
        if( WorkflowInstanceStatus.STARTING_ERROR.equals( woin.getStatus() ) ){
            markNewAfterStartingError( refNum );
        }
        else if( WorkflowInstanceStatus.ABORTING_ERROR.equals( woin.getStatus() ) ){
            markAbortAfterAbortingError( refNum );
        }
        else if( WorkflowInstanceStatus.EXECUTING_ERROR.equals( woin.getStatus() ) ){
            markExecutingAfterExecutingError( refNum );
        }
        else{
            throw new UnexpectedStatusException( Arrays.asList(
                    WorkflowInstanceStatus.STARTING_ERROR,
                    WorkflowInstanceStatus.ABORTING_ERROR,
                    WorkflowInstanceStatus.EXECUTING_ERROR ) );
        }
    }

    private void markNewAfterStartingError( long refNum ) throws UnexpectedStatusException{
        updateStatus( refNum, WorkflowInstanceStatus.NEW, WorkflowInstanceStatus.STARTING_ERROR );
    }

    private void markExecutingAfterExecutingError( long refNum ) throws UnexpectedStatusException{
        updateStatus( refNum, WorkflowInstanceStatus.EXECUTING, WorkflowInstanceStatus.EXECUTING_ERROR );
    }

    private void markAbortAfterAbortingError( long refNum ) throws UnexpectedStatusException{
        updateStatus( refNum, WorkflowInstanceStatus.ABORT, WorkflowInstanceStatus.ABORTING_ERROR );
    }

    @Override
    public void suspend( long refNum ) throws UnexpectedStatusException{
        updateStatus( refNum, WorkflowInstanceStatus.SUSPENDED, WorkflowInstanceStatus.EXECUTING );
    }

    @Override
    public void resume( long refNum ) throws UnexpectedStatusException{
        updateStatus( refNum, WorkflowInstanceStatus.EXECUTING, WorkflowInstanceStatus.SUSPENDED );
    }

    @Override
    public void handleStartingError( long woinRefNum, Exception exception ) throws UnexpectedStatusException{
        executionErrorService.handleError( woinRefNum, null, exception );
        Collection<WorkflowInstanceStatus> expectedStatuses = Arrays.asList(
                WorkflowInstanceStatus.NEW,
                WorkflowInstanceStatus.STARTING,
                WorkflowInstanceStatus.EXECUTING,
                WorkflowInstanceStatus.EXECUTED );
        updateStatus( woinRefNum, WorkflowInstanceStatus.STARTING_ERROR, expectedStatuses );
    }

    @Override
    public void handleAbortingError( long woinRefNum, Exception exception ) throws UnexpectedStatusException{
        executionErrorService.handleError( woinRefNum, null, exception );
        Collection<WorkflowInstanceStatus> expectedStatuses = Arrays.asList(
                WorkflowInstanceStatus.ABORT,
                WorkflowInstanceStatus.ABORTING,
                WorkflowInstanceStatus.ABORTED );
        updateStatus( woinRefNum, WorkflowInstanceStatus.ABORTING_ERROR, expectedStatuses );
    }

    @Override
    public void handleCompleteError( long woinRefNum, Long woitRefNum, Exception exception ) throws UnexpectedStatusException{
        executionErrorService.handleError( woinRefNum, woitRefNum, exception );
        updateStatus( woinRefNum, WorkflowInstanceStatus.EXECUTING_ERROR, WorkflowInstanceStatus.EXECUTING );
    }

    @Override
    public void lock( List<Long> refNums ){
        dao.updateLock( refNums, true );
    }

    @Override
    public void unlock( long refNum ){
        dao.updateLockAndNodeName( refNum, false, null );
    }

    @Override
    public void updateNodeName( long refNum, String nodeName ){
        dao.updateNodeName( refNum, nodeName );
    }

    @Override
    public void updateState( long refNum, String state ){
        WorkflowInstanceStatus expectedStatus = WorkflowInstanceStatus.ABORTING;
        boolean updateFailed = !dao.updateState( refNum, state, expectedStatus );
        if( updateFailed ){
            throw new UnexpectedStatusException( expectedStatus );
        }
    }

    @Override
    public void updateHistory( Long refNum, String history ){
        WorkflowInstanceStatus expectedStatus = WorkflowInstanceStatus.ABORTING;
        boolean updateFailed = !dao.updateHistory( refNum, history, expectedStatus );
        if( updateFailed ){
            throw new UnexpectedStatusException( expectedStatus );
        }
    }

    @Override
    public void recoverNotAssigned( String clusterName ){
        int count = dao.recoverNotAssigned( clusterName );
        log.info( "Recovered {} locked workflow instances not assigned to a node name for cluster {}", count, clusterName );
    }

    @Override
    public void recoverNew( String nodeName ){
        int count = dao.recover( nodeName, WorkflowInstanceStatus.NEW, WorkflowInstanceStatus.NEW );
        log.info( "Recovered {} new workflow instances for node {}", count, nodeName );
    }

    @Override
    public void recoverStarting( String nodeName ){
        int count = dao.recover( nodeName, WorkflowInstanceStatus.STARTING, WorkflowInstanceStatus.NEW );
        log.info( "Recovered {} starting workflow instances for node {}", count, nodeName );
    }

    @Override
    public void recoverExecuting( String nodeName ){
        int count = dao.recover( nodeName, WorkflowInstanceStatus.EXECUTING, WorkflowInstanceStatus.EXECUTING );
        log.info( "Recovered {} executing workflow instances for node {}", count, nodeName );
    }

    @Override
    public void recoverAbort( String nodeName ){
        int count = dao.recover( nodeName, WorkflowInstanceStatus.ABORT, WorkflowInstanceStatus.ABORT );
        log.info( "Recovered {} aborting workflow instances for node {}", count, nodeName );
    }

    @Override
    public void recoverAborting( String nodeName ){
        int count = dao.recover( nodeName, WorkflowInstanceStatus.ABORTING, WorkflowInstanceStatus.ABORT );
        log.info( "Recovered {} abort workflow instances for node {}", count, nodeName );
    }

    private void updateStatus( long refNum, WorkflowInstanceStatus newStatus,
                               WorkflowInstanceStatus expectedStatus ) throws UnexpectedStatusException{
        updateStatus( refNum, newStatus, Collections.singleton( expectedStatus ) );
    }

    private void updateStatus( long refNum, WorkflowInstanceStatus newStatus,
                               Collection<WorkflowInstanceStatus> expectedStatuses ) throws UnexpectedStatusException{
        boolean updateFailed = !dao.updateStatus( refNum, newStatus, expectedStatuses );
        if( updateFailed ){
            throw new UnexpectedStatusException( expectedStatuses );
        }
        else{
            log.info( "Updated the status of workflow instance {} to {}", refNum, newStatus );
        }
    }

}
