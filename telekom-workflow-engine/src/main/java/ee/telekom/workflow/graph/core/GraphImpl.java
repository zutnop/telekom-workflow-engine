package ee.telekom.workflow.graph.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.Transition;
import ee.telekom.workflow.graph.WorkflowException;

public class GraphImpl implements Graph{

    private String name;
    private int version;
    private Node startNode;
    private Map<Integer, Node> nodeById = new LinkedHashMap<>();
    private Map<Integer, List<Transition>> transitionsByStartNode = new LinkedHashMap<>();

    public GraphImpl( String name, int version ){
        this.name = name;
        this.version = version;
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public int getVersion(){
        return version;
    }

    @Override
    public Node getStartNode(){
        return startNode;
    }

    @Override
    public Node getNode( int id ){
        return nodeById.get( id );
    }

    @Override
    public Collection<Node> getNodes(){
        return new ArrayList<>( nodeById.values() );
    }

    @Override
    public List<Transition> getTransitions(){
        List<Transition> allTransitions = new LinkedList<>();
        for( List<Transition> transitions : transitionsByStartNode.values() ){
            allTransitions.addAll( transitions );
        }
        return allTransitions;
    }

    @Override
    public List<Transition> getOutputTransitions( Node node ){
        List<Transition> result = transitionsByStartNode.get( node.getId() );
        if( result != null ){
            return result;
        }
        else{
            return Collections.emptyList();
        }
    }

    @Override
    public Transition getOutputTransitions( Node node, String name ){
        for( Transition transition : getOutputTransitions( node ) ){
            if( ObjectUtils.equals( transition.getName(), name ) ){
                return transition;
            }
        }
        return null;
    }

    public void setStartNode( Node startNode ){
        this.startNode = startNode;
        addNode( startNode );
    }

    public void addNode( Node node ){
        Node previous = nodeById.get( node.getId() );
        if( previous != null && previous != node ){
            throw new WorkflowException( "Workflow " + name + ":" + version + " already contains a node with id " + node.getId() );
        }
        nodeById.put( node.getId(), node );
    }

    public void addTransition( Transition transition ){
        Transition previous = getOutputTransitions( transition.getStartNode(), transition.getName() );
        if( previous != null && previous != transition ){
            throw new WorkflowException( "Workflow " + name + ":" + version + " already contains a transition from node " + transition.getStartNode()
                    + " with name " + transition.getName() );
        }
        if( !contains( transition.getStartNode() ) ){
            throw new WorkflowException( "Workflow " + name + ":" + version + " does not contain the start node of transition " + transition );
        }
        if( !contains( transition.getStartNode() ) ){
            throw new WorkflowException( "Workflow " + name + ":" + version + " does not contain the end node of transition " + transition );
        }
        Integer nodeId = transition.getStartNode().getId();
        List<Transition> outputTransitions = transitionsByStartNode.get( nodeId );
        if( outputTransitions == null ){
            outputTransitions = new ArrayList<Transition>( 1 );
            transitionsByStartNode.put( nodeId, outputTransitions );
        }
        outputTransitions.add( transition );
    }

    private boolean contains( Node node ){
        Node sameIdNode = nodeById.get( node.getId() );
        return sameIdNode != null && sameIdNode == node;
    }
}
