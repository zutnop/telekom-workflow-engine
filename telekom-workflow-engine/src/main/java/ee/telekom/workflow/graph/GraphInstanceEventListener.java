package ee.telekom.workflow.graph;

/**
 * Listener interface for events in the life cycle of a {@link GraphInstance}.
 */
public interface GraphInstanceEventListener {

	/**
	 * Called after a new graph instance was created
	 * 
	 * @param instance
	 *            the newly created instance
	 */
	void onCreated(GraphInstance instance);

	/**
	 * Called after a graph instance was started.
	 * 
	 * @param instance
	 *            the started instance
	 */
	void onStarted(GraphInstance instance);

	/**
	 * Called before a graph instance is aborted
	 * 
	 * @param instance
	 *            the instance being aborted
	 */
	void onAborting(GraphInstance instance);

	/**
	 * Called after a graph instance was aborted
	 * 
	 * @param instance
	 *            the aborted instance
	 */
	void onAborted(GraphInstance instance);

	/**
	 * Called after a graph instance's execution completed
	 * 
	 * @param instance
	 *            the completed instance
	 */
	void onCompleted(GraphInstance instance);

}
