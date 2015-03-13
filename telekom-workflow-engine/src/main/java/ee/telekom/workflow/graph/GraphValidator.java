package ee.telekom.workflow.graph;

import java.util.List;

/**
 * Validator interface for {@link Graph}s
 */
public interface GraphValidator {

	/**
	 * Validates the given graph and returns a list of validation errors. The
	 * list might be empty if no errors are found but will never be
	 * <code>null</code>.
	 * 
	 * @param graph
	 *            the graph to be validated
	 * @return a list of validation errors or an empty list if no errors are
	 *         found
	 */
	List<String> validate(Graph graph);

}
