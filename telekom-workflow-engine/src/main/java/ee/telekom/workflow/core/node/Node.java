package ee.telekom.workflow.core.node;

import java.util.Date;

import ee.telekom.workflow.facade.WorkflowEngineFacade;

/**
 * Encapsulates the state of an engine's instance within the cluster.
 * <p>
 * This class is intended for internal usage within this module and should not
 * be passed to external clients via the {@link WorkflowEngineFacade}.
 *
 * @author Christian Klock
 */
public class Node{

    private Long refNum;
    private String nodeName;
    private String clusterName;
    private NodeStatus status;
    private Date heartbeat;

    public Long getRefNum(){
        return refNum;
    }

    public void setRefNum( Long refNum ){
        this.refNum = refNum;
    }

    public String getNodeName(){
        return nodeName;
    }

    public void setNodeName( String nodeName ){
        this.nodeName = nodeName;
    }

    public String getClusterName(){
        return clusterName;
    }

    public void setClusterName( String clusterName ){
        this.clusterName = clusterName;
    }

    public NodeStatus getStatus(){
        return status;
    }

    public void setStatus( NodeStatus status ){
        this.status = status;
    }

    public Date getHeartbeat(){
        return heartbeat;
    }

    public void setHeartbeat( Date heartbeat ){
        this.heartbeat = heartbeat;
    }

}
