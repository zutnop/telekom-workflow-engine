package ee.telekom.workflow.facade.model;

import java.io.Serializable;
import java.util.List;

/**
 * Model object that contains a search request.
 *
 * When searching for workflow instances, then multiple, non-empty fields are treated join
 * by an AND operator. Multiple entries within the same field (refNums, label1 or label2) are
 * joined by an OR operator.
 *
 * @author Christian Klock
 */
public class SearchWorkflowInstances implements Serializable{

    private static final long serialVersionUID = 1L;

    private List<Long> refNum;
    private List<String> workflowName;
    private List<String> label1;
    private List<String> label2;
    private List<WorkflowInstanceFacadeStatus> status;

    private int start;
    private int length;
    private Integer column;
    private String direction;

    public List<Long> getRefNum(){
        return refNum;
    }

    public void setRefNum( List<Long> refNum ){
        this.refNum = refNum;
    }

    public List<String> getWorkflowName(){
        return workflowName;
    }

    public void setWorkflowName( List<String> workflowName ){
        this.workflowName = workflowName;
    }

    public List<String> getLabel1(){
        return label1;
    }

    public void setLabel1( List<String> label1 ){
        this.label1 = label1;
    }

    public List<String> getLabel2(){
        return label2;
    }

    public void setLabel2( List<String> label2 ){
        this.label2 = label2;
    }

    public List<WorkflowInstanceFacadeStatus> getStatus(){
        return status;
    }

    public void setStatus( List<WorkflowInstanceFacadeStatus> status ){
        this.status = status;
    }

    public int getStart(){
        return start;
    }

    public void setStart( int start ){
        this.start = start;
    }

    public int getLength(){
        return length;
    }

    public void setLength( int length ){
        this.length = length;
    }

    public Integer getColumn(){
        return column;
    }

    public void setColumn( Integer column ){
        this.column = column;
    }

    public String getDirection(){
        return direction;
    }

    public void setDirection( String direction ){
        this.direction = direction;
    }
}
