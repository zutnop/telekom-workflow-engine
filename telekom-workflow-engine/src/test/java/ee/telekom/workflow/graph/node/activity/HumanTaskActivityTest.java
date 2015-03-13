package ee.telekom.workflow.graph.node.activity;

import java.util.HashMap;
import java.util.Map;

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
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.WorkItemStatus;
import ee.telekom.workflow.graph.core.EnvironmentImpl;
import ee.telekom.workflow.graph.node.input.AttributeMapping;
import ee.telekom.workflow.graph.node.input.ConstantMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.input.MapMapping;

public class HumanTaskActivityTest extends AbstractGraphTest{

    @Test
    public void test_simple(){
        InputMapping<String> roleMapping = ConstantMapping.of( "role-id" );
        InputMapping<String> userMapping = ConstantMapping.of( "user-id" );
        MapMapping argumentsMapping = new MapMapping();
        argumentsMapping.addEntryMapping( "clientId", new AttributeMapping<Object>( "clientId" ) );
        argumentsMapping.addEntryMapping( "taskId", new ConstantMapping<Object>( "job" ) );

        Graph graph = GraphFactory.INSTANCE.human_task_one_pre_post_special( roleMapping, userMapping, argumentsMapping, null );

        EnvironmentImpl initialEnv = createSingletonEnvironment( "clientId", 10 );
        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, "1,3" );
        expectedEnv.setAttribute( "clientId", 10 );

        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put( "clientId", 10 );
        arguments.put( "taskId", "job" );

        assertHumanTaskExecution( graph, initialEnv, expectedEnv, "role-id", "user-id", arguments );
    }

    private void assertHumanTaskExecution( Graph graph, Environment initialEnv,
                                           Environment expectedEnv, String role, String user,
                                           Map<String, Object> arguments ){
        RecordEventsListener listener = new RecordEventsListener();
        GraphEngine engine = createEngine( listener );
        GraphInstance instance = engine.start( graph, initialEnv );
        assertActiveTokens( instance, 1 );
        assertActiveWorkItems( instance, 1 );
        Token token = getTokenById( instance.getTokens(), 1 );
        assertAwaitingHumanTask( token, role, user, arguments );
        GraphWorkItem wi = token.getInstance().getActiveWorkItem( token );

        engine.complete( wi );

        assertActiveTokens( instance, 0 );
        assertActiveWorkItems( instance, 0 );
        assertEnvironmnent( instance, expectedEnv );
        Assert.assertEquals( EVENTS_1WI, listener.getEvents() );
    }

    private void assertAwaitingHumanTask( Token token, String role, String user, Map<String, Object> arguments ){
        Assert.assertTrue( token.isActive() );
        Assert.assertTrue( token.getNode() instanceof HumanTaskActivity );

        GraphWorkItem wi = token.getInstance().getActiveWorkItem( token );

        Assert.assertEquals( role, wi.getRole() );
        Assert.assertEquals( user, wi.getUser() );
        Assert.assertEquals( arguments, wi.getHumanTaskArguments() );
        Assert.assertEquals( WorkItemStatus.NEW, wi.getStatus() );
    }

}
