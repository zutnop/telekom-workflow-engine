package ee.telekom.workflow.graph.node.gateway;

import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.GraphFactory;

public class _05_SimpleMergeTest extends AbstractGraphTest {

	@Test
	public void one() {
		assertConditionalExecution(GraphFactory.INSTANCE.simplemerge_one(),
				null, "2");
	}

	@Test
	public void one_post() {
		assertConditionalExecution(
				GraphFactory.INSTANCE.simplemerge_one_post(), null, "2,4");
	}

	@Test
	public void two() {
		assertConditionalExecution(GraphFactory.INSTANCE.simplemerge_two(),
				GraphFactory.VALUE1, "2");

		assertConditionalExecution(GraphFactory.INSTANCE.simplemerge_two(),
				null, "3");
	}

	@Test
	public void two_post() {
		assertConditionalExecution(
				GraphFactory.INSTANCE.simplemerge_two_post(),
				GraphFactory.VALUE1, "2,5");

		assertConditionalExecution(
				GraphFactory.INSTANCE.simplemerge_two_post(), null, "3,5");
	}

}
