package ee.telekom.workflow.graph.node.gateway;

import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.GraphFactory;

public class _04_ExclusiveChoiceTest extends AbstractGraphTest {

	@Test
	public void one() {
		assertConditionalExecution(GraphFactory.INSTANCE.exclusivechoice_one(),
				GraphFactory.VALUE1, "2");

		assertConditionalExecution(GraphFactory.INSTANCE.exclusivechoice_one(),
				null, null);
	}

	@Test
	public void two() {
		assertConditionalExecution(GraphFactory.INSTANCE.exclusivechoice_two(),
				GraphFactory.VALUE1, "2");

		assertConditionalExecution(GraphFactory.INSTANCE.exclusivechoice_two(),
				GraphFactory.VALUE2, "3");

		assertConditionalExecution(GraphFactory.INSTANCE.exclusivechoice_two(),
				null, null);
	}

	@Test
	public void two_expressionlanguage() {
	    assertConditionalExecution(GraphFactory.INSTANCE.exclusivechoice_two_expressionlanguage(),
	            GraphFactory.VALUE1, "2");
	    
	    assertConditionalExecution(GraphFactory.INSTANCE.exclusivechoice_two_expressionlanguage(),
	            GraphFactory.VALUE2, "3");
	    
	    assertConditionalExecution(GraphFactory.INSTANCE.exclusivechoice_two_expressionlanguage(),
	            null, null);
	}
	
	@Test
	public void defaultTest() {
		assertConditionalExecution(
				GraphFactory.INSTANCE.exclusivechoice_default(), null, "2");
	}

	@Test
	public void one_default() {
		assertConditionalExecution(
				GraphFactory.INSTANCE.exclusivechoice_one_default(),
				GraphFactory.VALUE1, "2");

		assertConditionalExecution(
				GraphFactory.INSTANCE.exclusivechoice_one_default(), null, "3");
	}

	@Test
	public void twoConditionsTrue() {
		assertConditionalExecution(
				GraphFactory.INSTANCE.exclusivechoice_twoConditionsTrue(),
				GraphFactory.VALUE1, "2");
	}

}
