package ee.telekom.workflow.graph.node.input;

import javax.el.ELProcessor;

import org.apache.commons.lang.StringUtils;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.el.ElUtil;

/**
 * Provides expression language based InputMapping against {@link Environment} variables.
 * 
 * http://docs.oracle.com/javaee/7/api/javax/el/package-summary.html
 * 
 * If the mapping uses an unknown variable "foobar" (which has not yet been set in the Environment), an exception will occur: 
 * "ELResolver cannot handle a null base Object with identifier 'foobar'"
 *
 * @author Erko Hansar
 */
public class ExpressionLanguageMapping<T> implements InputMapping<T>{

    private String expression;

    public ExpressionLanguageMapping( String expression ){
        this.expression = expression;
    }

    public String getExpression(){
        return expression;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T evaluate( GraphInstance instance ){
        if( StringUtils.isNotBlank( expression ) ){
            ELProcessor processor = ElUtil.initNewELProcessor( instance.getEnvironment(), instance.getExternalId() );
            Object expressionResult = processor.eval( ElUtil.removeBrackets( expression ) );
            return (T)expressionResult;
        }
        return null;
    }

}
