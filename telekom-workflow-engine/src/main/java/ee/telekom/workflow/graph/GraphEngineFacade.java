package ee.telekom.workflow.graph;

import ee.telekom.workflow.graph.core.GraphInstanceImpl;

/**
 * A {@link GraphEngineFacade} is the interface that external systems should use
 * to communicated with a graph engine.
 * 
 * <p>
 * 
 * An graph engine executes a workflow. A {@link Graph} specifies how it should
 * be executed and a {@link GraphInstance} tracks the current state of
 * execution. It is an graph engine which creates instances of
 * {@link GraphInstance} and {@link Token}.
 * 
 * Engines must be thread-safe. Thus implementations of interface may be safely
 * shared among threads.
 */
public interface GraphEngineFacade{

    /**
     * A shorthand for {@link #start(String, Integer, Environment, String)} using 
     * the latest graph with the given name and an empty initial environment and
     * a <code>null</code> external id.
     */
    GraphInstance start( String graphName );

    /**
     * A shorthand for {@link #start(String, Integer, Environment, String)} using
     * an empty initial environment and a <code>null</code> external id.
     */
    GraphInstance start( String graphName, Integer version );

    /**
     * A shorthand for {@link #start(String, Integer, Environment, String)} using a
     * <code>null</code> external id.
     */
    GraphInstance start( String graphName, Integer version, Environment initialEnvironment );

    /**
     * Starts a {@link GraphInstance} of the {@link Graph} with the given name
     * and version and using the given {@link Environment} and associates the created
     * {@link GraphInstance} with the given external. Is the equivalent of doing
     * 
     * <pre>
     * Graph graph = engine.getRepository().getLatestGraph(graphName);
     * GraphInstance graphInstance = engine.start(graph, initialEnvironment,
     * 		externalId);
     * </pre>
     * 
     * If no graph is found for the given graphName, a {@link WorkflowException}
     * will be thrown.
     * 
     * @param graphName
     *            the name of the graph to execute
     * @param version
     *            the version of the graph to execute of <code>null</code> for
     *            the latest graph with the given name.
     * @param initialEnvironment
     *            A starting environment for the {@link GraphInstance}. All
     *            attributes from this environment will be copied into the new
     *            graph instance's environment before execution starts.
     * @param externalId
     *            the external id that the graph instance is associated with
     * @throws WorkflowException
     *             If no graph is found with the given graphName
     * 
     * @return a {@link GraphInstance} of the latest {@link Graph} with the
     *         given name and using the given {@link Environment} which has
     *         already been started (and, given that no wait states were
     *         entered, is already completed).
     */
    GraphInstance start( String graphName, Integer version, Environment initialEnvironment, Long externalId );

    /**
     * A shorthand for {@link #start(Graph, Environment, String)} using an empty
     * initial environment and a <code>null</code> external id.
     */
    GraphInstance start( Graph graph );

    GraphInstance start( Graph graph, Environment initialEnvironment );

    /**
     * Starts a {@link GraphInstance} of the given {@link Graph} using the given
     * {@link Environment}. A {@link Token} will be generated for the start node
     * (determined by {@link Graph#getStartNode()}), and this token will be
     * executed. If the graph does not contain nodes which go into a wait state,
     * the {@link GraphInstance} returned will be completed.
     * <p>
     * Before the graph instance is started, the given initial environment will
     * be copied into the one of the new graph instance.
     * 
     * @param graph
     *            the graph to create a graph instance of
     * @param initialEnvironment
     *            an initial environment for the graph instance
     * @param externalId
     *            the external id that the graph instance is associated with
     */
    GraphInstanceImpl start( Graph graph, Environment initialEnvironment, Long externalId );

    /**
     * Aborts the given {@link GraphInstance}'s execution and cancels all
     * associated tokens and work items.
     * 
     * @param instance
     *            the graph instance to abort
     */
    void abort( GraphInstance instance );

    /**
     * Completes the executed work item and continues execution until another
     * wait state is reached or until the graph instance is completed.
     * 
     * @param workItem
     *            the work item to complete
     */
    void complete( GraphWorkItem workItem );

    /**
     * Returns the engine's {@link GraphRepository}.
     * 
     * @return the engine's {@link GraphRepository}
     */
    GraphRepository getRepository();

    /**
     * Returns the engine's {@link BeanResolver}, or <code>null</code>
     * if the engine does not support to resolve beans. 
     * 
     * @return the engine's {@link BeanResolver}, or <code>null</code>
     * if the engine does not support to resolve beans.
     */
    BeanResolver getBeanResolver();

    /**
     * Returns the engine's {@link NewGraphInstanceCreator}, or <code>null</code>
     * if the engine does not support to create sub graph instances.
     * 
     * @return the engine's {@link NewGraphInstanceCreator}, or <code>null</code>
     * if the engine does not support to create sub graph instances. 
     */
    NewGraphInstanceCreator getNewGraphInstanceCreator();

    /**
     * Adds a {@link GraphInstanceEventListener} to the engine.
     * 
     * @param listener
     *            listener to be added
     */
    void registerInstanceEventListener( GraphInstanceEventListener listener );

    /**
     * Adds a {@link GraphWorkItemEventListener} to the engine.
     * 
     * @param listener
     *            listener to be added
     */
    void registerWorkItemEventListener( GraphWorkItemEventListener listener );

    /**
     * Adds a {@link NodeEventListener} to the engine.
     * 
     * @param listener
     *            listener to be added
     */
    void registerNodeEventListener( NodeEventListener listener );

}