package ee.telekom.workflow.graph.node.activity;

import javax.el.ELProcessor;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.WorkflowException;
import ee.telekom.workflow.graph.el.ElUtil;
import ee.telekom.workflow.graph.node.AbstractNode;

/**
 * Activity validating an attribute in the {@link GraphInstance}'s {@link Environment}.
 *
 * For a required attribute, it checks that the attribute's value is set in the environment (possibly also to <code>null</code>)
 * and that this value is assignable to the defined type.
 * For an optional attribute, it initialises the value to a default, if it is not yet set in the environment and ensures that
 * its value is assignable to the defined type.
 */
public class ValidateAttributeActivity extends AbstractNode{

    private String attribute;
    private boolean isRequired;
    private Class<?> type;
    private Object defaultValue;

    public ValidateAttributeActivity( int id, String attribute, Class<?> type ){
        this( id, null, attribute, type, true, null );
    }

    public ValidateAttributeActivity( int id, String attribute, Class<?> type, boolean isRequired ){
        this( id, null, attribute, type, isRequired, null );

    }

    public ValidateAttributeActivity( int id, String attribute, Class<?> type, boolean isRequired, Object defaultValue ){
        this( id, null, attribute, type, isRequired, defaultValue );
    }

    public ValidateAttributeActivity( int id, String name, String attribute, Class<?> type, boolean isRequired, Object defaultValue ){
        super( id, name );
        this.attribute = attribute;
        this.type = type;
        this.isRequired = isRequired;
        this.defaultValue = defaultValue;
    }

    public String getAttribute(){
        return attribute;
    }

    public boolean isRequired(){
        return isRequired;
    }

    public Class<?> getType(){
        return type;
    }

    public Object getDefaultValue(){
        return defaultValue;
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        GraphInstance instance = token.getInstance();
        Environment environment = instance.getEnvironment();

        if( !environment.containsAttribute( attribute ) ){
            if( isRequired ){
                // Throw exception
                throw new WorkflowException( "Missing required attribute '" + attribute + "'" );
            }
            else{
                // Use default value
                if( defaultValue instanceof String && ElUtil.hasBrackets( (String)defaultValue ) ){
                    ELProcessor processor = ElUtil.initNewELProcessor( environment, instance.getExternalId() );
                    Object expressionResult = processor.eval( ElUtil.removeBrackets( (String)defaultValue ) );
                    environment.setAttribute( attribute, expressionResult );
                }
                else{
                    environment.setAttribute( attribute, defaultValue );
                }
            }
        }

        // Validate type of value
        Object value = environment.getAttribute( attribute );
        if( value != null && !type.isAssignableFrom( value.getClass() ) ){
            throw new WorkflowException( "The value of attribute '" + attribute
                    + "' is of type " + value.getClass().getCanonicalName()
                    + " whis is not assignable to the expected type " + type.getCanonicalName() );
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
