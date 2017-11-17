package ee.telekom.workflow.graph.node.activity;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.NewGraphInstanceCreator;
import ee.telekom.workflow.graph.RecordEventsListener;
import ee.telekom.workflow.graph.RecordPathScript;
import ee.telekom.workflow.graph.core.EnvironmentImpl;
import ee.telekom.workflow.graph.core.GraphEngineImpl;
import ee.telekom.workflow.graph.node.input.AttributeMapping;
import ee.telekom.workflow.graph.node.input.MapMapping;

public class CreateNewInstanceActivityTest extends AbstractGraphTest{

    @Test
    public void test_with_attributes(){
        MapMapping mapMapping = new MapMapping();
        mapMapping.addEntryMapping( "customerName", new AttributeMapping<Object>( "customerName" ) );
        Graph graph = GraphFactory.INSTANCE.create_new_instance("test-workflow", 1, "label1", "label2", mapMapping);

        EnvironmentImpl initialEnv = createSingletonEnvironment( "customerName", "John Smith" );
        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1" );
        expectedEnv.setAttribute( "customerName", "John Smith" );

        RecordEventsListener listener = new RecordEventsListener();
        RecordNewGraphInstanceCreator creator = new RecordNewGraphInstanceCreator();
        GraphEngineImpl engine = createEngine( listener );
        engine.setNewGraphInstanceCreator(creator);
        GraphInstance instance = engine.start( graph, initialEnv );
        
        assertActiveTokens( instance, 0 );
        assertActiveWorkItems( instance, 0 );
        assertEnvironmnent( instance, expectedEnv );
        Assert.assertEquals( EVENTS_0WI, listener.getEvents() );
        Assert.assertEquals( "test-workflow", creator.getGraphName() );
        Assert.assertEquals( Integer.valueOf("1"), creator.getGraphVersion() );
        Assert.assertEquals( "label1", creator.getLabel1() );
        Assert.assertEquals( "label2", creator.getLabel2() );
        assertEnvironmnent( creator.getInitialEnvironment(), initialEnv );
    }

    @Test
    public void test_without_attributes(){
        Graph graph = GraphFactory.INSTANCE.create_new_instance("test-workflow", null, null, null, null);

        EnvironmentImpl initialEnv = createSingletonEnvironment( "customerName", "John Smith" );
        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1" );
        expectedEnv.setAttribute( "customerName", "John Smith" );

        RecordEventsListener listener = new RecordEventsListener();
        RecordNewGraphInstanceCreator creator = new RecordNewGraphInstanceCreator();
        GraphEngineImpl engine = createEngine( listener );
        engine.setNewGraphInstanceCreator(creator);
        GraphInstance instance = engine.start( graph, initialEnv );
        
        assertActiveTokens( instance, 0 );
        assertActiveWorkItems( instance, 0 );
        assertEnvironmnent( instance, expectedEnv );
        Assert.assertEquals( EVENTS_0WI, listener.getEvents() );
        Assert.assertEquals( "test-workflow", creator.getGraphName() );
        Assert.assertEquals( null, creator.getGraphVersion() );
        Assert.assertEquals( null, creator.getLabel1() );
        Assert.assertEquals( null, creator.getLabel2() );
        assertEnvironmnent( creator.getInitialEnvironment(), new EnvironmentImpl() );
    }

    private static class RecordNewGraphInstanceCreator implements NewGraphInstanceCreator{

    	private String graphName;
    	private Integer graphVersion;
    	private String label1;
    	private String label2;
    	private Environment initialEnvironment;
    	
        @Override
        public void create( String graphName, Integer graphVersion, String label1, String label2, Environment initialEnvironment ){
        	this.graphName = graphName;
        	this.graphVersion = graphVersion;
        	this.label1 = label1;
        	this.label2 = label2;
        	this.initialEnvironment = initialEnvironment;
        }

        public String getGraphName() {
			return graphName;
		}
        
        public Integer getGraphVersion() {
			return graphVersion;
		}
        
        public String getLabel1() {
			return label1;
		}
        
        public String getLabel2() {
			return label2;
		}
        
        public Environment getInitialEnvironment() {
			return initialEnvironment;
		}
        
    }

}
