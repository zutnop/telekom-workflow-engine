package ee.telekom.workflow.executor;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ee.telekom.workflow.TestApplicationContexts;
import ee.telekom.workflow.core.abort.AbortService;
import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.node.NodeService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.core.workunit.WorkType;
import ee.telekom.workflow.executor.marshall.Marshaller;
import ee.telekom.workflow.executor.marshall.TokenState;
import ee.telekom.workflow.executor.plugin.MockPlugin;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphRepository;
import ee.telekom.workflow.graph.WorkItemStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = TestApplicationContexts.DEFAULT)
@DirtiesContext
public class AbortIT extends AbstractWorkflowIT{

    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private AbortService abortService;

    @Autowired
    private GraphEngineFactory engineFactory;
    @Autowired
    private MockPlugin mockPlugin;
    @Autowired
    private WorkflowExecutor executor;

    @Autowired
    private NodeService nodeService;
    @Autowired
    private WorkflowEngineConfiguration config;

    @Test
    public void abort_before_start(){
        Graph graph = GraphFactory.INSTANCE.human_task_one_pre_post();
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();
        Integer version = graph.getVersion();

        long woinRefNum = workflowInstanceService.create( name, version, INITIAL_ENV, LABEL1, LABEL2 ).getRefNum();
        assertWoin( woinRefNum, name, version, true, WorkflowInstanceStatus.NEW );

        abortService.abort( woinRefNum );
        assertWoin( woinRefNum, name, version, true, WorkflowInstanceStatus.ABORT );

        simulatePollLockAssign( woinRefNum, WorkType.ABORT_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, true, WorkflowInstanceStatus.ABORT );

        executor.abortWorkflow( woinRefNum );
        assertWoin( woinRefNum, name, version, true, WorkflowInstanceStatus.ABORTED );
        assertActiveWoitsCount( woinRefNum, 0 );
        assertHumanTaskListeners( 0, 0, 0, 0 );
    }

