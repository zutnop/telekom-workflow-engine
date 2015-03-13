package ee.telekom.workflow.graph.core;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.RecordEventsListener;

public class NotificationTest extends AbstractGraphTest{

    @Test
    public void test_cancelling_discriminator_two_pre_post(){
        RecordEventsListener listener = new RecordEventsListener();
        GraphEngineImpl engine = new GraphEngineImpl();
        engine.registerInstanceEventListener( listener );
        engine.registerWorkItemEventListener( listener );
        engine.registerNodeEventListener( listener );
        Graph graph = GraphFactory.INSTANCE.cancelling_discriminator_two_pre_post();

        engine.start( graph, null );

        List<String> expectedEvents = Arrays.asList(
                RecordEventsListener.INSTANCE_CREATED,
                RecordEventsListener.NODE_ENTERING + " 1 1",
                RecordEventsListener.NODE_LEFT + " 1 1",
                RecordEventsListener.NODE_ENTERING + " 2 1",
                RecordEventsListener.NODE_LEFT + " 2 2",
                RecordEventsListener.NODE_LEFT + " 2 3",
                RecordEventsListener.NODE_ENTERING + " 3 2",
                RecordEventsListener.NODE_LEFT + " 3 2",
                RecordEventsListener.NODE_ENTERING + " -2 2",
                RecordEventsListener.NODE_LEFT + " -2 1",
                RecordEventsListener.NODE_ENTERING + " 6 1",
                RecordEventsListener.NODE_LEFT + " 6 1",
                RecordEventsListener.INSTANCE_STARTED,
                RecordEventsListener.INSTANCE_COMPLETED );

        Assert.assertEquals( expectedEvents, listener.getEvents() );
    }

    @Test
    public void test_synchronzation_two_pre_post(){
        RecordEventsListener listener = new RecordEventsListener();
        GraphEngineImpl engine = new GraphEngineImpl();
        engine.registerInstanceEventListener( listener );
        engine.registerWorkItemEventListener( listener );
        engine.registerNodeEventListener( listener );
        Graph graph = GraphFactory.INSTANCE.synchronzation_two_pre_post();

        engine.start( graph, null );

        List<String> expectedEvents = Arrays.asList(
                RecordEventsListener.INSTANCE_CREATED,
                RecordEventsListener.NODE_ENTERING + " 1 1",
                RecordEventsListener.NODE_LEFT + " 1 1",
                RecordEventsListener.NODE_ENTERING + " 2 1",
                RecordEventsListener.NODE_LEFT + " 2 2",
                RecordEventsListener.NODE_LEFT + " 2 3",
                RecordEventsListener.NODE_ENTERING + " 3 2",
                RecordEventsListener.NODE_LEFT + " 3 2",
                RecordEventsListener.NODE_ENTERING + " -2 2",
                RecordEventsListener.NODE_ENTERING + " 4 3",
                RecordEventsListener.NODE_LEFT + " 4 3",
                RecordEventsListener.NODE_ENTERING + " -2 3",
                RecordEventsListener.NODE_LEFT + " -2 1",
                RecordEventsListener.NODE_ENTERING + " 6 1",
                RecordEventsListener.NODE_LEFT + " 6 1",
                RecordEventsListener.INSTANCE_STARTED,
                RecordEventsListener.INSTANCE_COMPLETED );

        Assert.assertEquals( expectedEvents, listener.getEvents() );
    }
}
