package ee.telekom.workflow.core.recovery;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ee.telekom.workflow.TestApplicationContexts;
import ee.telekom.workflow.api.AutoRecovery;
import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.error.ExecutionErrorService;
import ee.telekom.workflow.core.node.NodeService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.core.workitem.WorkItemDao;
import ee.telekom.workflow.core.workitem.WorkItemService;
import ee.telekom.workflow.graph.WorkItemStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = TestApplicationContexts.DEFAULT)
@DirtiesContext
public class RecoveryIT extends TestApplicationContexts{

    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private WorkItemService workItemService;
    @Autowired
    private WorkItemDao workItemDao;
    @Autowired
    private ExecutionErrorService executionErrorService;

    @Autowired
    private RecoveryService recoveryService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private WorkflowEngineConfiguration config;

    @Test
    public void test_recoveryUnassigned(){
        long woinRefNum = workflowInstanceService.create( "test", 1, null, null, null ).getRefNum();
        workflowInstanceService.lock( Collections.singletonList( woinRefNum ) );
        assertUnassignedtRecovery( woinRefNum );
    }

    @Test
    public void test_recoveryAssigned_New(){
        long woinRefNum = workflowInstanceService.create( "test", 1, null, null, null ).getRefNum();
        workflowInstanceService.lock( Collections.singletonList( woinRefNum ) );
        workflowInstanceService.updateNodeNameFromNull( woinRefNum, config.getNodeName() );
        assertAssignedRecovery( woinRefNum, WorkflowInstanceStatus.NEW, WorkflowInstanceStatus.NEW );
    }

    @Test
    public void test_recoveryAssigned_Starting(){
        long woinRefNum = workflowInstanceService.create( "test", 1, null, null, null ).getRefNum();
        workflowInstanceService.lock( Collections.singletonList( woinRefNum ) );
        workflowInstanceService.updateNodeNameFromNull( woinRefNum, config.getNodeName() );
        workflowInstanceService.markStarting( woinRefNum );
        assertAssignedRecovery( woinRefNum, WorkflowInstanceStatus.STARTING, WorkflowInstanceStatus.NEW );
    }

    @Test
    public void test_recoveryAssigned_Abort(){
        long woinRefNum = workflowInstanceService.create( "test", 1, null, null, null ).getRefNum();
        workflowInstanceService.lock( Collections.singletonList( woinRefNum ) );
        workflowInstanceService.updateNodeNameFromNull( woinRefNum, config.getNodeName() );
        workflowInstanceService.markAbort( woinRefNum );
        assertAssignedRecovery( woinRefNum, WorkflowInstanceStatus.ABORT, WorkflowInstanceStatus.ABORT );
    }

    @Test
    public void test_recoveryAssigned_Aborting(){
        long woinRefNum = workflowInstanceService.create( "test", 1, null, null, null ).getRefNum();
        workflowInstanceService.lock( Collections.singletonList( woinRefNum ) );
        workflowInstanceService.updateNodeNameFromNull( woinRefNum, config.getNodeName() );
        workflowInstanceService.markAbort( woinRefNum );
        workflowInstanceService.markAborting( woinRefNum );
        assertAssignedRecovery( woinRefNum, WorkflowInstanceStatus.ABORTING, WorkflowInstanceStatus.ABORT );
    }

    @Test
    public void test_recoveryAssigned_Executing_New(){
        long woinRefNum = workflowInstanceService.create( "test", 1, null, null, null ).getRefNum();
        long woitRefNum = createWoit( woinRefNum, "signal", null, null );
        workflowInstanceService.lock( Collections.singletonList( woinRefNum ) );
        workflowInstanceService.updateNodeNameFromNull( woinRefNum, config.getNodeName() );
        workflowInstanceService.markStarting( woinRefNum );
        workflowInstanceService.markExecuting( woinRefNum );
        assertAssignedRecovery( woinRefNum, woitRefNum, WorkflowInstanceStatus.EXECUTING, WorkflowInstanceStatus.EXECUTING, WorkItemStatus.NEW,
                WorkItemStatus.NEW, false );
    }

    @Test
    public void test_recoveryAssigned_Executing_Executing(){
        long woinRefNum = workflowInstanceService.create( "test", 1, null, null, null ).getRefNum();
        long woitRefNum = createWoit( woinRefNum, "signal", null, null );
        workflowInstanceService.lock( Collections.singletonList( woinRefNum ) );
        workflowInstanceService.updateNodeNameFromNull( woinRefNum, config.getNodeName() );
        workflowInstanceService.markStarting( woinRefNum );
        workflowInstanceService.markExecuting( woinRefNum );
        workItemService.markExecuting( woitRefNum );
        assertAssignedRecovery( woinRefNum, woitRefNum, WorkflowInstanceStatus.EXECUTING, WorkflowInstanceStatus.EXECUTING, WorkItemStatus.EXECUTING,
                WorkItemStatus.NEW, false );
    }

    @Test
    public void test_recoveryAssigned_Executing_Executing_Task(){
        long woinRefNum = workflowInstanceService.create( "test", 1, null, null, null ).getRefNum();
        long woitRefNum = createWoit( woinRefNum, null, "bean", "method", AutoRecovery.DISABLED );
        workflowInstanceService.lock( Collections.singletonList( woinRefNum ) );
        workflowInstanceService.updateNodeNameFromNull( woinRefNum, config.getNodeName() );
        workflowInstanceService.markStarting( woinRefNum );
        workflowInstanceService.markExecuting( woinRefNum );
        workItemService.markExecuting( woitRefNum );
        assertAssignedRecovery( woinRefNum, woitRefNum, WorkflowInstanceStatus.EXECUTING, WorkflowInstanceStatus.EXECUTING_ERROR, WorkItemStatus.EXECUTING,
                WorkItemStatus.EXECUTING_ERROR, true );
    }

