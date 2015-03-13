package ee.telekom.workflow.graph;

/**
 * A {@link Transition} describes the order of execution between {@link Node}s.
 * They are directed links between nodes. During the execution of a
 * {@link GraphInstance}, transitions will determine the path tokens proceed
 * between nodes.
 * <p>
 * Transitions may be labelled with a name. Their name can be used for flow control.
 */
public interface Transition {

	public static final String DEFAULT_TRANSITION_NAME = null;

	/**
	 * Returns the {@link Node} at the start of the transition.
	 * 
	 * @return the {@link Node} at the start of the transition
	 */
	Node getStartNode();

	/**
	 * Returns the {@link Node} at the end of the transition.
	 * 
	 * @return the {@link Node} at the end of the transition
	 */
	Node getEndNode();

	/**
	 * Returns the transition name. Nulls and blanks are allowed.
	 * 
	 * @return the transition name
	 */
	String getName();

}