package ee.telekom.workflow.core.abort;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.core.error.ExecutionError;
import ee.telekom.workflow.core.error.ExecutionErrorService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.core.workitem.WorkItemService;

@Service
@Transactional
public class AbortServiceImpl implements AbortService{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private WorkItemService workItemService;
    @Autowired
    private ExecutionErrorService executionErrorService;

    /**
     * Prepares the workflow instance for being aborted and marks its status to {@link WorkflowInstanceStatus#ABORT}.
     * Preparing the workflow instance includes rewinding it from errors and unlocking it.
     */
    @Override
    public void abort( long woinRefNum ){
        log.info( "Abort execution of workflow instance " + woinRefNum );
        ExecutionError error = executionErrorService.findByWoinRefNum( woinRefNum );
        if( error != null ){
            workflowInstanceService.rewindAfterError( error.getWoinRefNum() );
            if( error.getWoitRefNum() != null ){
                workItemService.rewindAfterError( error.getWoitRefNum() );
            }
            executionErrorService.delete( error.getRefNum() );
        }
        workflowInstanceService.markAbort( woinRefNum );
        workflowInstanceService.unlock( woinRefNum );
    }

}
