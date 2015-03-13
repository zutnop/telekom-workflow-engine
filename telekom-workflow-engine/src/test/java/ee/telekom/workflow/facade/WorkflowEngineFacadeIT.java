package ee.telekom.workflow.facade;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ee.telekom.workflow.TestApplicationContexts;
import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.node.NodeService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.executor.GraphEngineFactory;
import ee.telekom.workflow.executor.WorkflowExecutor;
import ee.telekom.workflow.facade.model.CreateWorkflowInstance;
import ee.telekom.workflow.facade.model.ExecutionErrorState;
import ee.telekom.workflow.facade.model.WorkItemState;
import ee.telekom.workflow.facade.model.WorkflowInstanceState;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.WorkItemStatus;
import ee.telekom.workflow.graph.core.GraphImpl;
import ee.telekom.workflow.graph.core.TransitionImpl;
import ee.telekom.workflow.graph.node.activity.BeanAsyncCallActivity;
import ee.telekom.workflow.graph.node.activity.HumanTaskActivity;
import ee.telekom.workflow.graph.node.event.CatchSignal;
import ee.telekom.workflow.graph.node.event.CatchTimer;
import ee.telekom.workflow.graph.node.gateway.AndFork;
import ee.telekom.workflow.graph.node.gateway.AndJoin;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = TestApplicationContexts.DEFAULT)
@DirtiesContext
public class WorkflowEngineFacadeIT extends TestApplicationContexts{

    @Autowired
    private WorkflowEngineFacade facade;

    @Autowired
    private WorkflowExecutor executor;
    @Autowired
    private GraphEngineFactory engineFactory;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private WorkflowEngineConfiguration config;

    private static final String NAME1 = "name1";
    private static final int VERSION1 = 1;
    private static final String NAME2 = "name2";
    private static final int VERSION2 = 5;
    private static final String UNKNOWN = "unknown";

    private static final String SIGNAL = "go";
    private static final int TIMER_MS = 600000;
    private static final String BEAN = "bean1";
    private static final String METHOD = "method1";
    private static final String ROLE = "role1";
    private static final String USER = "user1";

    private static final Graph GRAPH1 = createTestGraph( NAME1, VERSION1 );
    private static final Graph GRAPH2 = createTestGraph( NAME2, VERSION2 );

    @Test
    @DirtiesContext
    public void testDeployedAndKnownWorkflowNames(){
        engineFactory.getSingletonInstance().getRepository().addGraph( GRAPH1 );
        engineFactory.getSingletonInstance().getRepository().addGraph( GRAPH2 );
        facade.createWorkflowInstance( createRequest( UNKNOWN, null, null, null ) );

        Set<String> deployedWorkflowNames = facade.getDeployedWorkflowNames();
        Assert.assertEquals( 2, deployedWorkflowNames.size() );
        Assert.assertTrue( deployedWorkflowNames.contains( NAME1 ) );
        Assert.assertTrue( deployedWorkflowNames.contains( NAME2 ) );

        Set<String> knownWorkflowNames = facade.getKnownWorkflowNames();
        Assert.assertEquals( 3, knownWorkflowNames.size() );
        Assert.assertTrue( knownWorkflowNames.contains( NAME1 ) );
        Assert.assertTrue( knownWorkflowNames.contains( NAME2 ) );
        Assert.assertTrue( knownWorkflowNames.contains( UNKNOWN ) );
    }

