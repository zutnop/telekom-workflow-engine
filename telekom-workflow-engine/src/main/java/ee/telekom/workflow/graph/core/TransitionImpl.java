package ee.telekom.workflow.graph.core;

import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.Transition;

public class TransitionImpl implements Transition{

    private String name;
    private Node startNode;
    private Node endNode;

    public TransitionImpl( Node startNode, Node endNode ){
        this( Transition.DEFAULT_TRANSITION_NAME, startNode, endNode );
    }

    public TransitionImpl( String name, Node startNode, Node endNode ){
        this.name = name;
        this.startNode = startNode;
        this.endNode = endNode;
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public Node getStartNode(){
        return startNode;
    }

    @Override
    public Node getEndNode(){
        return endNode;
    }

    @Override
    public String toString(){
        return "[Transition name='" + name + "', start=" + startNode + ", end=" + endNode + "]";
    }

}