    @Test
    public void abort_with_existing_graph_and_humantask(){
        Graph graph = GraphFactory.INSTANCE.human_task_one_pre_post();
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();
        Integer version = graph.getVersion();

        long woinRefNum = workflowInstanceService.create( name, version, INITIAL_ENV, LABEL1, LABEL2 ).getRefNum();
        assertWoin( woinRefNum, name, version, true, WorkflowInstanceStatus.NEW );

        simulatePollLockAssign( woinRefNum, WorkType.START_WORKFLOW, null );

        executor.startWorkflow( woinRefNum );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.EXECUTING );
        assertActiveWoitsCount( woinRefNum, 1 );
        long woitRefNum = getActiveWoitRefNum( woinRefNum, 1 );
        assertHumanTaskWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, ROLE, USER, HUMAN_TASK_ARGUMENTS, null );
        assertHumanTaskListeners( 1, 0, 0, 0 );

        submitHumanTask( woitRefNum, HUMAN_TASK_RESULT );
        assertHumanTaskWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.EXECUTED, ROLE, USER, HUMAN_TASK_ARGUMENTS, HUMAN_TASK_RESULT );

        abortService.abort( woinRefNum );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.ABORT );
        assertHumanTaskWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.EXECUTED, ROLE, USER, HUMAN_TASK_ARGUMENTS, HUMAN_TASK_RESULT );

        simulatePollLockAssign( woinRefNum, WorkType.ABORT_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.ABORT );

        executor.abortWorkflow( woinRefNum );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.ABORTED );
        assertActiveWoitsCount( woinRefNum, 0 );
        assertHumanTaskWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.CANCELLED, ROLE, USER, HUMAN_TASK_ARGUMENTS, HUMAN_TASK_RESULT );
        assertHumanTaskListeners( 1, 0, 1, 0 );
    }

    @Test
    public void abort_with_existing_graph_and_signal(){
        Graph graph = GraphFactory.INSTANCE.signal_one_pre_post();
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();
        Integer version = graph.getVersion();

        long woinRefNum = workflowInstanceService.create( name, version, INITIAL_ENV, LABEL1, LABEL2 ).getRefNum();
        assertWoin( woinRefNum, name, version, true, WorkflowInstanceStatus.NEW );

        simulatePollLockAssign( woinRefNum, WorkType.START_WORKFLOW, null );

        executor.startWorkflow( woinRefNum );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.EXECUTING );
        assertActiveWoitsCount( woinRefNum, 1 );
        long woitRefNum = getActiveWoitRefNum( woinRefNum, 1 );
        assertSignalWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, SIGNAL, null );
        assertHumanTaskListeners( 0, 0, 0, 0 );

        abortService.abort( woinRefNum );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.ABORT );
        assertSignalWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, SIGNAL, null );

        simulatePollLockAssign( woinRefNum, WorkType.ABORT_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.ABORT );

        executor.abortWorkflow( woinRefNum );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.ABORTED );
        assertActiveWoitsCount( woinRefNum, 0 );
        assertSignalWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.CANCELLED, SIGNAL, null );
        assertHumanTaskListeners( 0, 0, 0, 0 );
    }

    @Test
    public void abort_with_not_existing_graph_and_humantask(){
        Graph graph = GraphFactory.INSTANCE.human_task_one_pre_post();
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();
        Integer version = graph.getVersion();

        long woinRefNum = workflowInstanceService.create( name, version, INITIAL_ENV, LABEL1, LABEL2 ).getRefNum();
        assertWoin( woinRefNum, name, version, true, WorkflowInstanceStatus.NEW );

        simulatePollLockAssign( woinRefNum, WorkType.START_WORKFLOW, null );

        executor.startWorkflow( woinRefNum );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.EXECUTING );
        assertActiveWoitsCount( woinRefNum, 1 );
        long woitRefNum = getActiveWoitRefNum( woinRefNum, 1 );
        assertHumanTaskWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, ROLE, USER, HUMAN_TASK_ARGUMENTS, null );
        assertHumanTaskListeners( 1, 0, 0, 0 );

        submitHumanTask( woitRefNum, HUMAN_TASK_RESULT );
        assertHumanTaskWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.EXECUTED, ROLE, USER, HUMAN_TASK_ARGUMENTS, HUMAN_TASK_RESULT );

        mockPlugin.clearRepository();

        abortService.abort( woinRefNum );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.ABORT );
        assertHumanTaskWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.EXECUTED, ROLE, USER, HUMAN_TASK_ARGUMENTS, HUMAN_TASK_RESULT );

        simulatePollLockAssign( woinRefNum, WorkType.ABORT_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.ABORT );

        executor.abortWorkflow( woinRefNum );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.ABORTED );
        assertActiveWoitsCount( woinRefNum, 0 );
        assertHumanTaskWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.CANCELLED, ROLE, USER, HUMAN_TASK_ARGUMENTS, HUMAN_TASK_RESULT );
        assertHumanTaskListeners( 1, 0, 0, 1 );
    }

    @Test
    public void abort_with_not_existing_graph_and_signal(){
        Graph graph = GraphFactory.INSTANCE.signal_one_pre_post();
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();
        Integer version = graph.getVersion();

        long woinRefNum = workflowInstanceService.create( name, version, INITIAL_ENV, LABEL1, LABEL2 ).getRefNum();
        assertWoin( woinRefNum, name, version, true, WorkflowInstanceStatus.NEW );

        simulatePollLockAssign( woinRefNum, WorkType.START_WORKFLOW, null );

        executor.startWorkflow( woinRefNum );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.EXECUTING );
        assertActiveWoitsCount( woinRefNum, 1 );
        long woitRefNum = getActiveWoitRefNum( woinRefNum, 1 );
        assertSignalWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, SIGNAL, null );
        assertHumanTaskListeners( 0, 0, 0, 0 );

        mockPlugin.clearRepository();

        abortService.abort( woinRefNum );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.ABORT );
        assertSignalWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, SIGNAL, null );

        simulatePollLockAssign( woinRefNum, WorkType.ABORT_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.ABORT );

        executor.abortWorkflow( woinRefNum );
        assertWoin( woinRefNum, name, version, false, WorkflowInstanceStatus.ABORTED );
        assertActiveWoitsCount( woinRefNum, 0 );
        assertSignalWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.CANCELLED, SIGNAL, null );
        assertHumanTaskListeners( 0, 0, 0, 0 );
    }

    protected void assertWoin( long refNum,
                               String name,
                               Integer version,
                               boolean stateNull,
                               WorkflowInstanceStatus status ){
        WorkflowInstance woin;
        if( WorkflowInstanceStatus.EXECUTED.equals( status )
                || WorkflowInstanceStatus.ABORTED.equals( status ) ){
            woin = archiveDao.findWoinByRefNum( refNum );
            if( woin == null || workflowInstanceDao.findByRefNum( refNum ) != null ){
                Assert.fail( "Workflow instance not archived" );
            }
        }
        else{
            woin = workflowInstanceDao.findByRefNum( refNum );
            if( woin == null || archiveDao.findWoinByRefNum( refNum ) != null ){
                Assert.fail( "Workflow instance archived!" );
            }
        }
        Assert.assertEquals( refNum, (long)woin.getRefNum() );
        Assert.assertEquals( name, woin.getWorkflowName() );
        Assert.assertEquals( version, woin.getWorkflowVersion() );
        Assert.assertEquals( stateNull, woin.getState() == null );
        Assert.assertEquals( status, woin.getStatus() );
        if( WorkflowInstanceStatus.ABORTED.equals( status ) || WorkflowInstanceStatus.EXECUTED.equals( status ) ){
            Collection<TokenState> tokenStates = Marshaller.deserializeTokenStates( woin.getState() );
            if( tokenStates != null ){
                for( TokenState tokenState : tokenStates ){
                    Assert.assertFalse( tokenState.isActive() );
                }
            }
        }
    }

    public void assertHumanTaskListeners( int created, int completed, int cancelled, int cancelledWithoutGraph ){
        Assert.assertEquals( created, mockPlugin.getHtCreated() );
        Assert.assertEquals( completed, mockPlugin.getHtCompleted() );
        Assert.assertEquals( cancelled, mockPlugin.getHtCancelled() );
        Assert.assertEquals( cancelledWithoutGraph, mockPlugin.getHtCancelledWithoutGraph() );
    }

    @Override
    @Before
    public void prepareTest(){
        nodeService.findOrCreateByName( config.getNodeName() );
        mockPlugin.resetCounters();
    }

}
