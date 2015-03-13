package ee.telekom.workflow.graph.node.activity;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.node.AbstractNode;

/**
 * Activity executing a given {@link Script}. The script is executed synchronously.
 */
public class ScriptActivity extends AbstractNode{

    private Script script;

    public ScriptActivity( int id, Script script ){
        this( id, null, script );
    }

    public ScriptActivity( int id, String name, Script script ){
        super( id, name );
        this.script = script;
    }
    
    public Script getScript() {
        return script;
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        script.execute( token.getInstance().getEnvironment() );
        engine.complete( token, null );
    }

    @Override
    public void cancel( GraphEngine engine, Token token ){
        // Tokens cannot "wait" at this kind of node since the execution
        // is synchronous. Hence, no "cancel" action is required.
    }

    @Override
    public void store( Environment environment, Object result ){
        // This type of node does not produce a result
    }

    public static interface Script{
        void execute( Environment environment );
    }

}