    @Test
    @DirtiesContext
    public void manageWorkflowIntstance(){
        engineFactory.getSingletonInstance().getRepository().addGraph( GRAPH1 );

        // test createWorkflowInstance
        CreateWorkflowInstance request = createRequest( NAME1, null, "label1", "label2" );
        facade.createWorkflowInstance( request );
        Assert.assertEquals( 1, (long)request.getRefNum() );

        // test findWorkflowInstance
        WorkflowInstanceState woin = facade.findWorkflowInstance( 1, null );
        assertInstance( woin, 1, NAME1, null, "label1", "label2", WorkflowInstanceStatus.NEW );

        woin = facade.findWorkflowInstance( 1, true );
        assertInstance( woin, 1, NAME1, null, "label1", "label2", WorkflowInstanceStatus.NEW );

        woin = facade.findWorkflowInstance( 1, false );
        Assert.assertNull( woin );

        // test suspendWorkflowInstance
        executor.startWorkflow( 1 );
        woin = facade.findWorkflowInstance( 1, null );
        assertInstance( woin, 1, NAME1, VERSION1, "label1", "label2", WorkflowInstanceStatus.EXECUTING );

        facade.suspendWorkflowInstance( 1 );
        woin = facade.findWorkflowInstance( 1, true );
        assertInstance( woin, 1, NAME1, VERSION1, "label1", "label2", WorkflowInstanceStatus.SUSPENDED );

        // test resumeWorkflowInstance
        facade.resumeWorkflowInstance( 1 );
        woin = facade.findWorkflowInstance( 1, true );
        assertInstance( woin, 1, NAME1, VERSION1, "label1", "label2", WorkflowInstanceStatus.EXECUTING );

        // test findWorkItems
        List<WorkItemState> woits = facade.findWorkItems( 1, true );
        Assert.assertEquals( 4, woits.size() );
        assertSignal( woits.get( 3 ), 1, 1, 2, WorkItemStatus.NEW, SIGNAL );
        assertTimer( woits.get( 2 ), 2, 1, 3, WorkItemStatus.NEW, false );
        assertTask( woits.get( 1 ), 3, 1, 4, WorkItemStatus.NEW, BEAN, METHOD );
        assertHumanTask( woits.get( 0 ), 4, 1, 5, WorkItemStatus.NEW, ROLE, null );

        // test findWorkItem
        WorkItemState woit1 = facade.findWorkItem( 1, true );
        assertSignal( woit1, 1, 1, 2, WorkItemStatus.NEW, SIGNAL );
        WorkItemState woit2 = facade.findWorkItem( 2, true );
        assertTimer( woit2, 2, 1, 3, WorkItemStatus.NEW, false );
        WorkItemState woit3 = facade.findWorkItem( 3, true );
        assertTask( woit3, 3, 1, 4, WorkItemStatus.NEW, BEAN, METHOD );
        WorkItemState woit4 = facade.findWorkItem( 4, true );
        assertHumanTask( woit4, 4, 1, 5, WorkItemStatus.NEW, ROLE, null );

        // test findActiveWorkItemByTokenId
        woit1 = facade.findActiveWorkItemByTokenId( 1, 2 );
        assertSignal( woit1, 1, 1, 2, WorkItemStatus.NEW, SIGNAL );
        woit2 = facade.findActiveWorkItemByTokenId( 1, 3 );
        assertTimer( woit2, 2, 1, 3, WorkItemStatus.NEW, false );
        woit3 = facade.findActiveWorkItemByTokenId( 1, 4 );
        assertTask( woit3, 3, 1, 4, WorkItemStatus.NEW, BEAN, METHOD );
        woit4 = facade.findActiveWorkItemByTokenId( 1, 5 );
        assertHumanTask( woit4, 4, 1, 5, WorkItemStatus.NEW, ROLE, null );

        // test findActiveHumanTasksByRoleAndUser
        List<WorkItemState> humanTasks = facade.findActiveHumanTasksByRoleAndUser( ROLE, null );
        Assert.assertEquals( 1, humanTasks.size() );
        assertHumanTask( humanTasks.get( 0 ), 4, 1, 5, WorkItemStatus.NEW, ROLE, null );

        // test findActiveHumanTasksByRole
        humanTasks = facade.findActiveHumanTasksByRole( ROLE );
        Assert.assertEquals( 1, humanTasks.size() );
        assertHumanTask( humanTasks.get( 0 ), 4, 1, 5, WorkItemStatus.NEW, ROLE, null );

        // test findActiveHumanTasksByUser
        humanTasks = facade.findActiveHumanTasksByUser( USER );
        Assert.assertEquals( 0, humanTasks.size() );

        // test sendSignalToWorkflowInstance()
        facade.sendSignalToWorkflowInstance( 1, SIGNAL, null );
        woit1 = facade.findWorkItem( 1, null );
        assertSignal( woit1, 1, 1, 2, WorkItemStatus.EXECUTED, SIGNAL );

        // test skipTimer()
        facade.skipTimer( 2 );
        woit2 = facade.findWorkItem( 2, null );
        assertTimer( woit2, 2, 1, 3, WorkItemStatus.NEW, true );

        // test findExecutionError
        executor.executeTask( 1, 3 );
        ExecutionErrorState error = facade.findExecutionError( 1 );
        Assert.assertNotNull( error );
        Assert.assertEquals( 1, (long)error.getWoinRefNum() );
        Assert.assertEquals( 3, (long)error.getWoitRefNum() );
        woin = facade.findWorkflowInstance( 1, true );
        assertInstance( woin, 1, NAME1, VERSION1, "label1", "label2", WorkflowInstanceStatus.EXECUTING_ERROR );

        // test retryWorkflowInstance
        facade.retryWorkflowInstance( 1 );
        error = facade.findExecutionError( 1 );
        Assert.assertNull( error );
        woin = facade.findWorkflowInstance( 1, true );
        assertInstance( woin, 1, NAME1, VERSION1, "label1", "label2", WorkflowInstanceStatus.EXECUTING );

        // test submitTask
        facade.submitTask( 3, null );
        woit3 = facade.findWorkItem( 3, null );
        assertTask( woit3, 3, 1, 4, WorkItemStatus.EXECUTED, BEAN, METHOD );

        // test assignHumanTask
        facade.assignHumanTask( 4, USER );
        woit4 = facade.findWorkItem( 4, null );
        assertHumanTask( woit4, 4, 1, 5, WorkItemStatus.NEW, ROLE, USER );

        facade.assignHumanTask( 4, null );
        woit4 = facade.findWorkItem( 4, null );
        assertHumanTask( woit4, 4, 1, 5, WorkItemStatus.NEW, ROLE, null );

        facade.assignHumanTask( 4, USER );

        // test again findActiveHumanTasksByRoleAndUser
        humanTasks = facade.findActiveHumanTasksByRoleAndUser( ROLE, USER );
        Assert.assertEquals( 1, humanTasks.size() );
        assertHumanTask( humanTasks.get( 0 ), 4, 1, 5, WorkItemStatus.NEW, ROLE, USER );

        // test again findActiveHumanTasksByRole
        humanTasks = facade.findActiveHumanTasksByRole( ROLE );
        Assert.assertEquals( 1, humanTasks.size() );
        assertHumanTask( humanTasks.get( 0 ), 4, 1, 5, WorkItemStatus.NEW, ROLE, USER );

        // test again findActiveHumanTasksByUser
        humanTasks = facade.findActiveHumanTasksByUser( USER );
        Assert.assertEquals( 1, humanTasks.size() );
        assertHumanTask( humanTasks.get( 0 ), 4, 1, 5, WorkItemStatus.NEW, ROLE, USER );

        // test submitHumanTask
        facade.submitHumanTask( 4, null );
        woit4 = facade.findWorkItem( 4, null );
        assertHumanTask( woit4, 4, 1, 5, WorkItemStatus.EXECUTED, ROLE, USER );

        humanTasks = facade.findActiveHumanTasksByRoleAndUser( ROLE, USER );
        Assert.assertEquals( 0, humanTasks.size() );

        humanTasks = facade.findActiveHumanTasksByRole( ROLE );
        Assert.assertEquals( 0, humanTasks.size() );

        humanTasks = facade.findActiveHumanTasksByUser( USER );
        Assert.assertEquals( 0, humanTasks.size() );

        // Testing abortWorkflowInstance
        facade.abortWorkflowInstance( 1 );
        woin = facade.findWorkflowInstance( 1, true );
        assertInstance( woin, 1, NAME1, VERSION1, "label1", "label2", WorkflowInstanceStatus.ABORT );
        executor.abortWorkflow( 1 );
        woin = facade.findWorkflowInstance( 1, false );
        assertInstance( woin, 1, NAME1, VERSION1, "label1", "label2", WorkflowInstanceStatus.ABORTED );

        // test again findActiveWorkItemByTokenId
        Assert.assertNull( facade.findActiveWorkItemByTokenId( 1, 2 ) );
        Assert.assertNull( facade.findActiveWorkItemByTokenId( 1, 3 ) );
        Assert.assertNull( facade.findActiveWorkItemByTokenId( 1, 4 ) );
        Assert.assertNull( facade.findActiveWorkItemByTokenId( 1, 5 ) );
    }

