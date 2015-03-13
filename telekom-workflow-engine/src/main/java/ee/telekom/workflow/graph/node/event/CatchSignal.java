package ee.telekom.workflow.graph.node.event;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.node.AbstractNode;
import ee.telekom.workflow.graph.node.input.ConstantMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.output.OutputMapping;

/**
 * Catch event, pausing execution until the arrival of a signal from an external source.
 */
public class CatchSignal extends AbstractNode{

    private InputMapping<String> signalMapping;
    private OutputMapping resultMapping;

    public CatchSignal( int id, String signal ){
        this( id, null, ConstantMapping.of( signal ), null );
    }

    public CatchSignal( int id, String name, String signal ){
        this( id, name, ConstantMapping.of( signal ), null );
    }

    public CatchSignal( int id, String name, String signal, OutputMapping resultMapping ){
        this( id, name, ConstantMapping.of( signal ), resultMapping );
    }

    public CatchSignal( int id, String signal, OutputMapping resultMapping ){
        this( id, null, ConstantMapping.of( signal ), resultMapping );
    }

    public CatchSignal( int id, String name, InputMapping<String> signal, OutputMapping resultMapping ){
        super( id, name );
        this.signalMapping = signal;
        this.resultMapping = resultMapping;
    }

    public InputMapping<String> getSignalMapping(){
        return signalMapping;
    }

    public OutputMapping getResultMapping(){
        return resultMapping;
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        GraphInstance instance = token.getInstance();
        String signal = signalMapping.evaluate( instance );
        engine.addSignalItem( instance, token, signal );
    }

    @Override
    public void cancel( GraphEngine engine, Token token ){
        engine.cancelWorkItem( token );
    }

    @Override
    public void store( Environment environment, Object result ){
        if( resultMapping != null ){
            resultMapping.map( environment, result );
        }
    }

}
