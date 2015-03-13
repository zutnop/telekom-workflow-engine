package ee.telekom.workflow.graph.node.activity;

import javax.el.ELProcessor;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.el.ElUtil;
import ee.telekom.workflow.graph.node.AbstractNode;

/**
 * Activity setting an attribute in the {@link GraphInstance}'s {@link Environment} to a given value.
 * 
 * If the given value is a String and contains an EL expression ${expression} then the expression is 
 * first evaluated and the result is stored into the Environment. 
 */
public class SetAttributeActivity extends AbstractNode{

    private String attribute;
    private Object value;

    public SetAttributeActivity( int id, String attribute, Object value ){
        this( id, null, attribute, value );
    }

    public SetAttributeActivity( int id, String name, String attribute, Object value ){
        super( id, name );
        this.attribute = attribute;
        this.value = value;
    }

    public String getAttribute(){
        return attribute;
    }

    public Object getValue(){
        return value;
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        GraphInstance instance = token.getInstance();
        Environment environment = instance.getEnvironment();
        if( value instanceof String && ElUtil.hasBrackets( (String)value ) ){
            ELProcessor processor = ElUtil.initNewELProcessor( environment, instance.getExternalId() );
            Object expressionResult = processor.eval( ElUtil.removeBrackets( (String)value ) );
            environment.setAttribute( attribute, expressionResult );
        }
        else{
            environment.setAttribute( attribute, value );
        }
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

}
