package ee.telekom.workflow.core.workitem;

import java.io.Serializable;
import java.util.Date;

import ee.telekom.workflow.api.AutoRetryOnRecovery;
import ee.telekom.workflow.facade.WorkflowEngineFacade;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.WorkItemStatus;

/**
 * A work item is handle/reference to an uncompleted graph node execution in order
 * to complete it at some future point in time.
 * <p>
 * A {@link WorkItem} is the persisted correspondent of a {@link GraphWorkItem}.
 * <p>
 * This class is intended for internal usage within this module and should not
 * be passed to external clients via the {@link WorkflowEngineFacade}.
 *
 * @author Christian Klock
 */
public class WorkItem implements Serializable{

    private static final long serialVersionUID = 1L;
    private Long refNum;
    private Long woinRefNum;

    private int tokenId;
    private WorkItemStatus status;

    private String signal;
    private Date dueDate;

    private String bean;
    private String method;

    private String role;
    private String userName;

    private String arguments;
    private String result;

    private AutoRetryOnRecovery autoRetryOnRecovery;

    public Long getRefNum(){
        return refNum;
    }

    public void setRefNum( Long refNum ){
        this.refNum = refNum;
    }

    public Long getWoinRefNum(){
        return woinRefNum;
    }

    public void setWoinRefNum( Long woinRefNum ){
        this.woinRefNum = woinRefNum;
    }

    public int getTokenId(){
        return tokenId;
    }

    public void setTokenId( int tokenId ){
        this.tokenId = tokenId;
    }

    public WorkItemStatus getStatus(){
        return status;
    }

    public void setStatus( WorkItemStatus status ){
        this.status = status;
    }

    public String getSignal(){
        return signal;
    }

    public void setSignal( String signal ){
        this.signal = signal;
    }

    public Date getDueDate(){
        return dueDate;
    }

    public void setDueDate( Date dueDate ){
        this.dueDate = dueDate;
    }

    public String getBean(){
        return bean;
    }

    public void setBean( String bean ){
        this.bean = bean;
    }

    public String getMethod(){
        return method;
    }

    public void setMethod( String method ){
        this.method = method;
    }

    public String getRole(){
        return role;
    }

    public void setRole( String role ){
        this.role = role;
    }

    public String getUserName(){
        return userName;
    }

    public void setUserName( String userName ){
        this.userName = userName;
    }

    public String getArguments(){
        return arguments;
    }

    public void setArguments( String arguments ){
        this.arguments = arguments;
    }

    public String getResult(){
        return result;
    }

    public void setResult( String result ){
        this.result = result;
    }

    public boolean getAutoRetryOnRecovery(){
        return autoRetryOnRecovery == null ? AutoRetryOnRecovery.getDefault().asBoolean() : autoRetryOnRecovery.asBoolean();
    }

    public void setAutoRetryOnRecovery( AutoRetryOnRecovery autoRetryOnRecovery ){
        this.autoRetryOnRecovery = autoRetryOnRecovery;
    }

    public WorkItemType getType(){
        if( signal != null ){
            return WorkItemType.SIGNAL;
        }
        else if( dueDate != null ){
            return WorkItemType.TIMER;
        }
        else if( bean != null && method != null ){
            return WorkItemType.TASK;
        }
        else if( role != null || userName != null ){
            return WorkItemType.HUMAN_TASK;
        }
        else{
            throw new IllegalStateException( "Unknown work item type" );
        }
    }
}
