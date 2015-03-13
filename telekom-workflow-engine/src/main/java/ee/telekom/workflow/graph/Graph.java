package ee.telekom.workflow.graph;

import java.util.Collection;
import java.util.List;

/**
 * A {@link Graph} corresponds to a workflow definition. It has a set of
 * {@link Node}s, which correspond to actions that need to be taken for the
 * workflow to complete. It also has a set of {@link Transition}s which define
 * the order of execution between the nodes.
 */
public interface Graph {

	/**
	 * Returns the graph's name, which should be unique. Different versions with
	 * the same name may exist, but each of them should have a unique version
	 * number.
	 * 
	 * @return the graph's name
	 */
	String getName();

	/**
	 * Returns the version number of the graph. Different versions of a graph
	 * may exists. Newer versions of the graph should have higher version
	 * numbers than older versions. A graph can be uniquely identified by name
	 * and version number.
	 * 
	 * @return the graph's version
	 */
	int getVersion();

	/**
	 * Returns the graph's start node. The start node is the node where to start
	 * execution when creating a new {@link GraphInstance}. A non-empty graph must define
	 * a start node.
	 * 
	 * @return the node at which to start a workflow instance
	 */
	Node getStartNode();

	/**
	 * Returns the graph's node with the given id. Every node within a graph
	 * must be have a unique id.
	 * 
	 * @return the graph's node with the given id or <code>null</code> if the
	 *         graph does not contain a node with the given id.
	 */
	Node getNode(int id);

	/**
	 * Returns the list of all nodes in the graph.
	 * 
	 * @return list of all nodes in the graph
	 */
	Collection<Node> getNodes();

	/**
	 * Returns the list of all transitions in the graph.
	 * 
	 * @return list of all transitions in the graph
	 */
	List<Transition> getTransitions();

	/**
	 * Returns a list of transitions which have the given node as their starting
	 * point. The list may be empty but will never be <code>null</code>.
	 * 
	 * @param node
	 *            A node belonging to this graph
	 * @return A list of transitions
	 */
	List<Transition> getOutputTransitions(Node node);

	/**
	 * Returns the transition which has the given node as their starting point
	 * and the given name, or <code>null</code> if no such transition exists.
	 * 
	 * @param node
	 *            A node belonging to this graph
	 * @param name
	 *            A transition name
	 * @return A list of transitions
	 */
	Transition getOutputTransitions(Node node, String name);

}