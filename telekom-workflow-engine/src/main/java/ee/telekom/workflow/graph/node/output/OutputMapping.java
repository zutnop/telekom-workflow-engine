package ee.telekom.workflow.graph.node.output;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphInstance;

/**
 * An OutputMapping describes how the execution result of a node is mapped into the {@link Environment} 
 * of the {@link GraphInstance} being executed.
 */
public interface OutputMapping{

    void map( Environment environment, Object result );

}
