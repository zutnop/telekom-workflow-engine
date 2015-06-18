package ee.telekom.workflow.graph.el;

import java.util.Date;

import javax.el.ELProcessor;

import org.apache.commons.lang.StringUtils;

import ee.telekom.workflow.graph.Environment;

/**
 * Provides helper methods for Expression Language related operations
 * 
 * @author Erko Hansar
 */
public class ElUtil{

    /**
     * Checks if the given string starts with ${ and ends with } (the input is striped of whitespace first)
     */
    public static boolean hasBrackets( String condition ){
        String value = StringUtils.stripToEmpty( condition );
        return value.startsWith( "${" ) && value.endsWith( "}" );
    }

    /**
     * After trimming the input, removes two characters from the start and one from the end and returns the result.
     */
    public static String removeBrackets( String condition ){
        String value = StringUtils.strip( condition );
        if( value != null && value.length() >= 3 ){
            return value.substring( 2, value.length() - 1 );
        }
        return value;
    }

    /**
     * Creates and prepares a new ELProcessor instance with workflow engine configuration to evaluate expressions on
     * workflow instance environment together with some additional features (NOW variable, WORKFLOW_INSTANCE_ID variable).
     * 
     * The ELProcessor instance is meant to be used "quickly" only for current task execution/evaluation and should be discarded after that.
     */
    public static ELProcessor initNewELProcessor( Environment environment, Long externalInstanceId ){
        ELProcessor processor = new ELProcessor();
        processor.getELManager().addBeanNameResolver( new EnvironmentBeanNameResolver( environment ) );
        processor.setValue( ReservedVariables.NOW, new Date() );
        processor.setValue( ReservedVariables.WORKFLOW_INSTANCE_ID, externalInstanceId );
        return processor;
    }

    /**
     * Checks if the given variable name is a reserved keyword and SHOULD NOT be used as a key for an Environment attribute.
     */
    public static boolean isReservedVariable( String variableName ){
        return ReservedVariables.NOW.equalsIgnoreCase( variableName ) || ReservedVariables.WORKFLOW_INSTANCE_ID.equalsIgnoreCase( variableName );
    }

}
