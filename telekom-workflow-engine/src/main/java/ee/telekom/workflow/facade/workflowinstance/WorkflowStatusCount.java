package ee.telekom.workflow.facade.workflowinstance;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;

public class WorkflowStatusCount{

    private String workflowName;
    private WorkflowInstanceStatus status;
    private int count;

    public String getWorkflowName(){
        return workflowName;
    }

    public WorkflowInstanceStatus getStatus(){
        return status;
    }

    public void setStatus( WorkflowInstanceStatus status ){
        this.status = status;
    }

    public void setWorkflowName( String workflowName ){
        this.workflowName = workflowName;
    }

    public int getCount(){
        return count;
    }

    public void setCount( int count ){
        this.count = count;
    }

}
