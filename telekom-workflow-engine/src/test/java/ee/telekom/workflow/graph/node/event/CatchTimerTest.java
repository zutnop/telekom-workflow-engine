package ee.telekom.workflow.graph.node.event;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.RecordEventsListener;
import ee.telekom.workflow.graph.Token;

public class CatchTimerTest extends AbstractGraphTest {

	@Test
	public void one() {
		RecordEventsListener listener = new RecordEventsListener();
		GraphEngine engine = createEngine(listener);
		Graph graph = GraphFactory.INSTANCE.timer_one();

		GraphInstance instance = engine.start(graph, null);
		assertActiveTokens(instance, 1);
		Token token = instance.getTokens().iterator().next();
		assertAwaitingTimer(token, GraphFactory.TIMER_MS);
		assertActiveWorkItems(instance, 1);

		completeWorkItemOnToken(engine, token);
		assertActiveTokens(instance, 0);
		assertActiveWorkItems(instance, 0);
		Assert.assertEquals(EVENTS_1WI, listener.getEvents());
	}

	@Test
	public void one_pre_post() {
		RecordEventsListener listener = new RecordEventsListener();
		GraphEngine engine = createEngine(listener);
		Graph graph = GraphFactory.INSTANCE.timer_one_pre_post();

		GraphInstance instance = engine.start(graph, null);
		assertActiveTokens(instance, 1);
		Token token = instance.getTokens().iterator().next();
		assertAwaitingTimer(token, GraphFactory.TIMER_MS);
		assertActiveWorkItems(instance, 1);

		completeWorkItemOnToken(engine, token);
		assertActiveTokens(instance, 0);
		assertActiveWorkItems(instance, 0);
		assertPath(instance, "1,3");
		Assert.assertEquals(EVENTS_1WI, listener.getEvents());
	}

	@Test
	public void two() {
		RecordEventsListener listener = new RecordEventsListener();
		GraphEngine engine = createEngine(listener);
		Graph graph = GraphFactory.INSTANCE.timer_two();

		GraphInstance instance = engine.start(graph, null);
		assertActiveTokens(instance, 1);
		Token token = instance.getTokens().iterator().next();
		assertAwaitingTimer(token, GraphFactory.TIMER_MS);
		assertActiveWorkItems(instance, 1);

		completeWorkItemOnToken(engine, token);
		assertActiveTokens(instance, 1);
		assertActiveWorkItems(instance, 1);
		assertAwaitingTimer(token, GraphFactory.TIMER_MS);

		completeWorkItemOnToken(engine, token);
		assertActiveTokens(instance, 0);
		assertActiveWorkItems(instance, 0);
		Assert.assertEquals(EVENTS_2WI_SERIAL, listener.getEvents());
	}

	@Test
	public void parallel() {
		RecordEventsListener listener = new RecordEventsListener();
		GraphEngine engine = createEngine(listener);
		Graph graph = GraphFactory.INSTANCE.timer_parallel_pre_post();

		GraphInstance instance = engine.start(graph, null);
		assertActiveTokens(instance, 3);
		Token token2 = getTokenById(instance.getTokens(), 2);
		Token token3 = getTokenById(instance.getTokens(), 3);
		assertAwaitingTimer(token2, GraphFactory.TIMER_MS);
		assertAwaitingTimer(token3, GraphFactory.TIMER_MS);
		assertActiveWorkItems(instance, 2);
		assertPath(instance, "1");

		completeWorkItemOnToken(engine, token2);
		assertAwaitingTimer(token3, GraphFactory.TIMER_MS);
		assertActiveWorkItems(instance, 1);
		assertPath(instance, "1");

		completeWorkItemOnToken(engine, token3);
		assertActiveTokens(instance, 0);
		assertActiveWorkItems(instance, 0);
		assertPath(instance, "1,6");
		Assert.assertEquals(EVENTS_2WI_PARALLEL, listener.getEvents());
	}

}
