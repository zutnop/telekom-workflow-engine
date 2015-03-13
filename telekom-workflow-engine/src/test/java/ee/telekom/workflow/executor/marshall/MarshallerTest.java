package ee.telekom.workflow.executor.marshall;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.core.EnvironmentImpl;
import ee.telekom.workflow.graph.core.GraphEngineImpl;
import ee.telekom.workflow.graph.core.GraphInstanceImpl;
import ee.telekom.workflow.util.JsonUtil;

public class MarshallerTest{

    private static final Long EXTERNAL_ID = 10l;

    @Test
    public void test_sequence_one(){
        Graph graph = GraphFactory.INSTANCE.sequence_one();
        GraphEngineImpl engine = new GraphEngineImpl();

        GraphInstanceImpl instance = engine.start( graph, null, EXTERNAL_ID );
        assertMarshalling( instance );
    }

    @Test
    public void test_loop_cancelling_discriminator_twice(){
        Graph graph = GraphFactory.INSTANCE.loop_cancelling_discriminator_twice( 3 );
        GraphEngineImpl engine = new GraphEngineImpl();

        GraphInstanceImpl instance = engine.start( graph, null, EXTERNAL_ID );
        assertMarshalling( instance );
    }

    @Test
    public void test_signal(){
        Graph graph = GraphFactory.INSTANCE.signal_one_pre_post();
        GraphEngineImpl engine = new GraphEngineImpl();

        GraphInstanceImpl instance = engine.start( graph, null, EXTERNAL_ID );
        assertMarshalling( instance );

        GraphWorkItem wi = instance.getWorkItems().iterator().next();
        wi.setResult( "pseudo-result" );
        engine.complete( wi );
        assertMarshalling( instance );
    }

    @Test
    public void test_timer(){
        Graph graph = GraphFactory.INSTANCE.timer_one_pre_post();
        GraphEngineImpl engine = new GraphEngineImpl();

        GraphInstanceImpl instance = engine.start( graph, null, EXTERNAL_ID );
        assertMarshalling( instance );

        GraphWorkItem wi = instance.getWorkItems().iterator().next();
        engine.complete( wi );
        assertMarshalling( instance );
    }

    @Test
    public void test_task(){
        Graph graph = GraphFactory.INSTANCE.beanasynccall_one_pre_post_special( "bean", "method", null, null );
        GraphEngineImpl engine = new GraphEngineImpl();

        GraphInstanceImpl instance = engine.start( graph, null, EXTERNAL_ID );
        assertMarshalling( instance );

        GraphWorkItem wi = instance.getWorkItems().iterator().next();
        wi.setResult( "pseudo-result" );
        engine.complete( wi );
        assertMarshalling( instance );
    }

    private void assertMarshalling( GraphInstance instance1 ){
        Graph graph = instance1.getGraph();
        WorkflowInstance woin1 = new WorkflowInstance();
        List<WorkItem> woits1 = new LinkedList<>();
        Marshaller.marshall( instance1, woin1, woits1, WorkflowInstanceStatus.EXECUTED );

        GraphInstance instance2 = Marshaller.unmarshall( woin1, woits1, graph );

        WorkflowInstance woin2 = new WorkflowInstance();
        List<WorkItem> woits2 = new LinkedList<>();
        Marshaller.marshall( instance2, woin2, woits2, WorkflowInstanceStatus.EXECUTED );

        // Bugs should be assert'ed in the detailed comparison of instances,
        // but since this is a "easy" comparison, we also assert that the json
        // representations of the state objects are equal
        String json1a = JsonUtil.serialize( woin1, false );
        String json2a = JsonUtil.serialize( woin2, false );
        Assert.assertEquals( json1a, json2a );
        String json1b = JsonUtil.serialize( woin1, false );
        String json2b = JsonUtil.serialize( woin2, false );
        Assert.assertEquals( json1b, json2b );

        Assert.assertEquals( instance1.getGraph().getName(), instance2.getGraph().getName() );
        Assert.assertEquals( instance1.getGraph().getVersion(), instance2.getGraph().getVersion() );
        Assert.assertEquals( getAttributes( instance1.getEnvironment() ), getAttributes( instance2.getEnvironment() ) );
        assertTokens( instance1.getTokens(), instance2.getTokens() );
        Assert.assertEquals( ((GraphInstanceImpl)instance1).getTokenIdSequence(), ((GraphInstanceImpl)instance2).getTokenIdSequence() );
        assertWorkItems( instance1.getWorkItems(), instance2.getWorkItems() );

    }

    private void assertTokens( Collection<Token> tokens1, Collection<Token> tokens2 ){
        Assert.assertEquals( tokens1.size(), tokens2.size() );
        for( int i = 0; i < tokens1.size(); i++ ){
            Token token1 = ((List<Token>)tokens1).get( i );
            Token token2 = ((List<Token>)tokens2).get( i );
            Assert.assertEquals( token1.getId(), token2.getId() );
            Assert.assertEquals( token1.getNode().getId(), token2.getNode().getId() );
            Assert.assertEquals( getParentId( token1 ), getParentId( token2 ) );
            Assert.assertEquals( token1.isActive(), token2.isActive() );
        }

    }

    private void assertWorkItems( List<GraphWorkItem> workItems1, List<GraphWorkItem> workItems2 ){
        Assert.assertEquals( workItems1.size(), workItems2.size() );
        for( int i = 0; i < workItems1.size(); i++ ){
            GraphWorkItem wi1 = workItems1.get( i );
            GraphWorkItem wi2 = workItems2.get( i );
            Assert.assertEquals( wi1.getClass(), wi2.getClass() );
            Assert.assertEquals( wi1.getToken().getId(), wi2.getToken().getId() );
            Assert.assertEquals( wi1.getStatus(), wi2.getStatus() );
            Assert.assertEquals( wi1.getResult(), wi2.getResult() );
            Assert.assertEquals( wi1.getSignal(), wi2.getSignal() );
            Assert.assertEquals( wi1.getDueDate(), wi2.getDueDate() );
            Assert.assertEquals( wi1.getBean(), wi2.getBean() );
            Assert.assertEquals( wi1.getMethod(), wi2.getMethod() );
            Assert.assertEquals( wi1.getRole(), wi2.getRole() );
            Assert.assertEquals( wi1.getUser(), wi2.getUser() );
            if( wi1.getBean() != null ){
                Assert.assertArrayEquals( wi1.getTaskArguments(), wi2.getTaskArguments() );
            }
            else{
                Assert.assertEquals( wi1.getHumanTaskArguments(), wi2.getHumanTaskArguments() );
            }
        }
    }

    private Map<String, Object> getAttributes( Environment env ){
        return ((EnvironmentImpl)env).getAttributes();
    }

    private Integer getParentId( Token token ){
        return token.getParent() != null ? token.getParent().getId() : null;
    }
}
