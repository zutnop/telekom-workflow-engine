package ee.telekom.workflow.graph;

/**
 * A {@link Token} points to a {@link Node} in a {@link Graph}. The set of active tokens in a {@link GraphInstance} mark the graph instance's flow state.
 */
public interface Token {

	/**
	 * Returns the token's id. The id is unique among the token's in the
	 * containing graph instance.
	 * 
	 * @return the token's id
	 */
	int getId();

	/**
	 * Returns the {@link Node} that this token is pointing to.
	 * 
	 * @return the node that this token is pointing to
	 */
	Node getNode();

	/**
	 * Sets the {@link Node} that the token is pointing to.
	 * 
	 * @param node
	 *            the new node to point to
	 */
	void setNode(Node node);

	/**
	 * Returns the {@link GraphInstance} that this token belongs to.
	 * 
	 * @return the graph instance that this token belongs to
	 */
	GraphInstance getInstance();

	/**
	 * Returns the parent token which was involved in creating this token. If
	 * this node is a {@link GraphInstance}'s main token (the token that begins
	 * executing at the start node), <code>null</code> will be returned.
	 * 
	 * @return the token's parent token or <code>null</code> for a main token
	 */
	Token getParent();

	/**
	 * Marks this token as inactive, in the sense that it no longer represents
	 * an active part of the workflow instance. Once a token is marked inactive,
	 * it is generally only of historical interest.
	 * 
	 * A token may be marked inactive because its execution has completed or it
	 * has been cancelled.
	 */
	void markInactive();

	/**
	 * Returns whether the token is active.
	 * 
	 * @return true, if the token is active
	 */
	boolean isActive();

}