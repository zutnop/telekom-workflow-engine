package ee.telekom.workflow.core.error;

import ee.telekom.workflow.facade.WorkflowEngineFacade;

/**
 * Encapsulates information on an error when processing consuming a work unit.
 * <p>
 * This class is intended for internal usage within this module and should not
 * be passed to external clients via the {@link WorkflowEngineFacade}.
 * 
 * @author Christian Klock
 */
public class ExecutionError{

    private Long refNum;

    private Long woinRefNum;
    private Long woitRefNum;
    private String errorText;
    private String errorDetails;

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

    public Long getWoitRefNum(){
        return woitRefNum;
    }

    public void setWoitRefNum( Long woitRefNum ){
        this.woitRefNum = woitRefNum;
    }

    public String getErrorText(){
        return errorText;
    }

    public void setErrorText( String errorText ){
        this.errorText = errorText;
    }

    public String getErrorDetails(){
        return errorDetails;
    }

    public void setErrorDetails( String errorDetails ){
        this.errorDetails = errorDetails;
    }

}
