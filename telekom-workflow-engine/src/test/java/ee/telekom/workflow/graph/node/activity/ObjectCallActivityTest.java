package ee.telekom.workflow.graph.node.activity;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.RecordPathScript;
import ee.telekom.workflow.graph.SimpleCounter;
import ee.telekom.workflow.graph.core.EnvironmentImpl;
import ee.telekom.workflow.graph.node.input.ConstantMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.output.OutputMapping;
import ee.telekom.workflow.graph.node.output.ValueMapping;

public class ObjectCallActivityTest extends AbstractGraphTest{

    @Test
    public void test_inc(){
        SimpleCounter target = new SimpleCounter();
        String method = "inc";

        Graph graph = GraphFactory.INSTANCE.objectcall_one_pre_post( target, method, null, null );

        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        assertExecution( graph, null, expectedEnv );
        Assert.assertEquals( 1, target.get() );
    }

    @Test
    public void test_incByDelta(){
        SimpleCounter target = new SimpleCounter();
        String method = "incByDelta";
        InputMapping<Integer> delta = ConstantMapping.of( 5 );

        Graph graph = GraphFactory.INSTANCE.objectcall_one_pre_post( target, method, new InputMapping<?>[]{delta}, null );

        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        assertExecution( graph, null, expectedEnv );
        Assert.assertEquals( 5, target.get() );
    }

    @Test
    public void test_get(){
        SimpleCounter target = new SimpleCounter();
        String method = "get";
        OutputMapping resultMapping = new ValueMapping( "result" );

        Graph graph = GraphFactory.INSTANCE.objectcall_one_pre_post( target, method, null, resultMapping );

        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        expectedEnv.setAttribute( "result", 0 );
        assertExecution( graph, null, expectedEnv );
        Assert.assertEquals( 0, target.get() );
    }

    @Test
    public void test_incAndGet(){
        SimpleCounter target = new SimpleCounter();
        String method = "incAndGet";
        OutputMapping resultMapping = new ValueMapping( "result" );

        Graph graph = GraphFactory.INSTANCE.objectcall_one_pre_post( target, method, null, resultMapping );

        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        expectedEnv.setAttribute( "result", 1 );
        assertExecution( graph, null, expectedEnv );
        Assert.assertEquals( 1, target.get() );
    }

    @Test
    public void test_incByDeltaAndGet(){
        SimpleCounter target = new SimpleCounter();
        String method = "incByDeltaAndGet";
        OutputMapping resultMapping = new ValueMapping( "result" );
        InputMapping<Integer> delta = ConstantMapping.of( 5 );

        Graph graph = GraphFactory.INSTANCE.objectcall_one_pre_post( target, method, new InputMapping<?>[]{delta}, resultMapping );

        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        expectedEnv.setAttribute( "result", 5 );
        assertExecution( graph, null, expectedEnv );
        Assert.assertEquals( 5, target.get() );
    }

}
