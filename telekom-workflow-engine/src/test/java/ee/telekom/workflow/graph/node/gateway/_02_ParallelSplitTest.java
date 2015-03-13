package ee.telekom.workflow.graph.node.gateway;

import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.GraphFactory;

public class _02_ParallelSplitTest extends AbstractGraphTest {

	@Test
	public void one_branch() {
		assertExecution(GraphFactory.INSTANCE.split_one(), "2");
	}

	@Test
	public void two_branches() {
		assertExecution(GraphFactory.INSTANCE.split_two(), "2,3");
	}

	@Test
	public void three_branches() {
		assertExecution(GraphFactory.INSTANCE.split_three(), "2,3,4");
	}

	@Test
	public void two_branches_with_prefix() {
		assertExecution(GraphFactory.INSTANCE.split_two_pre(), "1,3,4");
	}

}
