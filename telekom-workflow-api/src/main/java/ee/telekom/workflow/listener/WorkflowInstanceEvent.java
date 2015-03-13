package ee.telekom.workflow.listener;

import java.util.Map;

/**
 * Provides details on the workflow instance associated with the event, the workflow instance's id,
 * the workflow's name and version and the instance's current attributes (the instance's internal memory).
 * 
 * The attributes must be used read-only!
 * 
 * @author Christian Klock
 */
public class WorkflowInstanceEvent{

    private Long woinRefNum;
    private String workflowName;
    private Integer workflowVersion;
    private Map<String, Object> attributes;

    public WorkflowInstanceEvent( Long woinRefNum, String workflowName, Integer workflowVersion, Map<String, Object> attributes ){
        this.woinRefNum = woinRefNum;
        this.workflowName = workflowName;
        this.workflowVersion = workflowVersion;
        this.attributes = attributes;
    }

    public Long getWoinRefNum(){
        return woinRefNum;
    }

    public String getWorkflowName(){
        return workflowName;
    }

    public Integer getWorkflowVersion(){
        return workflowVersion;
    }

    public Map<String, Object> getAttributes(){
        return attributes;
    }

}
