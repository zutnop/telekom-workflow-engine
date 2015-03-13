package ee.telekom.workflow.graph.node.activity;

import java.util.Map;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.node.AbstractNode;
import ee.telekom.workflow.graph.node.input.ConstantMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.output.OutputMapping;

/**
 * Activity, pausing execution until a human task (manual task) has been completed externally.
 */
public class HumanTaskActivity extends AbstractNode{

    private InputMapping<String> roleMapping;
    private InputMapping<String> userMapping;
    private InputMapping<Map<String, Object>> argumentsMapping;
    private OutputMapping resultMapping;

    public HumanTaskActivity( int id,
            String role,
            String user,
            InputMapping<Map<String, Object>> argumentsMapping,
            OutputMapping resultMapping ){
        this( id, null, ConstantMapping.of( role ), ConstantMapping.of( user ), argumentsMapping, resultMapping );
    }

    public HumanTaskActivity( int id,
            InputMapping<String> roleMapping,
            InputMapping<String> userMapping,
            InputMapping<Map<String, Object>> argumentsMapping,
            OutputMapping resultMapping ){
        this( id, null, roleMapping, userMapping, argumentsMapping, resultMapping );
    }

    public HumanTaskActivity( int id, String name,
            String role,
            String user,
            InputMapping<Map<String, Object>> argumentsMapping,
            OutputMapping resultMapping ){
        this( id, name, ConstantMapping.of( role ), ConstantMapping.of( user ), argumentsMapping, resultMapping );
    }

    public HumanTaskActivity( int id, String name,
            InputMapping<String> roleMapping,
            InputMapping<String> userMapping,
            InputMapping<Map<String, Object>> argumentsMapping,
            OutputMapping resultMapping ){
        super( id, name );
        this.roleMapping = roleMapping;
        this.userMapping = userMapping;
        this.argumentsMapping = argumentsMapping;
        this.resultMapping = resultMapping;
    }

    public InputMapping<String> getRoleMapping(){
        return roleMapping;
    }

    public InputMapping<String> getUserMapping(){
        return userMapping;
    }

    public InputMapping<Map<String, Object>> getArgumentsMapping(){
        return argumentsMapping;
    }

    public OutputMapping getResultMapping(){
        return resultMapping;
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        GraphInstance instance = token.getInstance();
        String role = roleMapping != null ? roleMapping.evaluate( instance ) : null;
        String user = userMapping != null ? userMapping.evaluate( instance ) : null;
        Map<String, Object> arguments = argumentsMapping != null ? argumentsMapping.evaluate( instance ) : null;
        engine.addHumanTaskItem( instance, token, role, user, arguments );
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
