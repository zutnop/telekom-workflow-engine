package ee.telekom.workflow.graph;

/**
 * Listener interface for events of tokens moving through within a {@link Graph}
 */
public interface NodeEventListener {

	/**
	 * Called before a token enters a node, i.e. before it begins executing on
	 * the given node
	 * 
	 * @param token
	 *            the token to enter the node
	 * @param node
	 *            the node to be entered
	 */
	void onEntering(Token token, Node node);

	/**
	 * Called after a token left a node, i.e. after the execution on the given
	 * node completed
	 * 
	 * @param token
	 *            the token that left the node
	 * @param node
	 *            the node that was left
	 */
	void onLeft(Token token, Node node);

}
