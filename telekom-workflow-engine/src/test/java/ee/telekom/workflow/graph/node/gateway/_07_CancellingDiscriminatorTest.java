package ee.telekom.workflow.graph.node.gateway;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.RecordEventsListener;
import ee.telekom.workflow.graph.Token;

public class _07_CancellingDiscriminatorTest extends AbstractGraphTest{

    private static final List<String> EVENTS_1ST_SIGNAL = Collections
            .unmodifiableList( Arrays.asList(
                    RecordEventsListener.INSTANCE_CREATED,
                    RecordEventsListener.WORK_ITEM_CREATED + " 4",
                    RecordEventsListener.WORK_ITEM_CREATED + " 5",
                    RecordEventsListener.WORK_ITEM_CREATED + " 6",
                    RecordEventsListener.WORK_ITEM_CREATED + " 7",
                    RecordEventsListener.INSTANCE_STARTED,
                    RecordEventsListener.WORK_ITEM_COMPLETED + " 4",
                    RecordEventsListener.WORK_ITEM_CANCELLED + " 5",
                    RecordEventsListener.WORK_ITEM_CANCELLED + " 6",
                    RecordEventsListener.WORK_ITEM_CANCELLED + " 7",
                    RecordEventsListener.INSTANCE_COMPLETED ) );

    private static final List<String> EVENTS_2ND_SIGNAL = Collections
            .unmodifiableList( Arrays.asList(
                    RecordEventsListener.INSTANCE_CREATED,
                    RecordEventsListener.WORK_ITEM_CREATED + " 4",
                    RecordEventsListener.WORK_ITEM_CREATED + " 5",
                    RecordEventsListener.WORK_ITEM_CREATED + " 6",
                    RecordEventsListener.WORK_ITEM_CREATED + " 7",
                    RecordEventsListener.INSTANCE_STARTED,
                    RecordEventsListener.WORK_ITEM_COMPLETED + " 5",
                    RecordEventsListener.WORK_ITEM_CANCELLED + " 4",
                    RecordEventsListener.WORK_ITEM_CANCELLED + " 6",
                    RecordEventsListener.WORK_ITEM_CANCELLED + " 7",
                    RecordEventsListener.INSTANCE_COMPLETED ) );

    private static final List<String> EVENTS_3RD_SIGNAL = Collections
            .unmodifiableList( Arrays.asList(
                    RecordEventsListener.INSTANCE_CREATED,
                    RecordEventsListener.WORK_ITEM_CREATED + " 4",
                    RecordEventsListener.WORK_ITEM_CREATED + " 5",
                    RecordEventsListener.WORK_ITEM_CREATED + " 6",
                    RecordEventsListener.WORK_ITEM_CREATED + " 7",
                    RecordEventsListener.INSTANCE_STARTED,
                    RecordEventsListener.WORK_ITEM_COMPLETED + " 6",
                    RecordEventsListener.WORK_ITEM_CANCELLED + " 7",
                    RecordEventsListener.WORK_ITEM_CANCELLED + " 4",
                    RecordEventsListener.WORK_ITEM_CANCELLED + " 5",
                    RecordEventsListener.INSTANCE_COMPLETED ) );

    private static final List<String> EVENTS_4TH_SIGNAL = Collections
            .unmodifiableList( Arrays.asList(
                    RecordEventsListener.INSTANCE_CREATED,
                    RecordEventsListener.WORK_ITEM_CREATED + " 4",
                    RecordEventsListener.WORK_ITEM_CREATED + " 5",
                    RecordEventsListener.WORK_ITEM_CREATED + " 6",
                    RecordEventsListener.WORK_ITEM_CREATED + " 7",
                    RecordEventsListener.INSTANCE_STARTED,
                    RecordEventsListener.WORK_ITEM_COMPLETED + " 7",
                    RecordEventsListener.WORK_ITEM_CANCELLED + " 6",
                    RecordEventsListener.WORK_ITEM_CANCELLED + " 4",
                    RecordEventsListener.WORK_ITEM_CANCELLED + " 5",
                    RecordEventsListener.INSTANCE_COMPLETED ) );

    @Test
    public void one(){
        assertExecution( GraphFactory.INSTANCE.cancelling_discriminator_one(),
                "2" );
    }

