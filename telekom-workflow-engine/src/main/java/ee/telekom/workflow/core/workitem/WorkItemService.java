package ee.telekom.workflow.core.workitem;

import java.util.List;

import ee.telekom.workflow.core.common.UnexpectedStatusException;

public interface WorkItemService{

    WorkItem find( long refNum );

    List<WorkItem> findActiveByWoinRefNum( long woinRefNum );

    WorkItem findActiveByWoinRefNumAndTokenId( long woinRefNum, int tokenId );

    void markExecuting( long refNum );

    void markExecutedAndSaveResult( long refNum, String result );

    void markCompleting( long refNum );

    void markCompleted( long refNum );

    void markCancelled( long refNum );

    void handleExecutingError( long woinRefNum, long woitRefNum, Exception e );

    void handleCompletingError( long woinRefNum, long woitRefNum, Exception e );

    void rewindAfterError( long refNum ) throws UnexpectedStatusException;

    void recoverExecuting( String nodeName );

    void recoverCompleting( String nodeName );

}
