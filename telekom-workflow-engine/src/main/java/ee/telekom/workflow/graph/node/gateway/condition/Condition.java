package ee.telekom.workflow.graph.node.gateway.condition;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.node.gateway.OrFork;
import ee.telekom.workflow.graph.node.gateway.XorFork;

/**
 * Condition that is evaluated based on an {@link Environment} of a {@link GraphInstance}. Conditions are used to model the conditional execution
 * of subsequent branches in {@link OrFork} and {@link XorFork}s.
 */
public interface Condition{

    boolean evaluate( GraphInstance instance );

}