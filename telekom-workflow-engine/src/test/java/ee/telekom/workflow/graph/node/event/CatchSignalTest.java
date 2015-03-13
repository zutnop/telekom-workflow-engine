package ee.telekom.workflow.graph.node.event;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.RecordEventsListener;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.node.output.MapEntryMapping;
import ee.telekom.workflow.graph.node.output.OutputMapping;
import ee.telekom.workflow.graph.node.output.ValueMapping;

public class CatchSignalTest extends AbstractGraphTest{

    @Test
    public void one(){
        RecordEventsListener listener = new RecordEventsListener();
        GraphEngine engine = createEngine( listener );
        Graph graph = GraphFactory.INSTANCE.signal_one();

        GraphInstance instance = engine.start( graph, null );
        assertActiveTokens( instance, 1 );
        Token token = instance.getTokens().iterator().next();
        assertAwaitingSignal( token, GraphFactory.SIGNAL );
        assertActiveWorkItems( instance, 1 );

        completeWorkItemOnToken( engine, token );
        assertActiveTokens( instance, 0 );
        assertActiveWorkItems( instance, 0 );
        Assert.assertEquals( EVENTS_1WI, listener.getEvents() );
    }

    @Test
    public void one_with_valuemapping(){
        Object result = new Object();
        OutputMapping resultMapping = new ValueMapping( "result" );

        RecordEventsListener listener = new RecordEventsListener();
        GraphEngine engine = createEngine( listener );
        Graph graph = GraphFactory.INSTANCE.signal_one_special( resultMapping );

        GraphInstance instance = engine.start( graph, null );
        GraphWorkItem wi = getActiveWorkItems( instance ).iterator().next();
        wi.setResult( result );
        engine.complete( wi );
        Assert.assertSame( result,
                instance.getEnvironment().getAttribute( "result" ) );
        Assert.assertEquals( EVENTS_1WI, listener.getEvents() );
    }

    @Test
    public void one_with_mapentrymapping(){
        Map<String, String> result = new HashMap<>();
        result.put( "alpha", "alphaValue" );
        result.put( "beta", "betaValue" );

        Map<String, String> mapping = new HashMap<>();
        mapping.put( "alpha", "a" );
        mapping.put( "beta", "b" );
        OutputMapping resultMapping = new MapEntryMapping( mapping );

        RecordEventsListener listener = new RecordEventsListener();
        GraphEngine engine = createEngine( listener );
        Graph graph = GraphFactory.INSTANCE.signal_one_special( resultMapping );

        GraphInstance instance = engine.start( graph, null );
        GraphWorkItem wi = getActiveWorkItems( instance ).iterator().next();
        wi.setResult( result );
        engine.complete( wi );
        Assert.assertEquals( "alphaValue", instance.getEnvironment()
                .getAttribute( "a" ) );
        Assert.assertEquals( "betaValue", instance.getEnvironment()
                .getAttribute( "b" ) );
        Assert.assertEquals( EVENTS_1WI, listener.getEvents() );
    }

    @Test
    public void one_pre_post(){
        RecordEventsListener listener = new RecordEventsListener();
        GraphEngine engine = createEngine( listener );
        Graph graph = GraphFactory.INSTANCE.signal_one_pre_post();

        GraphInstance instance = engine.start( graph, null );
        assertActiveTokens( instance, 1 );
        Token token = instance.getTokens().iterator().next();
        assertAwaitingSignal( token, GraphFactory.SIGNAL );
        assertActiveWorkItems( instance, 1 );

        completeWorkItemOnToken( engine, token );
        assertActiveTokens( instance, 0 );
        assertPath( instance, "1,3" );
        assertActiveWorkItems( instance, 0 );
        Assert.assertEquals( EVENTS_1WI, listener.getEvents() );
    }

    @Test
    public void two(){
        RecordEventsListener listener = new RecordEventsListener();
        GraphEngine engine = createEngine( listener );
        Graph graph = GraphFactory.INSTANCE.signal_two();

        GraphInstance instance = engine.start( graph, null );
        assertActiveTokens( instance, 1 );
        Token token = instance.getTokens().iterator().next();
        assertAwaitingSignal( token, GraphFactory.SIGNAL );
        assertActiveWorkItems( instance, 1 );

        completeWorkItemOnToken( engine, token );
        assertActiveTokens( instance, 1 );
        assertAwaitingSignal( token, GraphFactory.SIGNAL );
        assertActiveWorkItems( instance, 1 );

        completeWorkItemOnToken( engine, token );
        assertActiveTokens( instance, 0 );
        assertActiveWorkItems( instance, 0 );
        Assert.assertEquals( EVENTS_2WI_SERIAL, listener.getEvents() );
    }

    @Test
    public void parallel(){
        RecordEventsListener listener = new RecordEventsListener();
        GraphEngine engine = createEngine( listener );
        Graph graph = GraphFactory.INSTANCE.signal_parallel_pre_post();

        GraphInstance instance = engine.start( graph, null );
        assertActiveTokens( instance, 3 );
        Token token2 = getTokenById( instance.getTokens(), 2 );
        Token token3 = getTokenById( instance.getTokens(), 3 );
        assertAwaitingSignal( token2, GraphFactory.SIGNAL );
        assertAwaitingSignal( token3, GraphFactory.SIGNAL );
        assertActiveWorkItems( instance, 2 );
        assertPath( instance, "1" );

        completeWorkItemOnToken( engine, token2 );
        assertAwaitingSignal( token3, GraphFactory.SIGNAL );
        assertActiveWorkItems( instance, 1 );
        assertPath( instance, "1" );

        completeWorkItemOnToken( engine, token3 );
        assertActiveTokens( instance, 0 );
        assertActiveWorkItems( instance, 0 );
        assertPath( instance, "1,6" );
        Assert.assertEquals( EVENTS_2WI_PARALLEL, listener.getEvents() );
    }

}