    @Test
    public void one_post(){
        assertExecution(
                GraphFactory.INSTANCE.cancelling_discriminator_one_post(),
                "2,4" );
    }

    @Test
    public void two(){
        assertExecution( GraphFactory.INSTANCE.cancelling_discriminator_two(),
                "2" );
    }

    @Test
    public void two_firstBranchEmpty(){
        assertExecution(
                GraphFactory.INSTANCE
                        .cancelling_discriminator_two_firstBranchEmpty(),
                null );
    }

    @Test
    public void two_secondBranchEmpty(){
        assertExecution(
                GraphFactory.INSTANCE
                        .cancelling_discriminator_two_secondBranchEmpty(),
                "2" );
    }

    @Test
    public void two_before_after(){
        assertExecution(
                GraphFactory.INSTANCE.cancelling_discriminator_two_pre_post(),
                "1,3,6" );
    }

    @Test
    public void firstBranchEmpty_pre_post(){
        assertExecution(
                GraphFactory.INSTANCE
                        .cancelling_discriminator_two_firstBranchEmpty_pre_post(),
                "1,5" );
    }

    @Test
    public void secondBranchEmpty_pre_post(){
        assertExecution(
                GraphFactory.INSTANCE
                        .cancelling_discriminator_two_secondBranchEmpty_pre_post(),
                "1,3,6" );
    }

    @Test
    public void twice(){
        assertExecution( GraphFactory.INSTANCE.cancelling_discriminator_twice(),
                "1,3,6,8,11" );
    }

    @Test
    public void and_nested(){
        run_nested_test(
                GraphFactory.INSTANCE.cancelling_discriminator_and_nested(), 4,
                "1,3,9,8,16", EVENTS_1ST_SIGNAL );
        run_nested_test(
                GraphFactory.INSTANCE.cancelling_discriminator_and_nested(), 5,
                "1,3,9,8,16", EVENTS_2ND_SIGNAL );
        run_nested_test(
                GraphFactory.INSTANCE.cancelling_discriminator_and_nested(), 6,
                "1,3,9,14,16", EVENTS_3RD_SIGNAL );
        run_nested_test(
                GraphFactory.INSTANCE.cancelling_discriminator_and_nested(), 7,
                "1,3,9,14,16", EVENTS_4TH_SIGNAL );
    }

    @Test
    public void or_nested(){
        run_nested_test(
                GraphFactory.INSTANCE.cancelling_discriminator_or_nested(), 4,
                "1,3,9,8,16", EVENTS_1ST_SIGNAL );
        run_nested_test(
                GraphFactory.INSTANCE.cancelling_discriminator_or_nested(), 5,
                "1,3,9,8,16", EVENTS_2ND_SIGNAL );
        run_nested_test(
                GraphFactory.INSTANCE.cancelling_discriminator_or_nested(), 6,
                "1,3,9,14,16", EVENTS_3RD_SIGNAL );
        run_nested_test(
                GraphFactory.INSTANCE.cancelling_discriminator_or_nested(), 7,
                "1,3,9,14,16", EVENTS_4TH_SIGNAL );
    }

    private void run_nested_test( Graph graph, int tokenId, String expectedPath,
                                  List<String> expectedEvents ){
        RecordEventsListener listener = new RecordEventsListener();
        GraphEngine engine = createEngine( listener );

        GraphInstance instance = engine.start( graph, null );
        assertActiveTokens( instance, 7 );
        assertActiveWorkItems( instance, 4 );
        Token token4 = getTokenById( instance.getTokens(), 4 );
        Token token5 = getTokenById( instance.getTokens(), 5 );
        Token token6 = getTokenById( instance.getTokens(), 6 );
        Token token7 = getTokenById( instance.getTokens(), 7 );
        assertAwaitingSignal( token4, "1" );
        assertAwaitingSignal( token5, "2" );
        assertAwaitingSignal( token6, "3" );
        assertAwaitingSignal( token7, "4" );

        completeWorkItemOnToken( engine,
                getTokenById( instance.getTokens(), tokenId ) );
        assertActiveTokens( instance, 0 );
        assertActiveWorkItems( instance, 0 );
        Assert.assertEquals( expectedEvents, listener.getEvents() );
        assertPath( instance, expectedPath );
    }
}
