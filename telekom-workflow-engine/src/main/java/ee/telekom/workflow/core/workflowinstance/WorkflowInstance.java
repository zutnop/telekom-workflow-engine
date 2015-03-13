package ee.telekom.workflow.core.workflowinstance;

import ee.telekom.workflow.facade.WorkflowEngineFacade;
import ee.telekom.workflow.graph.GraphInstance;

/**
 * A workflow instance describes an executing instance of a workflow.
 * <p>
 * A {@link WorkflowInstance} is the persisted correspondent of a {@link GraphInstance}.
 * <p>
 * This class is intended for internal usage within this module and should not
 * be passed to external clients via the {@link WorkflowEngineFacade}.
 *
 * @author Christian Klock
 */
public class WorkflowInstance{

    private Long refNum;
    private String workflowName;
    private Integer workflowVersion;

    private String attributes;
    private String state;
    private String history;

    private String label1;
    private String label2;

    private String clusterName;
    private String nodeName;

    private WorkflowInstanceStatus status;
    private boolean locked;

    public Long getRefNum(){
        return refNum;
    }

    public void setRefNum( Long refNum ){
        this.refNum = refNum;
    }

    public String getWorkflowName(){
        return workflowName;
    }

    public void setWorkflowName( String workflowName ){
        this.workflowName = workflowName;
    }

    public Integer getWorkflowVersion(){
        return workflowVersion;
    }

    public void setWorkflowVersion( Integer workflowVersion ){
        this.workflowVersion = workflowVersion;
    }

    public String getAttributes(){
        return attributes;
    }

    public void setAttributes( String attributes ){
        this.attributes = attributes;
    }

    public String getState(){
        return state;
    }

    public void setState( String state ){
        this.state = state;
    }

    public String getHistory(){
        return history;
    }

    public void setHistory( String history ){
        this.history = history;
    }

    public String getLabel1(){
        return label1;
    }

    public void setLabel1( String label1 ){
        this.label1 = label1;
    }

    public String getLabel2(){
        return label2;
    }

    public void setLabel2( String label2 ){
        this.label2 = label2;
    }

    public String getClusterName(){
        return clusterName;
    }

    public void setClusterName( String clusterName ){
        this.clusterName = clusterName;
    }

    public String getNodeName(){
        return nodeName;
    }

    public void setNodeName( String nodeName ){
        this.nodeName = nodeName;
    }

    public WorkflowInstanceStatus getStatus(){
        return status;
    }

    public void setStatus( WorkflowInstanceStatus status ){
        this.status = status;
    }

    public boolean isLocked(){
        return locked;
    }

    public void setLocked( boolean locked ){
        this.locked = locked;
    }

}
