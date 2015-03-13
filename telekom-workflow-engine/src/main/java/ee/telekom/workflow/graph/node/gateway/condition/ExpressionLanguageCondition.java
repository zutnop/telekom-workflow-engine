package ee.telekom.workflow.graph.node.gateway.condition;

import javax.el.ELProcessor;

import org.apache.commons.lang.StringUtils;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.el.ElUtil;

/**
 * Provides expression language condition evaluation against {@link Environment} variables.
 * 
 * http://docs.oracle.com/javaee/7/api/javax/el/package-summary.html
 * 
 * If the condition uses an unknown variable "foobar" (which has not yet been set in the Environment), an exception will occur: 
 * "ELResolver cannot handle a null base Object with identifier 'foobar'"
 *
 * @author Erko Hansar
 */
public class ExpressionLanguageCondition implements Condition{

    private String condition;

    public ExpressionLanguageCondition( String condition ){
        this.condition = condition;
    }

    public String getCondition(){
        return condition;
    }

    @Override
    public boolean evaluate( GraphInstance instance ){
        boolean result = false;
        if( StringUtils.isNotBlank( condition ) ){
            ELProcessor processor = ElUtil.initNewELProcessor( instance.getEnvironment(), instance.getExternalId() );
            result = (boolean)processor.eval( condition );
        }
        return result;
    }

}
