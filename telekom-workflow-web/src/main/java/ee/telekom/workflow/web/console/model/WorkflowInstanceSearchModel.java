package ee.telekom.workflow.web.console.model;

import java.io.Serializable;
import java.util.Date;

import ee.telekom.workflow.facade.util.DateUtil;

public class WorkflowInstanceSearchModel implements Serializable{

    private static final long serialVersionUID = 1L;

    private Long refNum;
    private String workflowNameWithVersion;
    private String label1;
    private String label2;
    private Date dateCreated;
    private String dateCreatedText;
    private Date nextTimerDueDate;
    private String nextTimerDueDateText;
    private String hasActiveHumanTask;
    private String displayStatus;
    private String status;

    public Long getRefNum(){
        return refNum;
    }

    public void setRefNum( Long refNum ){
        this.refNum = refNum;
    }

    public String getWorkflowNameWithVersion(){
        return workflowNameWithVersion;
    }

    public void setWorkflowNameWithVersion( String workflowNameWithVersion ){
        this.workflowNameWithVersion = workflowNameWithVersion;
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

    public Date getDateCreated(){
        return dateCreated;
    }

    public void setDateCreated( Date dateCreated ){
        this.dateCreated = dateCreated;
        this.dateCreatedText = DateUtil.formatDate( dateCreated );
    }

    public String getDateCreatedText(){
        return dateCreatedText;
    }

    public Date getNextTimerDueDate(){
        return nextTimerDueDate;
    }

    public void setNextTimerDueDate( Date nextTimerDueDate ){
        this.nextTimerDueDate = nextTimerDueDate;
        this.nextTimerDueDateText = DateUtil.formatDate( nextTimerDueDate );;
    }

    public String getNextTimerDueDateText(){
        return nextTimerDueDateText;
    }

    public String getHasActiveHumanTask(){
        return hasActiveHumanTask;
    }

    public void setHasActiveHumanTask( String hasActiveHumanTask ){
        this.hasActiveHumanTask = hasActiveHumanTask;
    }

    public String getDisplayStatus(){
        return displayStatus;
    }

    public void setDisplayStatus( String status ){
        this.displayStatus = status;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus( String status ){
        this.status = status;
    }
}