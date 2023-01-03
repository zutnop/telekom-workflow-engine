package ee.telekom.workflow.graph.node.activity;

import ee.telekom.workflow.api.AutoRecovery;
import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.node.AbstractNode;
import ee.telekom.workflow.graph.node.input.ArrayMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.output.OutputMapping;

/**
 * Activity, pausing execution until a task (call of method on a bean with given name) has been completed.
 * The task is executed asynchronously by an external system. 
 */
public class BeanAsyncCallActivity extends AbstractNode{

    private String bean;
    private String method;
    private AutoRecovery autoRecovery;
    private ArrayMapping argumentsMapping;
    private OutputMapping resultMapping;

    public BeanAsyncCallActivity( int id, String bean, String method, InputMapping<?>[] argumentsMappings, OutputMapping resultMapping ){
        this( id, null, AutoRecovery.DISABLED, bean, method, argumentsMappings, resultMapping );
    }

    public BeanAsyncCallActivity( int id, String bean, AutoRecovery autoRecovery, String method, InputMapping<?>[] argumentsMappings, OutputMapping resultMapping ){
        this( id, null, autoRecovery, bean, method, argumentsMappings, resultMapping );
    }

    public BeanAsyncCallActivity( int id, String name, AutoRecovery autoRecovery, String bean, String method, InputMapping<?>[] argumentsMappings, OutputMapping resultMapping ){
        super( id, name );
        this.bean = bean;
        this.method = method;
        this.autoRecovery = autoRecovery;
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
        Object[] arguments = argumentsMapping.evaluate( instance );
        engine.addTaskItem( instance, token, bean, method, autoRecovery, arguments );
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
