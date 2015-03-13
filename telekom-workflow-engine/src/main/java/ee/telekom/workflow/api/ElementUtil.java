package ee.telekom.workflow.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ee.telekom.workflow.api.Element.Type;
import ee.telekom.workflow.graph.el.ElUtil;
import ee.telekom.workflow.graph.node.gateway.condition.Condition;
import ee.telekom.workflow.graph.node.gateway.condition.ExpressionLanguageCondition;
import ee.telekom.workflow.graph.node.input.ConstantMapping;
import ee.telekom.workflow.graph.node.input.ExpressionLanguageMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.input.MapMapping;
import ee.telekom.workflow.graph.node.output.MapEntryMapping;
import ee.telekom.workflow.graph.node.output.OutputMapping;
import ee.telekom.workflow.graph.node.output.ValueMapping;

/**
 * Utility class providing a number of convenience methods for handling {@link Element}s.
 */
public class ElementUtil{

    /**
     * Creates an {@link OutputMapping} based on an output element.
     */
    public static OutputMapping createOutputMapping( Element outputElement ){
        if( outputElement == null ){
            return null;
        }
        switch( outputElement.getType() ) {
            case VARIABLE:
                return new ValueMapping( outputElement.getString( 0 ) );
            case VARIABLES:
                Map<String, String> mappings = new LinkedHashMap<>();
                for( String mapping : outputElement.getStringArray( 0 ) ){
                    String[] pair = mapping.split( "=" );
                    mappings.put( pair[1], pair[0] );
                }
                return new MapEntryMapping( mappings );
            default :
                throw new IllegalStateException( "No strategy defined for creating an OutputMapping based on: " + outputElement );
        }
    }

    /**
     * Creates an array of {@link InputMapping}s based on an array of arguments.
     * Uses {@link ElementUtil#createInputMapping(Object)} for every argument.
     */
    public static InputMapping<?>[] createArrayMapping( Object[] arguments ){
        if( arguments == null || arguments.length == 0 ){
            return null;
        }
        List<InputMapping<?>> list = new ArrayList<>();
        for( Object argument : arguments ){
            list.add( createInputMapping( argument ) );
        }
        return list.toArray( new InputMapping<?>[arguments.length] );

    }

    /**
     * Creates a {@link MapMapping} based on the list of input elements. 
     * Only supports {@link Type#ATTRIBUTE} elements.
     */
    public static MapMapping createMapMapping( List<Element> inputElements ){
        if( inputElements == null || inputElements.isEmpty() ){
            return null;
        }
        MapMapping result = new MapMapping();
        for( Element element : inputElements ){
            String name = element.getString( 1 );
            result.addEntryMapping( name, createInputMapping( element.getObject( 0 ) ) );
        }
        return result;
    }

    /**
     * Creates a {@link Condition} based on the given conditional element. Supported types are
     * {@link Type#IF}, {@link Type#ELSE_IF}, {@link Type#ELSE}, {@link Type#BRANCH}. The method
     * works based on the fact that IF and ELSE_IF have exactly one argument, ELSE has no arguments,
     * BRANCH has either zero or one arguments 
     */
    public static Condition createCondition( Element element ){
        if( element.countArguments() > 0 ){
            return new ExpressionLanguageCondition( element.getString( 0 ) );
        }
        else{
            return null;
        }
    }

    /**
     * Returns, whether the given element is an output element. 
     * Remember, a {@link Row} has a syntax of: [OutputElement] MainElement [InputElement]*.
     */
    public static boolean isOutputElement( Element element ){
        switch( element.getType() ) {
            case VARIABLE:
            case VARIABLES:
                return true;
            default :
                return false;
        }
    }

    /**
     * Returns, whether the given element is a main element. 
     * Remember, a {@link Row} has a syntax of: [OutputElement] MainElement [InputElement]*.
     */
    public static boolean isMainElement( Element element ){
        switch( element.getType() ) {
            case START:
            case END:
            case VALUE:
            case CALL:
            case CALL_ASYNC:
            case HUMAN_TASK:
            case WAIT_SIGNAL:
            case WAIT_TIMER:
            case WAIT_UNTIL_DATE:
            case CREATE_INSTANCE:
            case WHILE_DO_BEGIN:
            case WHILE_DO_END:
            case DO_WHILE_BEGIN:
            case DO_WHILE_END:
            case IF:
            case ELSE_IF:
            case ELSE:
            case END_IF:
            case SPLIT:
            case BRANCH:
            case JOIN_FIRST:
            case JOIN_ALL:
            case ESCALATE:
            case VALIDATE_INPUT_VARIABLE:
                return true;
            default :
                return false;
        }
    }

    /**
     * Returns, whether the given element is an input element. 
     * Remember, a {@link Row} has a syntax of: [OutputElement] MainElement [InputElement]*.
     */
    public static boolean isInputElement( Element element ){
        return Type.ATTRIBUTE.equals( element.getType() );
    }

    ///// PRIVATE METHODS /////

    /**
     * Creates an {@link InputMapping} based on a argument.
     * @return {@link ExpressionLanguageMapping} for string arguments of style "${expression}", 
     *         or a {@link ConstantMapping} otherwise.
     */
    private static InputMapping<Object> createInputMapping( Object value ){
        if( value instanceof String ){
            String text = (String)value;
            if( ElUtil.hasBrackets( text ) ){
                return new ExpressionLanguageMapping<Object>( text );
            }
        }
        return ConstantMapping.of( value );
    }

}
