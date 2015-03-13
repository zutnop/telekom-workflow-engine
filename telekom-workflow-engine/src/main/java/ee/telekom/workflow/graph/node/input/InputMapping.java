package ee.telekom.workflow.graph.node.input;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphInstance;

/**
 * An InputMapping describes an input parameter to a node execution. The value of this parameter is evaluated dynamically 
 * and may depend on the {@link Environment} of the {@link GraphInstance} being executed.
 */
public interface InputMapping<T> {

    T evaluate( GraphInstance instance );

}
