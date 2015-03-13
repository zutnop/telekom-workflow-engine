package ee.telekom.workflow.graph.core;

import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.Token;

public class TokenImpl implements Token{

    private int id;
    private Node node;
    private GraphInstance instance;
    private Token parent;
    private boolean isActive;

    @Override
    public int getId(){
        return id;
    }

    public void setId( int id ){
        this.id = id;
    }

    @Override
    public Node getNode(){
        return node;
    }

    @Override
    public void setNode( Node node ){
        this.node = node;
    }

    @Override
    public GraphInstance getInstance(){
        return instance;
    }

    public void setInstance( GraphInstance instance ){
        this.instance = instance;
    }

    @Override
    public Token getParent(){
        return parent;
    }

    public void setParent( Token parent ){
        this.parent = parent;
    }

    @Override
    public boolean isActive(){
        return isActive;
    }

    public void setActive( boolean isActive ){
        this.isActive = isActive;
    }

    @Override
    public void markInactive(){
        setActive( false );
    }

}
