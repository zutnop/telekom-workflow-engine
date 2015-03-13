package ee.telekom.workflow.graph;

import java.util.Set;

/**
 * The {@link GraphRepository} is a container of {@link Graph} definitions.
 */
public interface GraphRepository{

    /**
     * Returns the {@link Graph} with the given name and the given version, 
     * or the latest graph with the given name if no version is <code>null</code>, 
     * or <code>null</code> if no such graph is found.<br>
     * The latest graph is the graph with the greatest version number.
     * 
     * @param name
     *            the graph name
     * @param version
     *            the graph version
     * @return the {@link Graph} with the given name and the given version, or
     *         <code>null</code> if no suitable graph is found.
     */
    Graph getGraph( String name, Integer version );

    /**
     * Returns all graphs with the given name ordered with decreasing version
     * number.
     * 
     * @param name
     *            the graph name
     * @return all graphs with the given name ordered with decreasing version
     *         number.
     */
    Set<Graph> getGraphs( String name );

    /**
     * Returns all graphs accessible via this repository.
     * 
     * @return all graphs accessible via this repository
     */
    Set<Graph> getGraphs();

    /**
     * Adds a graph to the repository and overwrites any previously defined
     * graph with identical name and version.
     * 
     * @param graph
     *            the graph to be added
     */
    void addGraph( Graph graph );

}