package ee.telekom.workflow.graph;

import ee.telekom.workflow.graph.node.activity.CreateNewInstanceActivity;

/**
 * Creates a new instance as defined by the arguments. Used to create instances for {@link CreateNewInstanceActivity}
 */
public interface NewGraphInstanceCreator{

    void create( String graphName, Integer graphVersion, String label1, String label2, Environment initialEnvironment );

}
