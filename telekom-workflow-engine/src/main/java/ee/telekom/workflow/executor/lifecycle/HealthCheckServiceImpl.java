package ee.telekom.workflow.executor.lifecycle;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.node.NodeService;
import ee.telekom.workflow.core.recovery.RecoveryService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.executor.producer.WorkProducerJob;
import ee.telekom.workflow.executor.queue.WorkQueue;

@Component
public class HealthCheckServiceImpl implements HealthCheckService{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private RecoveryService recoveryService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private WorkProducerJob producer;
    @Autowired
    private WorkQueue queue;
    @Autowired
    private LifecycleService lifecycleService;
    @Autowired
    private WorkflowEngineConfiguration config;
    @Autowired
    private WorkflowInstanceService workflowInstanceService;

    /**
    * "Cluster healing" is meant to repair inconsistent database state that results from an unclean cluster shutdown or network hardware failure.
    * Inconsistent database state results from non-transactional operations in the cluster, such as adding/taking work units to/from the work unit
    * queue. Let's recall the possible states of (locked, nodeName) of a work instance and how they relate to the non-transactional operations. 
    * <ol>
    * <li>After being a added to the work unit queue -> (true, null)
    * <li>After being taken from the work unit queue -> (true, 'the consuming node's name)
    * <li>After the work unit is successfully -> (false, null).
    * </ol>
    * <p>
    * <b>Scenario 1:</b> A node takes a work unit from the queue and updates the associated workflow instance node_name field. Maybe it also updates
    * the workflow instance's or work item's status field. Afterwards the node is found dead/failed.<br>
    * <b>Scenario 2:</b> The master node locks a workflow instance but fails before it can add the workflow instance to the work unit queue.<br>
    * <p>
    * <b>Resolution for scenario 1:</b><br>
    * <ol>
    * <li>If the node updated the workflow instance's or the work item's status field, then this field is left in the temporary execution status
    *     and needs to be recovered. For workflow instances: STARTING->NEW or ABORTING->ABORT. For work items: EXECUTING->(is task ? NEW : ERROR) 
    *     or COMPLETING->EXECUTED.
    * <li>The fields (locked, nodeName) need to be set to (false, null).
    * </ol>
    * NB! EXECUTING task work items cannot be automatically recovered, since their execution is not guaranteed to be transactional. Therefore, 
    *     they need to be handled manually. To this end, their status field is set to EXECUTING_ERROR and an error message is created in the EXER table.
    * <p>
    * <b>Resolution for scenario 2:</b><br>
    * First of all, we need to make sure that the working queue is empty and that every consumer has had sufficient time to assign its most recently
    * taken work unit to itself. To this extend, every consumer is granted the so called "maximum node assignment time."
    * <p>
    * Two different kind of errors may cause the work unit queue to be empty for at least the duration of the maximum node assignment and at the same
    * time (locked, nodeName) = (true, null) exist. The first kind is that the distributed queue failed. The second kind is that the node which took 
    * the element from the queue failed between taking the element and assigning the process execution to itself.
    * <p>
    * The recovery of this scenario is expensive because we need to suspend the producer, wait for the queue to become empty, wait for 
    * the maximum node assignment grace period, do the recovery and resume the producer. For this reason, this advanced recovery is not
    * run on every health check. 
    */
    @Override
    public void healFailedNodes(){
        List<String> nodes = nodeService.findFailedNodes();
        if( nodes.isEmpty() ){
            return;
        }
        log.info( "Healing nodes " + nodes );

        // recovery of locked workflow instances that are assigned to a dead node
        recoveryService.recoverExecutionsAssignedToNodes( nodes );

        // recovery of locked workflow instances that are NOT assigned to any node
        boolean isStarted = lifecycleService.isStarted();
        if( isStarted ){
            producer.suspend();
            queue.awaitEmpty();
            int maximumNodeAssignementTime = config.getMaximumNodeAssignmentTimeSeconds();
            sleep( maximumNodeAssignementTime );
        }
        String clusterName = config.getClusterName();
        recoveryService.recoverExecutionsNotAssignedToNodes( clusterName );
        if( isStarted ){
            producer.resume();
        }
        nodeService.markEnable( nodes );
    }

    /**
     * Check for workflow instances that are in the locked state without updates (meaning that a node is executing some work item for this instance) for longer
     * than configured workflowengine.workItemExecutionTimeWarnSeconds time. Then log an ERROR to draw attention to these potentially stuck workflows.
     *
     * NB! The workflow instance execution itself is NOT affected by this mechanism, just logging.
     */
    @Override
    public void checkForStuckWorkflows(){
        String clusterName = config.getClusterName();
        int workItemExecutionTimeWarnSeconds = config.getWorkItemExecutionTimeWarnSeconds();
        List<WorkflowInstance> stuckWorkflowInstances = workflowInstanceService.findStuck( clusterName, workItemExecutionTimeWarnSeconds );
        if ( !CollectionUtils.isEmpty( stuckWorkflowInstances ) ){
            String stuckRefNums = stuckWorkflowInstances.stream()
                    .map( woin -> woin.getRefNum().toString() )
                    .collect( Collectors.joining( ", " ) );
            log.error( "Found potentially stuck workflow instances (exceeding execution time of {} seconds), ref_num-s: {}"
                    , workItemExecutionTimeWarnSeconds
                    , stuckRefNums );
        }
    }

    private void sleep( int seconds ){
        try{
            TimeUnit.SECONDS.sleep( seconds );
        }
        catch( InterruptedException e ){
            log.warn( "Wake up after interrupt exception" );
        }
    }

}
