package ee.telekom.workflow.executor;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ee.telekom.workflow.TestApplicationContexts;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = TestApplicationContexts.DEFAULT)
@DirtiesContext
public class CreateNewInstanceActivityIT extends AbstractWorkflowIT{

    @Autowired
    private WorkflowInstanceService workflowInstanceService;

    @Autowired
    private WorkflowExecutor executor;

    @Autowired
    private GraphEngineFactory engineFactory;

    @Test
    public void test(){
        Graph graph = GraphFactory.INSTANCE.createNewInstanceActivity( "test-workflow" );
        GraphRepository repo = engineFactory.getSingletonInstance().getRepository();
        repo.addGraph( graph );

        String name = graph.getName();

        long woinRefNum = workflowInstanceService.create( name, null, null, null, null ).getRefNum();
        executor.startWorkflow( woinRefNum );

        long createWoinRefNum = woinRefNum + 1;
        Map<String, Object> arguments = Collections.singletonMap( "other", (Object)"Heli Kopter" );
        assertWoin( createWoinRefNum, "test-workflow", null, arguments, true, "a", "b", WorkflowInstanceStatus.NEW, null, false );
    }

}
