package ee.telekom.workflow.facade.model;

/**
 * Model object that provides details on an execution error.
 * 
 * @author Christian Klock
 */
public class ExecutionErrorState{

    private Long refNum;
    private Long woinRefNum;
    private Long woitRefNum;
    private String errorText;
    private String errorDetails;

    /**
     * @return the execution error's internal id.
     */
    public Long getRefNum(){
        return refNum;
    }

    public void setRefNum( Long refNum ){
        this.refNum = refNum;
    }

    /**
     * @return the concerned workflow instance's internal id, never null.
     */
    public Long getWoinRefNum(){
        return woinRefNum;
    }

    public void setWoinRefNum( Long woinRefNum ){
        this.woinRefNum = woinRefNum;
    }

    /**
     * @return the concerned work item's id. Null, if error occurred while performing an
     * action on the instance (e.g. start/abort instance) rather than on a work item (e.g. execute task, complete work item).
     */
    public Long getWoitRefNum(){
        return woitRefNum;
    }

    public void setWoitRefNum( Long woitRefNum ){
        this.woitRefNum = woitRefNum;
    }

    /**
     * @return the thrown exception's error message.
     */
    public String getErrorText(){
        return errorText;
    }

    public void setErrorText( String errorText ){
        this.errorText = errorText;
    }

    /**
     * @return the thrown exception's stack trace.
     */
    public String getErrorDetails(){
        return errorDetails;
    }

    public void setErrorDetails( String errorDetails ){
        this.errorDetails = errorDetails;
    }

}
