package ee.telekom.workflow.graph.node.activity;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.WorkflowException;
import ee.telekom.workflow.graph.core.EnvironmentImpl;
import ee.telekom.workflow.graph.core.GraphEngineImpl;

public class ValidateAttributeActivityTest extends AbstractGraphTest{

    @Test
    public void test_values(){
        Graph graph = GraphFactory.INSTANCE.validate_attribute();

        EnvironmentImpl initialEnv = new EnvironmentImpl();
        initialEnv.setAttribute( "required1", "v1" );
        initialEnv.setAttribute( "required2", "v2" );

        EnvironmentImpl expectedEnv = new EnvironmentImpl();
        expectedEnv.setAttribute( "required1", "v1" );
        expectedEnv.setAttribute( "required2", "v2" );
        expectedEnv.setAttribute( "optional1", null );
        expectedEnv.setAttribute( "optional2", null );
        expectedEnv.setAttribute( "optional3", "default" );
        expectedEnv.setAttribute( "attribute", false );

        GraphEngineImpl engine = new GraphEngineImpl();
        GraphInstance instance = engine.start( graph, initialEnv );

        assertEnvironmnent( instance, expectedEnv );
    }

    @Test
    public void test_missing_required_argument(){
        Graph graph = GraphFactory.INSTANCE.validate_attribute();

        EnvironmentImpl initialEnv = new EnvironmentImpl();
        initialEnv.setAttribute( "required1", "v1" );

        GraphEngineImpl engine = new GraphEngineImpl();
        try{
            engine.start( graph, initialEnv );
            Assert.fail( "Exception expected because required argument is missing" );
        }
        catch( WorkflowException e ){
        }
    }

    @Test
    public void test_missing_wrong_type(){
        Graph graph = GraphFactory.INSTANCE.validate_attribute();

        EnvironmentImpl initialEnv = new EnvironmentImpl();
        initialEnv.setAttribute( "required1", 1 );
        initialEnv.setAttribute( "required2", "null" );

        GraphEngineImpl engine = new GraphEngineImpl();
        try{
            engine.start( graph, initialEnv );
            Assert.fail( "Exception expected because required argument is missing" );
        }
        catch( WorkflowException e ){
        }
    }

}
