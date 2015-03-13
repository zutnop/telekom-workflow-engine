package ee.telekom.workflow.core.recovery;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.core.workitem.WorkItemService;

@Service
@Transactional
public class RecoveryServiceImpl implements RecoveryService{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private WorkItemService workItemService;

    @Override
    public void recoverExecutionsAssignedToNodes( List<String> deadNodes ){
        for( String nodeName : deadNodes ){
            log.info( "Running recovery for failed node {}", nodeName );
            workItemService.recoverExecuting( nodeName );
            workItemService.recoverCompleting( nodeName );
            workflowInstanceService.recoverNew( nodeName );
            workflowInstanceService.recoverExecuting( nodeName );
            workflowInstanceService.recoverStarting( nodeName );
            workflowInstanceService.recoverAbort( nodeName );
            workflowInstanceService.recoverAborting( nodeName );
        }
    }

    @Override
    public void recoverExecutionsNotAssignedToNodes( String clusterName ){
        workflowInstanceService.recoverNotAssigned( clusterName );
    }

}
