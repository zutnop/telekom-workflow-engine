package ee.telekom.workflow.graph.node.input;

import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.node.event.CatchTimer;
import ee.telekom.workflow.graph.node.expression.Expression;

/**
 * InputMapping for a value evaluated by the execution of the given expression.
 * <p>
 * NB! This is a home-built Expression support class (can be used by implementing the Expression interface), it doesn't have anything to 
 * do with the Expression Language support (@see ExpressionLanguageMapping). At the time of writing, this InputMapping is actually not 
 * usable via the workflow engine API (DSL).
 * <p>
 * A possible usage of this class is to dynamically evaluate the delay time of a
 * {@link CatchTimer} event node with custom logic based on {@link GraphInstance} 
 * attribute's (e.g. a client id). 
 */
public class ExpressionMapping<T> implements InputMapping<T>{

    private Expression<T> expression;
    private ArrayMapping argumentMapping;

    public ExpressionMapping( Expression<T> expression, InputMapping<?>... argumentMappings ){
        this.expression = expression;
        this.argumentMapping = new ArrayMapping( argumentMappings );
    }

    public Expression<T> getExpression(){
        return expression;
    }

    public ArrayMapping getArgumentMapping(){
        return argumentMapping;
    }

    @Override
    public T evaluate( GraphInstance instance ){
        Object[] arguments = argumentMapping.evaluate( instance );
        return expression.execute( arguments );
    }

}