    @Test
    @DirtiesContext
    public void testSendSignals(){
        engineFactory.getSingletonInstance().getRepository().addGraph( GRAPH1 );
        engineFactory.getSingletonInstance().getRepository().addGraph( GRAPH2 );

        CreateWorkflowInstance request1 = createRequest( NAME1, null, "1", null );
        CreateWorkflowInstance request2 = createRequest( NAME1, VERSION1, "1", null );
        CreateWorkflowInstance request3 = createRequest( NAME2, null, "2", null );

        // test createWorkflowInstances
        facade.createWorkflowInstances( Arrays.asList( request1, request2, request3 ) );
        Assert.assertEquals( 1, (long)request1.getRefNum() );
        Assert.assertEquals( 2, (long)request2.getRefNum() );
        Assert.assertEquals( 3, (long)request3.getRefNum() );
        assertInstance( facade.findWorkflowInstance( 1, true ), 1, NAME1, null, "1", null, WorkflowInstanceStatus.NEW );
        assertInstance( facade.findWorkflowInstance( 2, true ), 2, NAME1, VERSION1, "1", null, WorkflowInstanceStatus.NEW );
        assertInstance( facade.findWorkflowInstance( 3, true ), 3, NAME2, null, "2", null, WorkflowInstanceStatus.NEW );

        // test sendSignalToWorkItem
        executor.startWorkflow( 1 );
        assertInstance( facade.findWorkflowInstance( 1, true ), 1, NAME1, VERSION1, "1", null, WorkflowInstanceStatus.EXECUTING );
        assertSignal( facade.findWorkItem( 1, true ), 1, 1, 2, WorkItemStatus.NEW, SIGNAL );
        facade.sendSignalToWorkItem( 1, SIGNAL, null );
        assertSignal( facade.findWorkItem( 1, true ), 1, 1, 2, WorkItemStatus.EXECUTED, SIGNAL );

        // test sendSignalByLabel1
        executor.startWorkflow( 2 );
        assertInstance( facade.findWorkflowInstance( 2, true ), 2, NAME1, VERSION1, "1", null, WorkflowInstanceStatus.EXECUTING );
        assertSignal( facade.findWorkItem( 5, true ), 5, 2, 2, WorkItemStatus.NEW, SIGNAL );
        facade.sendSignalByLabel1( "1", SIGNAL, null );
        assertSignal( facade.findWorkItem( 5, true ), 5, 2, 2, WorkItemStatus.EXECUTED, SIGNAL );

        // test sendSignalByLabels
        executor.startWorkflow( 3 );
        assertInstance( facade.findWorkflowInstance( 3, true ), 3, NAME2, VERSION2, "2", null, WorkflowInstanceStatus.EXECUTING );
        assertSignal( facade.findWorkItem( 9, true ), 9, 3, 2, WorkItemStatus.NEW, SIGNAL );
        facade.sendSignalByLabels( "2", null, SIGNAL, null );
        assertSignal( facade.findWorkItem( 9, true ), 9, 3, 2, WorkItemStatus.EXECUTED, SIGNAL );
        facade.sendSignalByLabels( "x", "y", SIGNAL, null );
    }

