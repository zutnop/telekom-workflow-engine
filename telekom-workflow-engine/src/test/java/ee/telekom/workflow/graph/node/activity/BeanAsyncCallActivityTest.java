package ee.telekom.workflow.graph.node.activity;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.RecordEventsListener;
import ee.telekom.workflow.graph.RecordPathScript;
import ee.telekom.workflow.graph.SimpleCounter;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.WorkItemStatus;
import ee.telekom.workflow.graph.core.EnvironmentImpl;
import ee.telekom.workflow.graph.node.expression.SimpleMethodCallExpression;
import ee.telekom.workflow.graph.node.input.ConstantMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.output.OutputMapping;
import ee.telekom.workflow.graph.node.output.ValueMapping;

public class BeanAsyncCallActivityTest extends AbstractGraphTest{

    @Test
    public void test_inc(){
        SimpleCounter dummyBean = new SimpleCounter();
        String bean = "dummy";
        String method = "inc";

        Graph graph = GraphFactory.INSTANCE.beanasynccall_one_pre_post_special( bean, method, null, null );

        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        assertTaskExecution( graph, null, expectedEnv, dummyBean, bean, method, null );
        Assert.assertEquals( 1, dummyBean.get() );
    }

    @Test
    public void test_incByDelta(){
        SimpleCounter dummyBean = new SimpleCounter();
        String bean = "dummy";
        String method = "incByDelta";
        InputMapping<Integer> delta = ConstantMapping.of( 5 );

        Graph graph = GraphFactory.INSTANCE.beanasynccall_one_pre_post_special( bean, method, new InputMapping<?>[]{delta}, null );

        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        assertTaskExecution( graph, null, expectedEnv, dummyBean, bean, method, new Object[]{5} );
        Assert.assertEquals( 5, dummyBean.get() );
    }

    @Test
    public void test_get(){
        SimpleCounter dummyBean = new SimpleCounter();
        String bean = "dummy";
        String method = "get";
        OutputMapping resultMapping = new ValueMapping( "result" );

        Graph graph = GraphFactory.INSTANCE.beanasynccall_one_pre_post_special( bean, method, null, resultMapping );

        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        expectedEnv.setAttribute( "result", 0 );
        assertTaskExecution( graph, null, expectedEnv, dummyBean, bean, method, null );
        Assert.assertEquals( 0, dummyBean.get() );
    }

    @Test
    public void test_incAndGet(){
        SimpleCounter dummyBean = new SimpleCounter();
        String bean = "dummy";
        String method = "incAndGet";
        OutputMapping resultMapping = new ValueMapping( "result" );

        Graph graph = GraphFactory.INSTANCE.beanasynccall_one_pre_post_special( bean, method, null, resultMapping );

        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        expectedEnv.setAttribute( "result", 1 );
        assertTaskExecution( graph, null, expectedEnv, dummyBean, bean, method, null );
        Assert.assertEquals( 1, dummyBean.get() );
    }

    @Test
    public void test_incByDeltaAndGet(){
        SimpleCounter dummyBean = new SimpleCounter();
        String bean = "dummy";
        String method = "incByDeltaAndGet";
        OutputMapping resultMapping = new ValueMapping( "result" );
        InputMapping<Integer> delta = ConstantMapping.of( 5 );

        Graph graph = GraphFactory.INSTANCE.beanasynccall_one_pre_post_special( bean, method, new InputMapping<?>[]{delta}, resultMapping );

        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        expectedEnv.setAttribute( "result", 5 );
        assertTaskExecution( graph, null, expectedEnv, dummyBean, bean, method, new Object[]{5} );
        Assert.assertEquals( 5, dummyBean.get() );
    }

    private void assertTaskExecution( Graph graph,
                                      Environment initialEnv,
                                      Environment expectedEnv,
                                      Object dummyBean,
                                      String bean,
                                      String method,
                                      Object[] arguments ){
        RecordEventsListener listener = new RecordEventsListener();
        GraphEngine engine = createEngine( listener );
        GraphInstance instance = engine.start( graph, initialEnv );
        assertActiveTokens( instance, 1 );
        assertActiveWorkItems( instance, 1 );
        Token token = getTokenById( instance.getTokens(), 1 );
        assertAwaitingTask( token, bean, method, arguments );
        GraphWorkItem wi = token.getInstance().getActiveWorkItem( token );

        executeTask( wi, dummyBean );

        engine.complete( wi );

        assertActiveTokens( instance, 0 );
        assertActiveWorkItems( instance, 0 );
        assertEnvironmnent( instance, expectedEnv );
        Assert.assertEquals( EVENTS_1WI, listener.getEvents() );
    }

    private void assertAwaitingTask( Token token, String bean, String method, Object[] arguments ){
        Assert.assertTrue( token.isActive() );
        Assert.assertTrue( token.getNode() instanceof BeanAsyncCallActivity );

        GraphWorkItem wi = token.getInstance().getActiveWorkItem( token );

        Assert.assertEquals( bean, wi.getBean() );
        Assert.assertEquals( method, wi.getMethod() );
        Assert.assertArrayEquals( arguments, wi.getTaskArguments() );
        Assert.assertEquals( WorkItemStatus.NEW, wi.getStatus() );
    }

    private void executeTask( GraphWorkItem wi, Object dummyBean ){
        SimpleMethodCallExpression<Object> expression = new SimpleMethodCallExpression<Object>( dummyBean, wi.getMethod() );
        Object result = expression.execute( wi.getTaskArguments() );
        wi.setResult( result );
    }
}
