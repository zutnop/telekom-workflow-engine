package ee.telekom.workflow.core.common;

import java.util.Collection;

/**
 * Exception thrown when an entity (e.g. a workflow instance or a work item)
 * is not found in the expected status in the database during an UPDATE 
 * statement.
 * <p>
 * Throwing this type of exception can be considered an optimistic locking 
 * failure. A retry of the update is generally not possible.
 *  
 * @author Christian Klock
 */
public class UnexpectedStatusException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private static final String MSG = "The attempted update failed. Error: %s. This may be caused by a concurrent update.";

    public UnexpectedStatusException( String error ) {
        super(  String.format( MSG, error ) );
    }

    public UnexpectedStatusException( Object expectedStatus ){
        this( "The entity was not in the expected status (" + expectedStatus.toString() + ")" );
    }

    public UnexpectedStatusException( Collection<? extends Object> expectedStatuses ){
        this( "The entity was not in one of the expected status (" + expectedStatuses.toString() + ")" );
    }

}
