package ee.telekom.workflow.executor;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ee.telekom.workflow.TestApplicationContexts;
import ee.telekom.workflow.core.abort.AbortService;
import ee.telekom.workflow.core.retry.RetryService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.core.workunit.WorkType;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphRepository;
import ee.telekom.workflow.graph.RecordPathScript;
import ee.telekom.workflow.graph.WorkItemStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = TestApplicationContexts.DEFAULT)
@DirtiesContext
public class OneTimerItemIT extends AbstractWorkflowIT{

    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private AbortService abortService;
    @Autowired
    private RetryService retryService;

    @Autowired
    private WorkflowExecutor executor;

    @Autowired
    private GraphEngineFactory engineFactory;

    @Test
    public void start_complete(){
        Graph graph = GraphFactory.INSTANCE.timer_one_pre_post();
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();
        Integer version = graph.getVersion();
        Map<String, Object> envMap;

        long woinRefNum = workflowInstanceService.create( name, version, INITIAL_ENV, LABEL1, LABEL2 ).getRefNum();
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.NEW, null, false );

        simulatePollLockAssign( woinRefNum, WorkType.START_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.NEW, config.getNodeName(), true );

        executor.startWorkflow( woinRefNum );
        envMap = createMap( INITIAL_ARGUMENT_KEY, INITIAL_ARGUMENT_VALUE, "path", "1" );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTING, null, false );
        assertActiveWoitsCount( woinRefNum, 1 );
        long woitRefNum = getActiveWoitRefNum( woinRefNum, 1 );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, TIMER_MS );

        simulatePollLockAssign( woinRefNum, WorkType.COMPLETE_WORK_ITEM, woitRefNum, TIMER_MS );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTING, config.getNodeName(), true );

        executor.completeWorkItem( woinRefNum, woitRefNum );
        envMap = createMap( INITIAL_ARGUMENT_KEY, INITIAL_ARGUMENT_VALUE, "path", "1,3" );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTED, null, false );
        assertActiveWoitsCount( woinRefNum, 0 );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.COMPLETED, TIMER_MS );
    }

    @Test
    public void start_complete_error_retry_complete(){
        Graph graph = GraphFactory.INSTANCE.timer_one_pre_post();
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();
        Integer version = graph.getVersion();
        Map<String, Object> envMap;

        long woinRefNum = workflowInstanceService.create( name, version, INITIAL_ENV, LABEL1, LABEL2 ).getRefNum();
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.NEW, null, false );

        simulatePollLockAssign( woinRefNum, WorkType.START_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.NEW, config.getNodeName(), true );

        executor.startWorkflow( woinRefNum );
        envMap = createMap( INITIAL_ARGUMENT_KEY, INITIAL_ARGUMENT_VALUE, "path", "1" );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTING, null, false );
        assertActiveWoitsCount( woinRefNum, 1 );
        long woitRefNum = getActiveWoitRefNum( woinRefNum, 1 );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, TIMER_MS );

        simulatePollLockAssign( woinRefNum, WorkType.COMPLETE_WORK_ITEM, woitRefNum, TIMER_MS );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTING, config.getNodeName(), true );

        RecordPathScript.startThrowingException();
        executor.completeWorkItem( woinRefNum, woitRefNum );
        RecordPathScript.stopThrowingException();
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTING_ERROR, config.getNodeName(), true );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.COMPLETING_ERROR, TIMER_MS );
        assertExer( woinRefNum, woitRefNum, RecordPathScript.EXCEPTION_MESSAGE );

        retryService.retry( woinRefNum );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTING, null, false );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, TIMER_MS );
        assertNoExer( woinRefNum );

        simulatePollLockAssign( woinRefNum, WorkType.COMPLETE_WORK_ITEM, woitRefNum, TIMER_MS );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTING, config.getNodeName(), true );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, TIMER_MS );

        executor.completeWorkItem( woinRefNum, woitRefNum );
        envMap = createMap( INITIAL_ARGUMENT_KEY, INITIAL_ARGUMENT_VALUE, "path", "1,3" );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTED, null, false );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.COMPLETED, TIMER_MS );
        assertActiveWoitsCount( woinRefNum, 0 );
    }

    @Test
    public void start_abort(){
        Graph graph = GraphFactory.INSTANCE.timer_one_pre_post();
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();
        Integer version = graph.getVersion();
        Map<String, Object> envMap;

        long woinRefNum = workflowInstanceService.create( name, version, INITIAL_ENV, LABEL1, LABEL2 ).getRefNum();
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.NEW, null, false );

        simulatePollLockAssign( woinRefNum, WorkType.START_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.NEW, config.getNodeName(), true );

        executor.startWorkflow( woinRefNum );
        envMap = createMap( INITIAL_ARGUMENT_KEY, INITIAL_ARGUMENT_VALUE, "path", "1" );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTING, null, false );
        assertActiveWoitsCount( woinRefNum, 1 );
        long woitRefNum = getActiveWoitRefNum( woinRefNum, 1 );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, TIMER_MS );

        abortService.abort( woinRefNum );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.ABORT, null, false );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, TIMER_MS );

        simulatePollLockAssign( woinRefNum, WorkType.ABORT_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.ABORT, config.getNodeName(), true );

        executor.abortWorkflow( woinRefNum );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.ABORTED, null, false );
        assertActiveWoitsCount( woinRefNum, 0 );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.CANCELLED, TIMER_MS );
    }

    @Test
    public void start_complete_error_abort(){
        Graph graph = GraphFactory.INSTANCE.timer_one_pre_post();
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();
        Integer version = graph.getVersion();
        Map<String, Object> envMap;

        long woinRefNum = workflowInstanceService.create( name, version, INITIAL_ENV, LABEL1, LABEL2 ).getRefNum();
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.NEW, null, false );

        simulatePollLockAssign( woinRefNum, WorkType.START_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.NEW, config.getNodeName(), true );

        executor.startWorkflow( woinRefNum );
        envMap = createMap( INITIAL_ARGUMENT_KEY, INITIAL_ARGUMENT_VALUE, "path", "1" );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTING, null, false );
        assertActiveWoitsCount( woinRefNum, 1 );
        long woitRefNum = getActiveWoitRefNum( woinRefNum, 1 );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, TIMER_MS );

        simulatePollLockAssign( woinRefNum, WorkType.COMPLETE_WORK_ITEM, woitRefNum, TIMER_MS );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTING, config.getNodeName(), true );

        RecordPathScript.startThrowingException();
        executor.completeWorkItem( woinRefNum, woitRefNum );
        RecordPathScript.stopThrowingException();
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTING_ERROR, config.getNodeName(), true );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.COMPLETING_ERROR, TIMER_MS );
        assertExer( woinRefNum, woitRefNum, RecordPathScript.EXCEPTION_MESSAGE );

        abortService.abort( woinRefNum );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.ABORT, null, false );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, TIMER_MS );

        simulatePollLockAssign( woinRefNum, WorkType.ABORT_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.ABORT, config.getNodeName(), true );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.NEW, TIMER_MS );

        executor.abortWorkflow( woinRefNum );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.ABORTED, null, false );
        assertActiveWoitsCount( woinRefNum, 0 );
        assertTimerWoit( woitRefNum, woinRefNum, 1, WorkItemStatus.CANCELLED, TIMER_MS );
    }

}