    @Test
    @DirtiesContext
    public void testCreateWorkflowInstancesAndFindByLabel(){
        CreateWorkflowInstance request1 = createRequest( NAME1, null, "1", null );
        CreateWorkflowInstance request2 = createRequest( NAME1, VERSION1, "1", null );
        CreateWorkflowInstance request3 = createRequest( NAME2, null, "2", null );
        CreateWorkflowInstance request4 = createRequest( NAME2, VERSION2, "2", null );
        CreateWorkflowInstance request5 = createRequest( UNKNOWN, null, "3", null );

        // test createWorkflowInstances
        facade.createWorkflowInstances( Arrays.asList( request1, request2, request3, request4, request5 ) );
        Assert.assertEquals( 1, (long)request1.getRefNum() );
        Assert.assertEquals( 2, (long)request2.getRefNum() );
        Assert.assertEquals( 3, (long)request3.getRefNum() );
        Assert.assertEquals( 4, (long)request4.getRefNum() );
        Assert.assertEquals( 5, (long)request5.getRefNum() );
        assertInstance( facade.findWorkflowInstance( 1, true ), 1, NAME1, null, "1", null, WorkflowInstanceStatus.NEW );
        assertInstance( facade.findWorkflowInstance( 2, true ), 2, NAME1, VERSION1, "1", null, WorkflowInstanceStatus.NEW );
        assertInstance( facade.findWorkflowInstance( 3, true ), 3, NAME2, null, "2", null, WorkflowInstanceStatus.NEW );
        assertInstance( facade.findWorkflowInstance( 4, true ), 4, NAME2, VERSION2, "2", null, WorkflowInstanceStatus.NEW );
        assertInstance( facade.findWorkflowInstance( 5, true ), 5, UNKNOWN, null, "3", null, WorkflowInstanceStatus.NEW );

        // test findWorkflowInstancesByLabels
        Assert.assertEquals( 2, facade.findWorkflowInstancesByLabels( "1", null, false ).size() );
        Assert.assertEquals( 2, facade.findWorkflowInstancesByLabels( "1", null, false ).size() );
        Assert.assertEquals( 2, facade.findWorkflowInstancesByLabel1( "1", false ).size() );
        Assert.assertEquals( 2, facade.findWorkflowInstancesByLabel1( "1", false ).size() );

        // test abortWorkflowInstance  
        facade.abortWorkflowInstance( 5 );
        executor.abortWorkflow( 5 );
        Assert.assertEquals( 1, facade.findWorkflowInstancesByLabels( "3", null, false ).size() );
        Assert.assertEquals( 0, facade.findWorkflowInstancesByLabels( "3", null, true ).size() );
    }

