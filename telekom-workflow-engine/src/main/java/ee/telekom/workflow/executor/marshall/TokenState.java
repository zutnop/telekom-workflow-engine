package ee.telekom.workflow.executor.marshall;

/**
 * Helper class to serialize the state of tokens.
 * 
 * @author Christian Klock
 */
public class TokenState{

    private int id;
    private int nodeId;
    private Integer parentId;
    private boolean isActive;

    public int getId(){
        return id;
    }

    public void setId( int id ){
        this.id = id;
    }

    public int getNodeId(){
        return nodeId;
    }

    public void setNodeId( int nodeId ){
        this.nodeId = nodeId;
    }

    public Integer getParentId(){
        return parentId;
    }

    public void setParentId( Integer parentId ){
        this.parentId = parentId;
    }

    public boolean isActive(){
        return isActive;
    }

    public void setActive( boolean isActive ){
        this.isActive = isActive;
    }

}
