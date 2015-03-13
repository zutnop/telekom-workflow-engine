package ee.telekom.workflow.graph.node.event;

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

public class ThrowEscalationTest extends AbstractGraphTest {

	private static List<String> EVENTS_2 = Collections.unmodifiableList(Arrays
			.asList(RecordEventsListener.INSTANCE_CREATED,
					RecordEventsListener.WORK_ITEM_CREATED + " 2",
					RecordEventsListener.WORK_ITEM_CREATED + " 3",
					RecordEventsListener.WORK_ITEM_CREATED + " 4",
					RecordEventsListener.INSTANCE_STARTED,
					RecordEventsListener.WORK_ITEM_COMPLETED + " 2",
					RecordEventsListener.WORK_ITEM_CANCELLED + " 3",
					RecordEventsListener.WORK_ITEM_CANCELLED + " 4",
					RecordEventsListener.INSTANCE_COMPLETED));

	private static List<String> EVENTS_3 = Collections.unmodifiableList(Arrays
			.asList(RecordEventsListener.INSTANCE_CREATED,
					RecordEventsListener.WORK_ITEM_CREATED + " 2",
					RecordEventsListener.WORK_ITEM_CREATED + " 3",
					RecordEventsListener.WORK_ITEM_CREATED + " 4",
					RecordEventsListener.INSTANCE_STARTED,
					RecordEventsListener.WORK_ITEM_COMPLETED + " 3",
					RecordEventsListener.WORK_ITEM_CANCELLED + " 2",
					RecordEventsListener.WORK_ITEM_CANCELLED + " 4",
					RecordEventsListener.INSTANCE_COMPLETED));

	private static List<String> EVENTS_4 = Collections.unmodifiableList(Arrays
			.asList(RecordEventsListener.INSTANCE_CREATED,
					RecordEventsListener.WORK_ITEM_CREATED + " 2",
					RecordEventsListener.WORK_ITEM_CREATED + " 3",
					RecordEventsListener.WORK_ITEM_CREATED + " 4",
					RecordEventsListener.INSTANCE_STARTED,
					RecordEventsListener.WORK_ITEM_COMPLETED + " 4",
					RecordEventsListener.WORK_ITEM_CANCELLED + " 2",
					RecordEventsListener.WORK_ITEM_CANCELLED + " 3",
					RecordEventsListener.INSTANCE_COMPLETED));

	@Test
	public void test_simple() {
		run_test(2, EVENTS_2);
		run_test(3, EVENTS_3);
		run_test(4, EVENTS_4);
	}

	public void run_test(int tokenId, List<String> expectedEvents) {
		RecordEventsListener listener = new RecordEventsListener();
		GraphEngine engine = createEngine(listener);
		Graph graph = GraphFactory.INSTANCE.escalation_three();

		GraphInstance instance = engine.start(graph, null);
		assertActiveTokens(instance, 4);
		Token token2 = getTokenById(instance.getActiveTokens(), 2);
		Token token3 = getTokenById(instance.getActiveTokens(), 3);
		Token token4 = getTokenById(instance.getActiveTokens(), 4);
		assertAwaitingSignal(token2, "invoice");
		assertAwaitingSignal(token3, "payment");
		assertAwaitingSignal(token4, "unknown");
		assertActiveWorkItems(instance, 3);

		Token selectedToken = getTokenById(instance.getActiveTokens(), tokenId);
		completeWorkItemOnToken(engine, selectedToken);
		assertActiveTokens(instance, 0);
		assertActiveWorkItems(instance, 0);
		Assert.assertEquals(expectedEvents, listener.getEvents());
	}

}