    @Test
    public void test_recoveryAssigned_Executing_Executed(){
        long woinRefNum = workflowInstanceService.create( "test", 1, null, null, null ).getRefNum();
        long woitRefNum = createWoit( woinRefNum, "signal", null, null );
        workflowInstanceService.lock( Collections.singletonList( woinRefNum ) );
        workflowInstanceService.updateNodeNameFromNull( woinRefNum, config.getNodeName() );
        workflowInstanceService.markStarting( woinRefNum );
        workflowInstanceService.markExecuting( woinRefNum );
        workItemService.markExecuting( woitRefNum );
        workItemService.markExecutedAndSaveResult( woitRefNum, null );
        assertAssignedRecovery( woinRefNum, woitRefNum, WorkflowInstanceStatus.EXECUTING, WorkflowInstanceStatus.EXECUTING, WorkItemStatus.EXECUTED,
                WorkItemStatus.EXECUTED, false );
    }

    @Test
    public void test_recoveryAssigned_Executing_Completing(){
        long woinRefNum = workflowInstanceService.create( "test", 1, null, null, null ).getRefNum();
        long woitRefNum = createWoit( woinRefNum, "signal", null, null );
        workflowInstanceService.lock( Collections.singletonList( woinRefNum ) );
        workflowInstanceService.updateNodeNameFromNull( woinRefNum, config.getNodeName() );
        workflowInstanceService.markStarting( woinRefNum );
        workflowInstanceService.markExecuting( woinRefNum );
        workItemService.markExecuting( woitRefNum );
        workItemService.markExecutedAndSaveResult( woitRefNum, null );
        workItemService.markCompleting( woitRefNum );
        assertAssignedRecovery( woinRefNum, woitRefNum, WorkflowInstanceStatus.EXECUTING, WorkflowInstanceStatus.EXECUTING, WorkItemStatus.COMPLETING,
                WorkItemStatus.EXECUTED, false );
    }

    private void assertUnassignedtRecovery( long woinRefNum ){
        assertWoinStatusAndLock( woinRefNum, WorkflowInstanceStatus.NEW, true, null );
        recoveryService.recoverExecutionsNotAssignedToNodes( config.getClusterName() );
        assertWoinStatusAndLock( woinRefNum, WorkflowInstanceStatus.NEW, false, null );
    }

    private void assertAssignedRecovery( long woinRefNum, WorkflowInstanceStatus currentStatus, WorkflowInstanceStatus expectedStatus ){
        assertWoinStatusAndLock( woinRefNum, currentStatus, true, config.getNodeName() );
        recoveryService.recoverExecutionsAssignedToNodes( Collections.singletonList( config.getNodeName() ) );
        assertWoinStatusAndLock( woinRefNum, expectedStatus, false, null );
    }

    private void assertAssignedRecovery( long woinRefNum,
                                         long woitRefNum,
                                         WorkflowInstanceStatus currentStatus,
                                         WorkflowInstanceStatus expectedStatus,
                                         WorkItemStatus currentWoitStatus,
                                         WorkItemStatus expectedWoitStatus,
                                         boolean hasError ){
        assertWoinStatusAndLock( woinRefNum, currentStatus, true, config.getNodeName() );
        assertWoitStatus( woitRefNum, currentWoitStatus );
        recoveryService.recoverExecutionsAssignedToNodes( Collections.singletonList( config.getNodeName() ) );
        assertWoinStatusAndLock( woinRefNum, expectedStatus, hasError, hasError ? config.getNodeName() : null );
        assertWoitStatus( woitRefNum, expectedWoitStatus );
        Assert.assertEquals( hasError, executionErrorService.findByWoinRefNum( woinRefNum ) != null );
    }

    private void assertWoinStatusAndLock( long refNum, WorkflowInstanceStatus status, boolean locked, String nodeName ){
        WorkflowInstance woin = workflowInstanceService.find( refNum );
        Assert.assertEquals( status, woin.getStatus() );
        Assert.assertEquals( locked, woin.isLocked() );
        Assert.assertEquals( nodeName, woin.getNodeName() );
    }

    private void assertWoitStatus( long refNum, WorkItemStatus status ){
        WorkItem woit = workItemService.find( refNum );
        Assert.assertEquals( status, woit.getStatus() );
    }

    private long createWoit( long woinRefNum, String signal, String bean, String method ){
        WorkItem woit = new WorkItem();
        woit.setStatus( WorkItemStatus.NEW );
        woit.setWoinRefNum( woinRefNum );
        woit.setSignal( signal );
        woit.setBean( bean );
        woit.setMethod( method );
        workItemDao.create( Collections.singletonList( woit ) );
        return woit.getRefNum();
    }

    private long createWoit( long woinRefNum, String signal, String bean, String method, AutoRecovery autoRecovery ){
        WorkItem woit = new WorkItem();
        woit.setStatus( WorkItemStatus.NEW );
        woit.setWoinRefNum( woinRefNum );
        woit.setSignal( signal );
        woit.setBean( bean );
        woit.setMethod( method );
        woit.setAutoRecovery(autoRecovery);
        workItemDao.create( Collections.singletonList( woit ) );
        return woit.getRefNum();
    }

    @Before
    public void prepareTest(){
        nodeService.findOrCreateByName( config.getNodeName() );
    }

}
