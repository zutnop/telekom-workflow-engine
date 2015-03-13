package ee.telekom.workflow.executor.marshall;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.core.archive.ArchiveDao;
import ee.telekom.workflow.core.common.UnexpectedStatusException;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceDao;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.core.workitem.WorkItemDao;
import ee.telekom.workflow.executor.GraphEngineFactory;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.WorkItemStatus;

@Repository
@Transactional
public class GraphInstanceRepositoryImpl implements GraphInstanceRepository{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    //NB! This class does not use service layer methods as it comprises a repository (located between
    //    the service and DAO layer).
    @Autowired
    private WorkflowInstanceDao woinDao;
    @Autowired
    private WorkItemDao woitDao;
    @Autowired
    private ArchiveDao archiveDao;

    @Autowired
    private GraphEngineFactory engineFactory;

    @Override
    public GraphInstance load( long woinRefNum ){
        WorkflowInstance workflowInstance = woinDao.findByRefNum( woinRefNum );
        List<WorkItem> workItems = woitDao.findActiveByWoinRefNum( woinRefNum );
        Graph graph = engineFactory.getGraph( workflowInstance.getWorkflowName(), workflowInstance.getWorkflowVersion() );
        return Marshaller.unmarshall( workflowInstance, workItems, graph );
    }

    @Override
    public void save( GraphInstance graphInstance, WorkflowInstanceStatus completeStatus ){
        WorkflowInstance workflowInstance = new WorkflowInstance();
        List<WorkItem> workItems = new LinkedList<>();
        Marshaller.marshall( graphInstance, workflowInstance, workItems, completeStatus );

        List<Long> markCancelled = new LinkedList<>();
        Long markCompleted = null;
        List<WorkItem> createNew = new LinkedList<>();

        for( WorkItem woit : workItems ){
            if( woit.getRefNum() != null ){
                if( WorkItemStatus.CANCELLED.equals( woit.getStatus() ) ){
                    markCancelled.add( woit.getRefNum() );
                }
                else if( WorkItemStatus.COMPLETED.equals( woit.getStatus() ) ){
                    markCompleted = woit.getRefNum();
                }
            }
            else{
                createNew.add( woit );
            }
        }
        if( !markCancelled.isEmpty() ){
            Collection<WorkItemStatus> expectedStatuses = Arrays.asList( WorkItemStatus.NEW, WorkItemStatus.EXECUTED );
            boolean sucess = woitDao.updateStatus( markCancelled, WorkItemStatus.CANCELLED, expectedStatuses );
            if( !sucess ){
                throw new UnexpectedStatusException( expectedStatuses );
            }
            if( log.isInfoEnabled() ){
                log.info( "Cancelled work items {} ", StringUtils.join( markCancelled, "," ) );
            }
        }
        if( markCompleted != null ){
            Collection<WorkItemStatus> expectedStatuses = Arrays.asList( WorkItemStatus.COMPLETING );
            boolean sucess = woitDao.updateStatus( markCompleted, WorkItemStatus.COMPLETED, expectedStatuses );
            if( !sucess ){
                throw new UnexpectedStatusException( expectedStatuses );
            }
            log.info( "Completed work item {} ", markCompleted );
        }
        if( !createNew.isEmpty() ){
            woitDao.create( createNew );
            if( log.isInfoEnabled() ){
                log.info( "Created new work items {} ", StringUtils.join( getRefNums( createNew ), "," ) );
            }
        }
        List<WorkflowInstanceStatus> expectedStatuses;
        if( WorkflowInstanceStatus.EXECUTED.equals( workflowInstance.getStatus() )
                || WorkflowInstanceStatus.ABORTED.equals( workflowInstance.getStatus() ) ){
            // A workflow instance that got completed should be marked completed in the database, even if there was a concurrent ABORT or SUSPEND request.
            expectedStatuses = Arrays.asList( WorkflowInstanceStatus.STARTING, WorkflowInstanceStatus.EXECUTING, WorkflowInstanceStatus.ABORTING,
                    WorkflowInstanceStatus.SUSPENDED, WorkflowInstanceStatus.ABORT );
        }
        else{
            expectedStatuses = Arrays.asList( WorkflowInstanceStatus.STARTING, WorkflowInstanceStatus.EXECUTING, WorkflowInstanceStatus.ABORTING );
        }

        woinDao.updateAndUnlock(
                workflowInstance.getRefNum(),
                workflowInstance.getWorkflowVersion(),
                workflowInstance.getAttributes(),
                workflowInstance.getHistory(),
                workflowInstance.getState(),
                workflowInstance.getStatus(),
                expectedStatuses
                );
        log.info( "Updated workflow instance {} with status {} ", workflowInstance.getRefNum(), workflowInstance.getStatus() );
        if( WorkflowInstanceStatus.EXECUTED.equals( workflowInstance.getStatus() )
                || WorkflowInstanceStatus.ABORTED.equals( workflowInstance.getStatus() ) ){
            archiveDao.archive( workflowInstance.getRefNum() );
            log.info( "Archived workflow instance {}", workflowInstance.getRefNum() );
        }
    }

    private List<Long> getRefNums( List<WorkItem> workItems ){
        List<Long> result = new ArrayList<Long>( workItems.size() );
        for( WorkItem wi : workItems ){
            result.add( wi.getRefNum() );
        }
        return result;
    }
}
