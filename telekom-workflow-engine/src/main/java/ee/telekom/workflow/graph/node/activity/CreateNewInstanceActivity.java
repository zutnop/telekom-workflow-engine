package ee.telekom.workflow.graph.node.activity;

import java.util.Map;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.NewGraphInstanceCreator;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.WorkflowException;
import ee.telekom.workflow.graph.core.EnvironmentImpl;
import ee.telekom.workflow.graph.node.AbstractNode;
import ee.telekom.workflow.graph.node.input.ConstantMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.input.MapMapping;

/**
 * Activity, that orders a 
 * The task is executed asynchronously by an external system. 
 */
public class CreateNewInstanceActivity extends AbstractNode{

    private InputMapping<String> graphNameMapping;
    private InputMapping<Integer> graphVersionMapping;
    private InputMapping<String> label1Mapping;
    private InputMapping<String> label2Mapping;
    private MapMapping argumentsMapping;

    public CreateNewInstanceActivity( int id, String graphName, Integer graphVersion, String label1, String label2, MapMapping argumentsMapping ){
        this( id, null, graphName, graphVersion, label1, label2, argumentsMapping );
    }

    public CreateNewInstanceActivity( int id, String name, String graphName, Integer graphVersion, String label1, String label2, MapMapping argumentsMapping ){
        this( id, null, ConstantMapping.of( graphName ), ConstantMapping.of( graphVersion ), ConstantMapping.of( label1 ), ConstantMapping.of( label2 ),
                argumentsMapping );
    }

    public CreateNewInstanceActivity( int id, InputMapping<String> graphNameMapping, InputMapping<Integer> graphVersionMapping,
            InputMapping<String> label1Mapping, InputMapping<String> label2Mapping, MapMapping argumentsMapping ){
        this( id, null, graphNameMapping, graphVersionMapping, label1Mapping, label2Mapping, argumentsMapping );
    }

    public CreateNewInstanceActivity( int id, String name, InputMapping<String> graphNameMapping, InputMapping<Integer> graphVersionMapping,
            InputMapping<String> label1Mapping, InputMapping<String> label2Mapping, MapMapping argumentsMapping ){
        super( id, name );
        this.graphNameMapping = graphNameMapping;
        this.graphVersionMapping = graphVersionMapping;
        this.label1Mapping = label1Mapping;
        this.label2Mapping = label2Mapping;
        this.argumentsMapping = argumentsMapping;
    }

    public MapMapping getArgumentsMapping(){
        return argumentsMapping;
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        GraphInstance instance = token.getInstance();
        String graphName = graphNameMapping.evaluate( instance );
        Integer graphVersion = graphVersionMapping.evaluate( instance );
        String label1 = label1Mapping.evaluate( instance );
        String label2 = label2Mapping.evaluate( instance );
        Environment initialEnvironment = new EnvironmentImpl();
        if (argumentsMapping != null) {
	        Map<String, Object> arguments = argumentsMapping.evaluate( instance );
	        for( Map.Entry<String, Object> argument : arguments.entrySet() ){
	            initialEnvironment.setAttribute( argument.getKey(), argument.getValue() );
	        }
        }
        getNewGraphInstanceCreator( engine ).create( graphName, graphVersion, label1, label2, initialEnvironment );
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

    private NewGraphInstanceCreator getNewGraphInstanceCreator( GraphEngine engine ){
        if( engine.getNewGraphInstanceCreator() == null ){
            throw new WorkflowException( "The engine does not provide a NewGraphInstanceCreator" );
        }
        return engine.getNewGraphInstanceCreator();
    }

}