    @Test
    @DirtiesContext
    public void testNextActiveTimerDueDates(){
        engineFactory.getSingletonInstance().getRepository().addGraph( GRAPH1 );

        facade.createWorkflowInstance( createRequest( NAME1, null, "1", null ) );
        Assert.assertEquals( 0, facade.getNextActiveTimerDueDates( Collections.singletonList( 1l ) ).size() );

        executor.startWorkflow( 1 );
        Assert.assertEquals( 1, facade.getNextActiveTimerDueDates( Collections.singletonList( 1l ) ).size() );

        facade.skipTimer( 2 );
        Assert.assertEquals( 1, facade.getNextActiveTimerDueDates( Collections.singletonList( 1l ) ).size() );

        executor.completeWorkItem( 1, 2 );
        Assert.assertEquals( 0, facade.getNextActiveTimerDueDates( Collections.singletonList( 1l ) ).size() );
    }

    @Test
    @DirtiesContext
    public void testWorkflowInstancesWithHumanTasks(){
        engineFactory.getSingletonInstance().getRepository().addGraph( GRAPH1 );

        facade.createWorkflowInstance( createRequest( NAME1, null, "1", null ) );
        debug();
        Assert.assertEquals( 0, facade.getWorkflowInstancesWithActiveHumanTask( Collections.singletonList( 1l ) ).size() );

        executor.startWorkflow( 1 );
        debug();
        Assert.assertEquals( 1, facade.getWorkflowInstancesWithActiveHumanTask( Collections.singletonList( 1l ) ).size() );

        facade.submitHumanTask( 4, null );
        debug();
        Assert.assertEquals( 0, facade.getWorkflowInstancesWithActiveHumanTask( Collections.singletonList( 1l ) ).size() );

        executor.completeWorkItem( 1, 4 );
        debug();
        Assert.assertEquals( 0, facade.getWorkflowInstancesWithActiveHumanTask( Collections.singletonList( 1l ) ).size() );
    }

    private void debug(){
        System.out.println( "===============================================" );
        for( WorkItemState woit : facade.findWorkItems( 1, true ) ){
            System.out.println( ToStringBuilder.reflectionToString( woit ) );
        }
    }

