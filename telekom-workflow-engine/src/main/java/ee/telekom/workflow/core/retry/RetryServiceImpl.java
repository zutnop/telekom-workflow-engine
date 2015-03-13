package ee.telekom.workflow.core.retry;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.core.error.ExecutionError;
import ee.telekom.workflow.core.error.ExecutionErrorService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.core.workitem.WorkItemService;

@Service
@Transactional
public class RetryServiceImpl implements RetryService{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private WorkItemService workItemService;
    @Autowired
    private ExecutionErrorService executionErrorService;

    @Override
    public void retry( long woinRefNum ){
        ExecutionError error = executionErrorService.findByWoinRefNum( woinRefNum );
        log.info( "Retry execution of workflow instance " + error.getWoinRefNum() + (error.getWoitRefNum() == null ? "" : " " + error.getWoitRefNum()) );
        workflowInstanceService.rewindAfterError( error.getWoinRefNum() );
        if( error.getWoitRefNum() != null ){
            workItemService.rewindAfterError( error.getWoitRefNum() );
        }
        executionErrorService.delete( error.getRefNum() );
        workflowInstanceService.unlock( error.getWoinRefNum() );
    }

}
