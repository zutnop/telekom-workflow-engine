package ee.telekom.workflow.core.workflowinstance;

import java.util.List;
import java.util.Map;

import ee.telekom.workflow.core.common.UnexpectedStatusException;

public interface WorkflowInstanceService{

    WorkflowInstance create( String workflowName, Integer workflowVersion, Map<String, Object> arguments, String label1, String label2 );

    WorkflowInstance find( long refNum );

    void markStarting( long refNum ) throws UnexpectedStatusException;

    void markExecuting( long refNum ) throws UnexpectedStatusException;

    void markExecuted( long refNum ) throws UnexpectedStatusException;

    void markAbort( long refNum ) throws UnexpectedStatusException;

    void markAborting( long refNum ) throws UnexpectedStatusException;

    void markAborted( long refNum ) throws UnexpectedStatusException;

    void rewindAfterError( long refNum ) throws UnexpectedStatusException;

    void suspend( long refNum ) throws UnexpectedStatusException;

    void resume( long refNum ) throws UnexpectedStatusException;

    void handleStartingError( long refNum, Exception exception ) throws UnexpectedStatusException;

    void handleAbortingError( long refNum, Exception exception ) throws UnexpectedStatusException;

    void handleCompleteError( long prexRefNum, Long actionRefNum, Exception exception ) throws UnexpectedStatusException;

    void lock( List<Long> refNum );

    void unlock( long refNum );

    void updateNodeName( long refNum, String nodeName );

    void updateState( long refNum, String state );

    void updateHistory( Long refNum, String history );

    void recoverNotAssigned( String clusterName );

    void recoverNew( String nodeName );

    void recoverStarting( String nodeName );

    void recoverExecuting( String nodeName );

    void recoverAbort( String nodeName );

    void recoverAborting( String nodeName );

}