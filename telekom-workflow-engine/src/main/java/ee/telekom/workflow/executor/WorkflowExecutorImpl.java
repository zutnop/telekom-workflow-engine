package ee.telekom.workflow.executor;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import ee.telekom.workflow.core.archive.ArchiveService;
import ee.telekom.workflow.core.common.UnexpectedStatusException;
import ee.telekom.workflow.core.notification.ExceptionNotificationService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.core.workitem.WorkItemService;
import ee.telekom.workflow.core.workitem.WorkItemType;
import ee.telekom.workflow.executor.marshall.GraphInstanceRepository;
import ee.telekom.workflow.executor.marshall.Marshaller;
import ee.telekom.workflow.executor.marshall.TokenState;
import ee.telekom.workflow.executor.plugin.WorkflowEnginePlugin;
import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.WorkflowException;
import ee.telekom.workflow.util.CallUtil;

@Component
public class WorkflowExecutorImpl implements WorkflowExecutor{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private WorkItemService workItemService;
    @Autowired
    private ArchiveService archiveService;
    @Autowired
    private GraphInstanceRepository graphInstanceRepository;
    @Autowired
    private GraphEngineFactory engineFactory;
    @Autowired
    private WorkflowEnginePlugin plugin;
    @Autowired
    private PlatformTransactionManager platformTransactionManager;
    @Autowired
    private ExceptionNotificationService exceptionNotificationService;

    /*
     * A few words on the treatment of UnexpectedStatusException's.
     * 
     * In general, these exception should only occur as consequence of a
     * concurrent status transition. A concurrent status transition may be
     * caused by a user requesting a process to abort. For that reason, the
     * given database changes are rolled rolled back and the process execution
     * is unlocked (so that it may be polled for the aborting logic). No entry
     * is added to the error logging tables.
     */

    @Override
    public void startWorkflow( long woinRefNum ){
        log.debug( "Starting" );

        TransactionStatus status = null;
        try{
            workflowInstanceService.markStarting( woinRefNum );
            status = begin();

            WorkflowInstance woin = workflowInstanceService.find( woinRefNum );
            Environment env = Marshaller.deserializeEnv( woin.getAttributes() );
            GraphInstance graphInstance = engineFactory.getSingletonInstance().start( woin.getWorkflowName(), woin.getWorkflowVersion(), env, woinRefNum );
            graphInstanceRepository.save( graphInstance, WorkflowInstanceStatus.EXECUTED );

            commit( status, "Started" );
        }
        catch( UnexpectedStatusException e ){
            // See above for a reasoning on why these exceptions are caught separately.
            log.warn( e.getMessage() );
            if( status != null ){
                rollback( status );
            }
            workflowInstanceService.unlock( woinRefNum );
            log.warn( "Unlocked", e );
        }
        catch( Exception e ){
            log.warn( "Handling error", e );
            rollback( status );
            try{
                workflowInstanceService.handleStartingError( woinRefNum, e );
            }
            catch( Exception e2 ){
                log.error( "Handling error failed.", e2 );
            }
            exceptionNotificationService.handleException( e );
        }
    }

    @Override
    public void abortWorkflow( long woinRefNum ){
        log.debug( "Aborting" );

        TransactionStatus status = null;
        try{
            workflowInstanceService.markAborting( woinRefNum );
            status = begin();
            WorkflowInstance woin = workflowInstanceService.find( woinRefNum );

            if( woin.getState() == null ){
                // Abort a workflow instance that has not started previously (at least not successfully) 
                woin.setHistory( "abort|aborted|" );
                workflowInstanceService.updateHistory( woin.getRefNum(), woin.getHistory() );
                workflowInstanceService.markAborted( woin.getRefNum() );
                workflowInstanceService.unlock( woin.getRefNum() );
                archiveService.archive( woin.getRefNum(), -1 );
            }
            else if( engineFactory.getGraph( woin.getWorkflowName(), woin.getWorkflowVersion() ) != null ){
                // Abort a workflow instance that has been started and which is associated to an existing graph 
                GraphInstance graphInstance = graphInstanceRepository.load( woin.getRefNum() );
                engineFactory.getSingletonInstance().abort( graphInstance );
                graphInstanceRepository.save( graphInstance, WorkflowInstanceStatus.ABORTED );
            }
            else{
                // Abort a workflow instance that has been started and which is associated to a graph that is no longer
                // existing (e.g. because the particular graph version has been removed)
                List<WorkItem> woits = workItemService.findActiveByWoinRefNum( woin.getRefNum() );
                for( WorkItem woit : woits ){
                    if( WorkItemType.HUMAN_TASK.equals( woit.getType() ) ){
                        plugin.onHumanTaskCancelled( woin, woit );
                    }
                    workItemService.markCancelled( woit.getRefNum() );
                }
                Collection<TokenState> tokenStates = Marshaller.deserializeTokenStates( woin.getState() );
                for( TokenState tokenState : tokenStates ){
                    tokenState.setActive( false );
                }
                woin.setState( Marshaller.serializeTokenStates( tokenStates ) );
                workflowInstanceService.updateState( woin.getRefNum(), woin.getState() );
                woin.setHistory( woin.getHistory() + "abort|aborted|" );
                workflowInstanceService.updateHistory( woin.getRefNum(), woin.getHistory() );
                workflowInstanceService.markAborted( woin.getRefNum() );
                workflowInstanceService.unlock( woin.getRefNum() );
                archiveService.archive( woin.getRefNum(), -1 );
            }

            commit( status, "Aborted" );
        }
        catch( Exception e ){
            log.warn( "Handling error", e );
            rollback( status );
            try{
                workflowInstanceService.handleAbortingError( woinRefNum, e );
            }
            catch( Exception e2 ){
                log.error( "Handling error failed.", e2 );
            }
            exceptionNotificationService.handleException( e );
        }
    }