    /**
     * <pre>
     *     +--[signal]--+
     * [AND]--[timer]---[AND]
     *     +--[task]----+
     *     +--[htask]---+
     * </pre>
     */
    private static Graph createTestGraph( String name, int version ){
        GraphImpl graph = new GraphImpl( name, version );

        Node fork = new AndFork( 1 );
        Node signal = new CatchSignal( 2, SIGNAL );
        Node timer = new CatchTimer( 3, TIMER_MS );
        Node task = new BeanAsyncCallActivity( 4, BEAN, METHOD, null, null );
        Node htask = new HumanTaskActivity( 5, ROLE, null, null, null );
        Node join = new AndJoin( -1 );

        graph.setStartNode( fork );
        graph.addNode( signal );
        graph.addNode( timer );
        graph.addNode( task );
        graph.addNode( htask );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_2", fork, signal ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, timer ) );
        graph.addTransition( new TransitionImpl( "1_4", fork, task ) );
        graph.addTransition( new TransitionImpl( "1_5", fork, htask ) );
        graph.addTransition( new TransitionImpl( signal, join ) );
        graph.addTransition( new TransitionImpl( timer, join ) );
        graph.addTransition( new TransitionImpl( task, join ) );
        graph.addTransition( new TransitionImpl( htask, join ) );

        return graph;
    }

    private CreateWorkflowInstance createRequest( String name, Integer version, String label1, String label2 ){
        CreateWorkflowInstance request = new CreateWorkflowInstance();
        request.setWorkflowName( name );
        request.setWorkflowVersion( version );
        request.setLabel1( label1 );
        request.setLabel2( label2 );
        return request;
    }

    private void assertInstance( WorkflowInstanceState woin,
                                 long refNum,
                                 String name,
                                 Integer version,
                                 String label1,
                                 String label2,
                                 WorkflowInstanceStatus status ){
        Assert.assertEquals( refNum, (long)woin.getRefNum() );
        Assert.assertEquals( name, woin.getWorkflowName() );
        Assert.assertEquals( version, woin.getWorkflowVersion() );
        Assert.assertEquals( label1, woin.getLabel1() );
        Assert.assertEquals( label2, woin.getLabel2() );
        Assert.assertEquals( status.name(), woin.getStatus() );
    }

    private void assertSignal( WorkItemState woit, long refNum, long woinRefNum, int tokenId, WorkItemStatus status, String signal ){
        assertCommon( woit, refNum, woinRefNum, tokenId, status );
        Assert.assertEquals( signal, woit.getSignal() );
    }

    private void assertTimer( WorkItemState woit, long refNum, long woinRefNum, int tokenId, WorkItemStatus status, boolean isDue ){
        assertCommon( woit, refNum, woinRefNum, tokenId, status );
        Assert.assertEquals( isDue, woit.getDueDate().getTime() - System.currentTimeMillis() <= 0 );
    }

    private void assertTask( WorkItemState woit, long refNum, long woinRefNum, int tokenId, WorkItemStatus status, String bean, String method ){
        assertCommon( woit, refNum, woinRefNum, tokenId, status );
        Assert.assertEquals( bean, woit.getBean() );
        Assert.assertEquals( method, woit.getMethod() );
    }

    private void assertHumanTask( WorkItemState woit, long refNum, long woinRefNum, int tokenId, WorkItemStatus status, String role, String user ){
        assertCommon( woit, refNum, woinRefNum, tokenId, status );
        Assert.assertEquals( role, woit.getRole() );
        Assert.assertEquals( user, woit.getUserName() );
    }

    private void assertCommon( WorkItemState woit, long refNum, long woinRefNum, int tokenId, WorkItemStatus status ){
        Assert.assertEquals( refNum, (long)woit.getRefNum() );
        Assert.assertEquals( woinRefNum, (long)woit.getWoinRefNum() );
        Assert.assertEquals( tokenId, woit.getTokenId() );
        Assert.assertEquals( status.name(), woit.getStatus() );
    }

    @Before
    public void prepareTest(){
        nodeService.findOrCreateByName( config.getNodeName() );
    }

}
