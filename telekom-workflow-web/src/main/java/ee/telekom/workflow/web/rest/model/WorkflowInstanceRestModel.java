package ee.telekom.workflow.web.rest.model;

import java.io.Serializable;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;

public class WorkflowInstanceRestModel implements Serializable{

    private static final long serialVersionUID = 1L;
    private long refNum;
    private String workflowName;
    private Integer workflowVersion;

    private String label1;
    private String label2;

    private WorkflowInstanceStatus status;

    // For Spring
    public WorkflowInstanceRestModel(){
    }

    public WorkflowInstanceRestModel( long refNum, String workflowName, Integer workflowVersion, String label1, String label2, WorkflowInstanceStatus status ){
        this.refNum = refNum;
        this.workflowName = workflowName;
        this.workflowVersion = workflowVersion;
        this.label1 = label1;
        this.label2 = label2;
        this.status = status;
    }

    public long getRefNum(){
        return refNum;
    }

    public String getWorkflowName(){
        return workflowName;
    }

    public Integer getWorkflowVersion(){
        return workflowVersion;
    }

    public String getLabel1(){
        return label1;
    }

    public String getLabel2(){
        return label2;
    }

    public WorkflowInstanceStatus getStatus(){
        return status;
    }

}
