package ee.telekom.workflow.facade.model;

import java.util.Map;

/**
 * Model object that contains necessary information to create a new workflow instance.
 *  
 * @author Christian Klock
 */
public class CreateWorkflowInstance{

    private Long refNum;
    private String workflowName;
    private Integer workflowVersion;
    private String label1;
    private String label2;
    private Map<String, Object> arguments;

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

    public Map<String, Object> getArguments(){
        return arguments;
    }

    public void setArguments( Map<String, Object> arguments ){
        this.arguments = arguments;
    }

}
