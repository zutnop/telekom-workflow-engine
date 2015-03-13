package ee.telekom.workflow.core.archive;

import java.util.Collections;
import java.util.Date;

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
import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.core.workitem.WorkItemDao;
import ee.telekom.workflow.executor.GraphEngineFactory;
import ee.telekom.workflow.executor.WorkflowExecutor;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphRepository;
import ee.telekom.workflow.graph.WorkItemStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = TestApplicationContexts.DEFAULT)
@DirtiesContext
public class ArchiveIT extends TestApplicationContexts{

    @Autowired
    private WorkflowInstanceService woinService;
    @Autowired
    private WorkItemDao woitDao;
    @Autowired
    private ArchiveDao archiveDao;
    @Autowired
    private GraphEngineFactory engineFactory;
    @Autowired
    private WorkflowExecutor executor;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private WorkflowEngineConfiguration config;

    @Test
    public void test_1(){
        WorkflowInstance woin = woinService.create( "a", 1, null, "label1", "label2" );
        long woinRefNum = woin.getRefNum();
        WorkItem woit = createWoit( woinRefNum );
        woitDao.create( Collections.singletonList( woit ) );

        archiveDao.archive( woinRefNum );

        Assert.assertNull( woinService.find( woinRefNum ) );
        Assert.assertTrue( woitDao.findByWoinRefNum( woinRefNum ).isEmpty() );
        assertWoin( woin );
        assertWoit( woit );
    }

    @Test
    public void test_2(){
        Graph graph = GraphFactory.INSTANCE.timer_one();
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();
        Integer version = graph.getVersion();

        long woinRefNum = woinService.create( name, version, Collections.singletonMap( "super", (Object)"man" ), "LABEL1", "LABEL2" ).getRefNum();
        executor.startWorkflow( woinRefNum );
        WorkflowInstance woin = woinService.find( woinRefNum );
        WorkItem woit = woitDao.findByWoinRefNum( woinRefNum ).get( 0 );

        archiveDao.archive( woinRefNum );
        Assert.assertNull( woinService.find( woinRefNum ) );
        Assert.assertTrue( woitDao.findByWoinRefNum( woinRefNum ).isEmpty() );
        assertWoin( woin );
        assertWoit( woit );
    }

    private void assertWoin( WorkflowInstance expected ){
        WorkflowInstance actual = archiveDao.findWoinByRefNum( expected.getRefNum() );
        Assert.assertEquals( expected.getRefNum(), actual.getRefNum() );
        Assert.assertEquals( expected.getWorkflowName(), actual.getWorkflowName() );
        Assert.assertEquals( expected.getWorkflowVersion(), actual.getWorkflowVersion() );
        Assert.assertEquals( expected.getAttributes(), actual.getAttributes() );
        Assert.assertEquals( expected.getState(), actual.getState() );
        Assert.assertEquals( expected.getHistory(), actual.getHistory() );
        Assert.assertEquals( expected.getLabel1(), actual.getLabel1() );
        Assert.assertEquals( expected.getLabel2(), actual.getLabel2() );
        Assert.assertEquals( expected.getStatus(), actual.getStatus() );
        Assert.assertEquals( expected.getClusterName(), actual.getClusterName() );
        Assert.assertEquals( expected.getNodeName(), actual.getNodeName() );
        Assert.assertEquals( expected.isLocked(), actual.isLocked() );

    }

    protected void assertWoit( WorkItem expected ){
        WorkItem actual = archiveDao.findWoitByRefNum( expected.getRefNum() );
        Assert.assertEquals( expected.getRefNum(), actual.getRefNum() );
        Assert.assertEquals( expected.getWoinRefNum(), actual.getWoinRefNum() );
        Assert.assertEquals( expected.getTokenId(), actual.getTokenId() );
        Assert.assertEquals( expected.getStatus(), actual.getStatus() );
        Assert.assertEquals( expected.getSignal(), actual.getSignal() );
        Assert.assertEquals( expected.getDueDate(), actual.getDueDate() );
        Assert.assertEquals( expected.getBean(), actual.getBean() );
        Assert.assertEquals( expected.getMethod(), actual.getMethod() );
        Assert.assertEquals( expected.getRole(), actual.getRole() );
        Assert.assertEquals( expected.getUserName(), actual.getUserName() );
        Assert.assertEquals( expected.getArguments(), actual.getArguments() );
        Assert.assertEquals( expected.getResult(), actual.getResult() );

    }

    private WorkItem createWoit( long woinRefNum ){
        WorkItem woit = new WorkItem();
        woit.setWoinRefNum( woinRefNum );
        woit.setTokenId( 1 );
        woit.setStatus( WorkItemStatus.NEW );
        woit.setSignal( "signal" );
        woit.setDueDate( new Date() );
        woit.setBean( "bean" );
        woit.setMethod( "method" );
        woit.setRole( "role" );
        woit.setUserName( "userName" );
        woit.setArguments( "test-argumetns" );
        woit.setResult( "test-result" );
        return woit;
    }

    @Before
    public void prepareTest(){
        nodeService.findOrCreateByName( config.getNodeName() );
    }

}
