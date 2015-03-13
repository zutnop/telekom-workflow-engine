package ee.telekom.workflow.core.error;

/**
 * Provides services for handling execution errors.
 * 
 * @author Christian Klock
 */
public interface ExecutionErrorService{

    /**
     * Creates a new {@link ExecutionError} entry.
     */
    void handleError( long woinRefNum, Long woitRefNum, Exception exception );

    /**
     * Returns the workflow instance's execution error or null, if the workflow
     * instance is currently not in an error status.
     */
    ExecutionError findByWoinRefNum( long woinRefNum );

    /**
     * Delete an execution error by the execution error refNum.
     */
    void delete( long refNum );

}
