package ee.telekom.workflow.graph.node.activity;

import ee.telekom.workflow.graph.BeanResolver;
import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.WorkflowException;
import ee.telekom.workflow.graph.node.AbstractNode;
import ee.telekom.workflow.graph.node.input.ArrayMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.output.OutputMapping;
import ee.telekom.workflow.util.CallUtil;

/**
 * Activity that is synchronously calling a method on the bean with the given name.
 */
public class BeanCallActivity extends AbstractNode{

    private String bean;
    private String method;
    private ArrayMapping argumentsMapping;
    private OutputMapping resultMapping;

    public BeanCallActivity( int id, String bean, String method, InputMapping<?>[] argumentsMappings, OutputMapping resultMapping ){
        this( id, null, bean, method, argumentsMappings, resultMapping );
    }

    public BeanCallActivity( int id, String name, String bean, String method, InputMapping<?>[] argumentsMappings, OutputMapping resultMapping ){
        super( id, name );
        this.bean = bean;
        this.method = method;
        this.argumentsMapping = new ArrayMapping( argumentsMappings );
        this.resultMapping = resultMapping;
    }

    public String getBean(){
        return bean;
    }

    public String getMethod(){
        return method;
    }

    public ArrayMapping getArgumentsMapping(){
        return argumentsMapping;
    }

    public OutputMapping getResultMapping(){
        return resultMapping;
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        GraphInstance instance = token.getInstance();
        Object target = getBeanResolver( engine ).getBean( bean );
        Object[] arguments = argumentsMapping.evaluate( instance );
        Object result = CallUtil.call( target, method, arguments );
        engine.complete( token, result );
    }

    @Override
    public void cancel( GraphEngine engine, Token token ){
        // Tokens cannot "wait" at this kind of node since the execution
        // is synchronous. Hence, no "cancel" action is required.
    }

    @Override
    public void store( Environment environment, Object result ){
        if( resultMapping != null ){
            resultMapping.map( environment, result );
        }
    }

    private BeanResolver getBeanResolver( GraphEngine engine ){
        if( engine.getBeanResolver() == null ){
            throw new WorkflowException( "The engine does not provide a bean resolver" );
        }
        return engine.getBeanResolver();
    }
}
