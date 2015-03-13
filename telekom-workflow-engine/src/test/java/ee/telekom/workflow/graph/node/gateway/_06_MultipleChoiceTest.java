package ee.telekom.workflow.graph.node.gateway;

import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.GraphFactory;

public class _06_MultipleChoiceTest extends AbstractGraphTest {

	@Test
	public void one() {
		assertConditionalExecution(GraphFactory.INSTANCE.multiplechoice_one(),
				GraphFactory.VALUE1, "2");

		assertConditionalExecution(GraphFactory.INSTANCE.multiplechoice_one(),
				null, null);
	}

	@Test
	public void two() {
		assertConditionalExecution(GraphFactory.INSTANCE.multiplechoice_two(),
				GraphFactory.VALUE1, "2");

		assertConditionalExecution(GraphFactory.INSTANCE.multiplechoice_two(),
				GraphFactory.VALUE2, "3");

		assertConditionalExecution(GraphFactory.INSTANCE.multiplechoice_two(),
				null, null);
	}

	@Test
	public void defaultTest() {
		assertConditionalExecution(
				GraphFactory.INSTANCE.multiplechoice_default(), null, "2");
	}

	@Test
	public void one_default() {
		assertConditionalExecution(
				GraphFactory.INSTANCE.multiplechoice_one_default(),
				GraphFactory.VALUE1, "2,3");

		assertConditionalExecution(
				GraphFactory.INSTANCE.multiplechoice_one_default(), null, "3");
	}

	@Test
	public void twoConditionsTrue() {
		assertConditionalExecution(
				GraphFactory.INSTANCE.multiplechoice_twoConditionsTrue(),
				GraphFactory.VALUE1, "2,3");
	}

}