    @Override
    public void completeWorkItem( long woinRefNum, long woitRefNum ){
        log.debug( "Completing" );

        TransactionStatus status = null;
        try{
            workflowInstanceService.assertIsExecuting(woinRefNum);
            workItemService.markCompleting( woitRefNum );
            status = begin();

            GraphInstance graphInstance = graphInstanceRepository.load( woinRefNum );
            GraphWorkItem graphWorkItem = findGraphWorkItem( graphInstance, woitRefNum );
            engineFactory.getSingletonInstance().complete( graphWorkItem );
            graphInstanceRepository.save( graphInstance, WorkflowInstanceStatus.EXECUTED );

            commit( status, "Completed" );
        }
        catch( UnexpectedStatusException e ){
            // See above for a reasoning on why these exceptions are caught
            // separately.
            log.warn( e.getMessage() );
            if( status != null ){
                rollback( status );
            }
            workflowInstanceService.unlock( woinRefNum );
            log.warn( "Unlocked", e );
        }
        catch( Exception e ){
            log.warn( "Handling error", e );
            rollback( status );
            try{
                workItemService.handleCompletingError( woinRefNum, woitRefNum, e );
            }
            catch( Exception e2 ){
                log.error( "Handling error failed.", e2 );
            }
            exceptionNotificationService.handleException( e );
        }
    }

    @Override
    public void executeTask( long woinRefNum, long woitRefNum ){
        log.debug( "Executing" );

        TransactionStatus status = null;
        try{
            workflowInstanceService.assertIsExecuting(woinRefNum);
            workItemService.markExecuting( woitRefNum );
            status = begin();

            WorkItem woit = workItemService.find( woitRefNum );
            Object target = engineFactory.getSingletonInstance().getBeanResolver().getBean( woit.getBean() );
            Object[] arguments = Marshaller.deserializeTaskArguments( woit.getArguments() );
            Object returnValue = CallUtil.call( target, woit.getMethod(), arguments );
            String result = Marshaller.serializeResult( returnValue );

            workItemService.markExecutedAndSaveResult( woitRefNum, result );
            workflowInstanceService.unlock( woinRefNum );
            commit( status, "Executed" );
        }
        catch( UnexpectedStatusException e ){
            log.warn( e.getMessage() );
            if( status != null ){
                rollback( status );
            }
            workflowInstanceService.unlock( woinRefNum );
            log.warn( "Unlocked", e );
        }
        catch( Exception e ){
            log.warn( "Handling error", e );
            rollback( status );
            try{
                workItemService.handleExecutingError( woinRefNum, woitRefNum, e );
            }
            catch( Exception e2 ){
                log.error( "Handling error failed.", e2 );
            }
            exceptionNotificationService.handleException( e );
        }
    }

    private TransactionStatus begin(){
        TransactionDefinition definition = new DefaultTransactionDefinition( TransactionDefinition.PROPAGATION_REQUIRES_NEW );
        return platformTransactionManager.getTransaction( definition );
    }

    private void commit( TransactionStatus status, String logMessage ){
        platformTransactionManager.commit( status );
        log.info( logMessage );
    }

    private void rollback( TransactionStatus status ){
        if( status != null ){
            log.info( "Trying to roll back" );
            try{
                platformTransactionManager.rollback(status);
                log.info( "Rolled back" );
            }
            catch( Exception e ){
                log.error( "Failed to roll back transaction", e );
            }
        }
        else{
            log.warn( "Cannot roll back, because transaction status is null" );
        }
    }

    private GraphWorkItem findGraphWorkItem( GraphInstance graphInstance, long externalId ){
        for( GraphWorkItem graphWorkItem : graphInstance.getWorkItems() ){
            if( graphWorkItem.getExternalId() == externalId ){
                return graphWorkItem;
            }
        }
        throw new WorkflowException( "Unknown work item with external id " + externalId );
    }
}
