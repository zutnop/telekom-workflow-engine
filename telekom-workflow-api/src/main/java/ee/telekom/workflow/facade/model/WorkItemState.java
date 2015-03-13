package ee.telekom.workflow.facade.model;

import java.util.Date;

/**
 * Model object that reflects a work item's state.
 * 
 * @author Christian Klock
 */
public class WorkItemState{

    public static enum Type{
        SIGNAL, TIMER, TASK, HUMAN_TASK
    }

    private Long refNum;
    private Long woinRefNum;

    private int tokenId;
    private String status;

    private String signal;
    private Date dueDate;

    private String bean;
    private String method;

    private String role;
    private String userName;

    private String arguments;
    private String result;

    private Date dateCreated;
    private Date dateUpdated;

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

    public String getStatus(){
        return status;
    }

    public void setStatus( String status ){
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

    public Date getDateCreated(){
        return dateCreated;
    }

    public void setDateCreated( Date dateCreated ){
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated(){
        return dateUpdated;
    }

    public void setDateUpdated( Date dateUpdated ){
        this.dateUpdated = dateUpdated;
    }

    public Type getType(){
        if( signal != null ){
            return Type.SIGNAL;
        }
        else if( dueDate != null ){
            return Type.TIMER;
        }
        else if( bean != null && method != null ){
            return Type.TASK;
        }
        else if( role != null || userName != null ){
            return Type.HUMAN_TASK;
        }
        else{
            throw new IllegalStateException( "Unknown work item type" );
        }
    }

}
