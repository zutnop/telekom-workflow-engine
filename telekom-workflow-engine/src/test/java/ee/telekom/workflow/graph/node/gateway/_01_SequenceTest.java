package ee.telekom.workflow.graph.node.gateway;

import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.GraphFactory;

public class _01_SequenceTest extends AbstractGraphTest {

	@Test
	public void oneNode() {
		assertExecution(GraphFactory.INSTANCE.sequence_one(), "1");
	}

	@Test
	public void twoNodes() {
		assertExecution(GraphFactory.INSTANCE.sequence_two(), "1,2");
	}

	@Test
	public void threeNodes() {
		assertExecution(GraphFactory.INSTANCE.sequence_three(), "1,2,3");
	}

}
