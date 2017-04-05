package ee.telekom.workflow.web.console.form;

import java.io.Serializable;

public class CreateWorkflowInstanceForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long refNum;
    private String workflowName;
    private String workflowVersion;
    private String arguments;
    private String label1;
    private String label2;

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

    public String getWorkflowVersion(){
        return workflowVersion;
    }

    public void setWorkflowVersion( String workflowVersion ){
        this.workflowVersion = workflowVersion;
    }

    public String getArguments(){
        return arguments;
    }

    public void setArguments( String arguments ){
        this.arguments = arguments;
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

}
