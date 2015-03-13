package ee.telekom.workflow.graph.node.expression;

/**
 * Interface for an expression that can be executed with optional arguments and may return a value.
 */
public interface Expression<T> {

    T execute( Object... arguments );

}
