package ee.telekom.workflow.graph.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphValidator;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.Transition;
import ee.telekom.workflow.graph.el.ElUtil;
import ee.telekom.workflow.graph.node.activity.BeanAsyncCallActivity;
import ee.telekom.workflow.graph.node.activity.BeanCallActivity;
import ee.telekom.workflow.graph.node.activity.CreateNewInstanceActivity;
import ee.telekom.workflow.graph.node.activity.HumanTaskActivity;
import ee.telekom.workflow.graph.node.activity.ObjectCallActivity;
import ee.telekom.workflow.graph.node.activity.SetAttributeActivity;
import ee.telekom.workflow.graph.node.event.CatchSignal;
import ee.telekom.workflow.graph.node.input.MapMapping;
import ee.telekom.workflow.graph.node.output.MapEntryMapping;
import ee.telekom.workflow.graph.node.output.OutputMapping;
import ee.telekom.workflow.graph.node.output.ValueMapping;

public class GraphValidatorImpl implements GraphValidator{

    @Override
    public List<String> validate( Graph graph ){
        List<String> errors = new LinkedList<>();

        // validate name and version
        validateNameAndVersion( graph, errors );
        if( !errors.isEmpty() ){
            return errors;
        }

        // allow empty graphs
        if( graph.getNodes().isEmpty() && graph.getTransitions().isEmpty() ){
            return errors;
        }

        // otherwise check for mandatory start node
        validateStartNode( graph, errors );
        if( !errors.isEmpty() ){
            return errors;
        }

        // and for any unreachable nodes 
        validateNoUnreachableNodes( graph, errors );

        // check for reserved variable names
        validateReservedVariableNames( graph, errors );

        return errors;
    }

    private static void validateNameAndVersion( Graph graph, List<String> errors ){
        if( StringUtils.isBlank( graph.getName() ) ){
            errors.add( "Graph does not define a name" );
        }
        if( graph.getVersion() <= 0 ){
            errors.add( "Graph version is not a positive number." );
        }
    }

    private static void validateStartNode( Graph graph, List<String> errors ){
        if( graph.getStartNode() == null ){
            errors.add( describe( graph ) + " does not define a start node" );
        }
    }

    private static void validateNoUnreachableNodes( Graph graph, List<String> errors ){
        Set<Integer> reachables = new TreeSet<>();
        Queue<Node> queue = new LinkedList<>();
        queue.offer( graph.getStartNode() );
        Node reachable;
        while( (reachable = queue.poll()) != null ){
            if( reachables.contains( reachable.getId() ) ){
                continue;
            }
            reachables.add( reachable.getId() );
            for( Transition transition : graph.getOutputTransitions( reachable ) ){
                queue.add( transition.getEndNode() );
            }
        }
        for( Node node : graph.getNodes() ){
            if( !reachables.contains( node.getId() ) ){
                errors.add( describe( graph ) + " contains unreachable node " + describe( node ) );
            }
        }
    }

    private static String describe( Graph graph ){
        return graph.getName() + ":" + graph.getVersion();
    }

    private static String describe( Node node ){
        return node.getId() + " " + node.getName() + " (" + node.getClass().getSimpleName() + ")";
    }

    /**
     * check through all the node types which use mappings which set Environment attributes
     */
    private static void validateReservedVariableNames( Graph graph, List<String> errors ){
        for( Node node : graph.getNodes() ){
            if( node instanceof BeanAsyncCallActivity ){
                validateReservedVariablesFromMapping( ((BeanAsyncCallActivity)node).getResultMapping(), graph, node, errors );
            }
            else if( node instanceof BeanCallActivity ){
                validateReservedVariablesFromMapping( ((BeanCallActivity)node).getResultMapping(), graph, node, errors );
            }
            else if( node instanceof CreateNewInstanceActivity ){
                MapMapping mapMapping = ((CreateNewInstanceActivity)node).getArgumentsMapping();
                if( mapMapping != null && mapMapping.getEntryMappings() != null ){
                    for( String key : mapMapping.getEntryMappings().keySet() ){
                        validateReservedVariable( key, graph, node, errors );
                    }
                }
            }
            else if( node instanceof HumanTaskActivity ){
                validateReservedVariablesFromMapping( ((HumanTaskActivity)node).getResultMapping(), graph, node, errors );
            }
            else if( node instanceof ObjectCallActivity ){
                validateReservedVariablesFromMapping( ((ObjectCallActivity)node).getResultMapping(), graph, node, errors );
            }
            else if( node instanceof SetAttributeActivity ){
                validateReservedVariable( ((SetAttributeActivity)node).getAttribute(), graph, node, errors );
            }
            else if( node instanceof CatchSignal ){
                validateReservedVariablesFromMapping( ((CatchSignal)node).getResultMapping(), graph, node, errors );
            }
        }
    }

    private static void validateReservedVariablesFromMapping( OutputMapping outputMapping, Graph graph, Node node, List<String> errors ){
        if( outputMapping instanceof MapEntryMapping ){
            Map<String, String> mappings = ((MapEntryMapping)outputMapping).getMappings();
            if( mappings != null ){
                for( String variableName : mappings.values() ){
                    validateReservedVariable( variableName, graph, node, errors );
                }
            }
        }
        else if( outputMapping instanceof ValueMapping ){
            validateReservedVariable( ((ValueMapping)outputMapping).getName(), graph, node, errors );
        }
    }

    private static void validateReservedVariable( String variableName, Graph graph, Node node, List<String> errors ){
        if( ElUtil.isReservedVariable( variableName ) ){
            errors.add( describe( graph ) + " node " + describe( node ) + " contains a reserved variable name: " + variableName );
        }
    }

}
