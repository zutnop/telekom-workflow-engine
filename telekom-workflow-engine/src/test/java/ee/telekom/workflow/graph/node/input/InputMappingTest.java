package ee.telekom.workflow.graph.node.input;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.RecordPathScript;
import ee.telekom.workflow.graph.SimpleCounter;
import ee.telekom.workflow.graph.core.EnvironmentImpl;
import ee.telekom.workflow.graph.node.expression.Expression;
import ee.telekom.workflow.graph.node.expression.SimpleMethodCallExpression;
import ee.telekom.workflow.graph.node.output.OutputMapping;
import ee.telekom.workflow.graph.node.output.ValueMapping;

public class InputMappingTest extends AbstractGraphTest{

    @Test
    public void test_ConstantMapping(){
        SimpleCounter target = new SimpleCounter();
        String method = "incByDelta";
        InputMapping<Integer> delta = ConstantMapping.of( 5 );

        Graph graph = GraphFactory.INSTANCE.objectcall_one_pre_post( target, method, new InputMapping<?>[]{delta}, null );

        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        assertExecution( graph, null, expectedEnv );
        Assert.assertEquals( 5, target.get() );
    }

    @Test
    public void test_AttributeMapping(){
        SimpleCounter target = new SimpleCounter();
        String method = "incByDelta";
        InputMapping<Integer> delta = new AttributeMapping<Integer>( "delta" );

        Graph graph = GraphFactory.INSTANCE.objectcall_one_pre_post( target, method, new InputMapping<?>[]{delta}, null );

        EnvironmentImpl initialEnv = createSingletonEnvironment( "delta", 10 );
        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        expectedEnv.setAttribute( "delta", 10 );
        assertExecution( graph, initialEnv, expectedEnv );
        Assert.assertEquals( 10, target.get() );
    }

    @Test
    public void test_ConstantMapping_array(){
        SimpleCounter target = new SimpleCounter();
        String method = "incByDeltas";
        InputMapping<Object> deltas = new ConstantMapping<Object>( new int[]{5, 10, 20} );

        Graph graph = GraphFactory.INSTANCE.objectcall_one_pre_post( target, method, new InputMapping<?>[]{deltas}, null );

        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        assertExecution( graph, null, expectedEnv );
        Assert.assertEquals( 35, target.get() );
    }

    @Test
    public void test_AttributeMapping_array(){
        SimpleCounter target = new SimpleCounter();
        String method = "incByDeltas";
        InputMapping<Object> deltas = new AttributeMapping<Object>( "deltas" );

        Graph graph = GraphFactory.INSTANCE.objectcall_one_pre_post( target, method, new InputMapping<?>[]{deltas}, null );

        EnvironmentImpl initialEnv = createSingletonEnvironment( "deltas", new int[]{5, 10, 20} );
        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        expectedEnv.setAttribute( "deltas", new int[]{5, 10, 20} );
        assertExecution( graph, initialEnv, expectedEnv );
        Assert.assertEquals( 35, target.get() );
    }

    @Test
    public void test_ExpressionLanguageMapping(){
        SimpleCounter target = new SimpleCounter();
        String method = "incByDelta";
        InputMapping<Integer> deltaPlusOne = new ExpressionLanguageMapping<Integer>( "${(delta + 1).intValue()}" );

        Graph graph = GraphFactory.INSTANCE.objectcall_one_pre_post( target, method, new InputMapping<?>[]{deltaPlusOne}, null );

        EnvironmentImpl initialEnv = createSingletonEnvironment( "delta", 10 );
        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        expectedEnv.setAttribute( "delta", 10 );
        assertExecution( graph, initialEnv, expectedEnv );
        Assert.assertEquals( 11, target.get() );
    }

    @Test
    public void test_ExpressionMapping(){
        SimpleCounter target = new SimpleCounter();
        String method = "incByDeltaAndGet";
        OutputMapping resultMapping = new ValueMapping( "result" );

        SimpleCounter counter2 = new SimpleCounter();
        counter2.incByDelta( 11 );
        Expression<Integer> expression = new SimpleMethodCallExpression<Integer>( counter2, "incByDeltaAndGet" );
        InputMapping<Integer> delta = new AttributeMapping<Integer>( "delta" );
        InputMapping<Integer> expressionMapping = new ExpressionMapping<Integer>( expression, delta );

        Graph graph = GraphFactory.INSTANCE.objectcall_one_pre_post( target, method, new InputMapping<?>[]{expressionMapping}, resultMapping );

        EnvironmentImpl initialEnv = createSingletonEnvironment( "delta", 10 );
        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        expectedEnv.setAttribute( "delta", 10 );
        expectedEnv.setAttribute( "result", 21 );
        assertExecution( graph, initialEnv, expectedEnv );
        Assert.assertEquals( 21, target.get() );
    }

    @Test
    public void test_MapMapping(){
        SimpleCounter target = new SimpleCounter();
        String method = "add";

        MapMapping argument1 = new MapMapping();
        argument1.addEntryMapping( "value1", new AttributeMapping<Object>( "value1" ) );
        argument1.addEntryMapping( "value2", new ConstantMapping<Object>( 11 ) );

        Graph graph = GraphFactory.INSTANCE.objectcall_one_pre_post( target, method, new InputMapping<?>[]{argument1}, null );

        EnvironmentImpl initialEnv = createSingletonEnvironment( "value1", 12 );
        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        expectedEnv.setAttribute( "value1", 12 );
        assertExecution( graph, initialEnv, expectedEnv );
        Assert.assertEquals( 23, target.get() );

    }

}
