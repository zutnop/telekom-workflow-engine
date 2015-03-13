package ee.telekom.workflow.graph.node;

import ee.telekom.workflow.graph.Node;

/**
 * Abstract node implementation providing id and name.
 */
public abstract class AbstractNode implements Node{

    private int id;
    private String name;

    public AbstractNode( int id ){
        this( id, null );
    }

    public AbstractNode( int id, String name ){
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId(){
        return id;
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public String toString(){
        return "[" + getClass().getSimpleName() + " id=" + id + (name == null ? "" : (", name=" + name)) + "]";
    }

}
