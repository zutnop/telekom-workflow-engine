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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = TestApplicationContexts.DEFAULT)
@DirtiesContext
public class NoWorkItemIT extends AbstractWorkflowIT{

    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private RetryService retryService;
    @Autowired
    private AbortService abortService;

    @Autowired
    private WorkflowExecutor executor;

    @Autowired
    private GraphEngineFactory engineFactory;

    @Test
    public void start(){
        Graph graph = GraphFactory.INSTANCE.sequence_one();
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
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTED, null, false );
        assertActiveWoitsCount( woinRefNum, 0 );
    }

    @Test
    public void abort(){
        Graph graph = GraphFactory.INSTANCE.sequence_one();
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();
        Integer version = graph.getVersion();

        long woinRefNum = workflowInstanceService.create( name, version, INITIAL_ENV, LABEL1, LABEL2 ).getRefNum();
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.NEW, null, false );

        abortService.abort( woinRefNum );
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.ABORT, null, false );

        simulatePollLockAssign( woinRefNum, WorkType.ABORT_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.ABORT, config.getNodeName(), true );

        executor.abortWorkflow( woinRefNum );
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.ABORTED, null, false );
        assertActiveWoitsCount( woinRefNum, 0 );
    }

    @Test
    public void start_retry_start(){
        Graph graph = GraphFactory.INSTANCE.sequence_one();
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();
        Integer version = graph.getVersion();
        Map<String, Object> envMap;

        long woinRefNum = workflowInstanceService.create( name, version, INITIAL_ENV, LABEL1, LABEL2 ).getRefNum();
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.NEW, null, false );

        simulatePollLockAssign( woinRefNum, WorkType.START_WORKFLOW, null );
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.NEW, config.getNodeName(), true );

        RecordPathScript.startThrowingException();
        executor.startWorkflow( woinRefNum );
        RecordPathScript.stopThrowingException();
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.STARTING_ERROR, config.getNodeName(), true );
        assertActiveWoitsCount( woinRefNum, 0 );
        assertExer( woinRefNum, null, RecordPathScript.EXCEPTION_MESSAGE );

        retryService.retry( woinRefNum );
        assertNoExer( woinRefNum );
        assertWoin( woinRefNum, name, version, INITIAL_ENV, true, LABEL1, LABEL2, WorkflowInstanceStatus.NEW, null, false );

        simulatePollLockAssign( woinRefNum, WorkType.START_WORKFLOW, null );
        executor.startWorkflow( woinRefNum );
        envMap = createMap( INITIAL_ARGUMENT_KEY, INITIAL_ARGUMENT_VALUE, "path", "1" );
        assertWoin( woinRefNum, name, version, envMap, false, LABEL1, LABEL2, WorkflowInstanceStatus.EXECUTED, null, false );
    }

}
