package ee.telekom.workflow.graph.node.event;

import java.util.Date;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.node.AbstractNode;
import ee.telekom.workflow.graph.node.input.ConstantMapping;
import ee.telekom.workflow.graph.node.input.DueDateMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;

/**
 * Catch event, pausing execution for a given amount of time.
 */
public class CatchTimer extends AbstractNode{

    private InputMapping<Date> dueDateMapping;

    // 4 constructors based on delay

    public CatchTimer( int id, long delayMillis ){
        this( id, new DueDateMapping( delayMillis ) );
    }

    public CatchTimer( int id, String name, long delayMillis ){
        this( id, name, new DueDateMapping( delayMillis ) );
    }

    // changed order of arguments for java compiler (otherwise we have an ambiguity in the erasure of the method signature)
    public CatchTimer( InputMapping<Long> delayMillisMapping, int id ){
        this( id, new DueDateMapping( delayMillisMapping ) );
    }

    // changed order of arguments for java compiler (otherwise we have an ambiguity in the erasure of the method signature)
    public CatchTimer( InputMapping<Long> delayMillisMapping, int id, String name ){
        this( id, name, new DueDateMapping( delayMillisMapping ) );
    }

    // 4 constructors based on due date

    public CatchTimer( int id, Date dueDate ){
        this( id, ConstantMapping.of( dueDate ) );
    }

    public CatchTimer( int id, String name, Date dueDate ){
        this( id, name, ConstantMapping.of( dueDate ) );
    }

    public CatchTimer( int id, InputMapping<Date> dueDateMapping ){
        super( id );
        this.dueDateMapping = dueDateMapping;
    }

    public CatchTimer( int id, String name, InputMapping<Date> dueDateMapping ){
        super( id, name );
        this.dueDateMapping = dueDateMapping;
    }

    public InputMapping<Date> getDueDateMapping(){
        return dueDateMapping;
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        GraphInstance instance = token.getInstance();
        Date dueDate = dueDateMapping.evaluate( instance );
        engine.addTimerItem( instance, token, dueDate );
    }

    @Override
    public void cancel( GraphEngine engine, Token token ){
        engine.cancelWorkItem( token );
    }

    @Override
    public void store( Environment environment, Object result ){
        // This type of node does not produce or expect a result
    }

}
