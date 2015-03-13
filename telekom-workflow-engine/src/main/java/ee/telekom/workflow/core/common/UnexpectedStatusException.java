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

    private static final String MSG = "The attempted update failed because the entity was not in one of the expected statuses (%s). This may be caused by a concurrent update.";

    public UnexpectedStatusException( Object expectedStatus ){
        super( String.format( MSG, expectedStatus.toString() ) );
    }

    public UnexpectedStatusException( Collection<? extends Object> expectedStatuses ){
        super( String.format( MSG, expectedStatuses.toString() ) );
    }

}
