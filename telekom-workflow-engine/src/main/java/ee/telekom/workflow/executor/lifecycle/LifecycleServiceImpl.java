package ee.telekom.workflow.executor.lifecycle;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.lock.LockService;
import ee.telekom.workflow.core.node.Node;
import ee.telekom.workflow.core.node.NodeService;
import ee.telekom.workflow.core.node.NodeStatus;
import ee.telekom.workflow.executor.consumer.WorkConsumerJob;
import ee.telekom.workflow.executor.plugin.WorkflowEnginePlugin;
import ee.telekom.workflow.executor.producer.WorkProducerJob;
import ee.telekom.workflow.executor.queue.WorkQueue;

@Component(LifecycleServiceImpl.BEAN)
public class LifecycleServiceImpl implements LifecycleService{

    // The bean's name is used to reference the bean in a test XML application context file.
    // Therefore, we explicitly set the bean name to a constant.
    public static final String BEAN = "lifecycleService";

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private WorkflowEnginePlugin plugin;
    @Autowired
    private WorkQueue queue;
    @Autowired
    private WorkConsumerJob consumerJob;
    @Autowired
    private WorkProducerJob producerJob;
    @Autowired
    private HealthCheckService healthCheckService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private LockService lockService;
    @Autowired
    private WorkflowEngineConfiguration config;

    private final Object monitor = new Object();
    private final AtomicBoolean isStarted = new AtomicBoolean( false );

    @Override
    public void startUp(){
        synchronized( monitor ){
            String nodeName = config.getNodeName();
            Node node = nodeService.findOrCreateByName( nodeName );
            log.info( "Running start-up with node status " + node.getStatus() );
            switch( node.getStatus() ) {
                case DISABLE:
                    // Should only happen if someone mark this node as DISABLED as long as it was offline.
                    stopIfRunning();
                    nodeService.markDisabled( node.getRefNum() );
                    break;
                case DISABLED:
                    // Nothing to be done
                    break;
                case ENABLE:
                    startIfNotRunning();
                    nodeService.markEnabled( node.getRefNum() );
                    break;
                case ENABLED:
                    nodeService.markFailed( node.getRefNum() );
                    healFailedNodesIfLockedOwned();
                    node = nodeService.findOrCreateByName( nodeName );
                    if( NodeStatus.ENABLE.equals( node.getStatus() ) ){
                        log.info( "Healed node" );
                        startIfNotRunning();
                        nodeService.markEnabled( node.getRefNum() );
                    }
                    break;
                case FAILED:
                    healFailedNodesIfLockedOwned();
                    node = nodeService.findOrCreateByName( nodeName );
                    if( NodeStatus.ENABLE.equals( node.getStatus() ) ){
                        log.info( "Healed node" );
                        startIfNotRunning();
                        nodeService.markEnabled( node.getRefNum() );
                    }
                    break;
                default :
                    throw new IllegalArgumentException( "Unknown status" );
            }
        }
    }

    @Override
    public void shutDown(){
        synchronized( monitor ){
            String nodeName = config.getNodeName();
            Node node = nodeService.findOrCreateByName( nodeName );
            log.info( "Running shut-down with node status " + node.getStatus() );
            switch( node.getStatus() ) {
                case DISABLE:
                    stopIfRunning();
                    nodeService.markDisabled( node.getRefNum() );
                    break;
                case DISABLED:
                    // Nothing to be done
                    break;
                case ENABLE:
                    stopIfRunning();
                    break;
                case ENABLED:
                    stopIfRunning();
                    nodeService.markEnable( node.getRefNum() );
                    break;
                case FAILED:
                    stopIfRunning();
                    break;
                default :
                    throw new IllegalArgumentException( "Unknown status" );
            }
        }
    }

    @Override
    public void doHeartBeat(){
        log.info( "Doing a heart beat" );
        nodeService.doHeartBeat();
    }

    @Override
    public void checkNodeStatus(){
        synchronized( monitor ){
            String nodeName = config.getNodeName();
            Node node = nodeService.findOrCreateByName( nodeName );
            log.info( "Running lifecycle check with node status " + node.getStatus() );

            // Step 1: Mark dead nodes as failed
            nodeService.markDeadNodesFailed();

            // Step 2: Recover stuck work from dead nodes and reset the status of FAILED nodes.
            healFailedNodesIfLockedOwned();

            // Step 3: Do we need to start-up or shutdown the engine?
            node = nodeService.findOrCreateByName( nodeName );
            if( NodeStatus.ENABLE.equals( node.getStatus() ) ){
                startIfNotRunning();
                nodeService.markEnabled( node.getRefNum() );
            }
            else if( NodeStatus.DISABLE.equals( node.getStatus() ) ){
                stopIfRunning();
                nodeService.markDisabled( node.getRefNum() );
            }
            else if( NodeStatus.ENABLED.equals( node.getStatus() ) ){
                if( !producerJob.isStarted() && lockService.eagerAcquire() ){
                    log.info( "This node recently acquired the master lock. Starting producer." );
                    producerJob.start();
                }
                else if( producerJob.isStarted() && !lockService.eagerAcquire() ){
                    log.info( "This node recently lost the master lock. Stopping producer." );
                    producerJob.stop();
                }
            }
            else if( NodeStatus.FAILED.equals( node.getStatus() ) ){
                // another node marked this node as failed. if we
                // happen to get to know this, we will shutdown
                stopIfRunning();
            }
        }
    }

    @Override
    public boolean isStarted(){
        return isStarted.get();
    }

    private void startIfNotRunning(){
        if( isStarted.get() ){
            log.info( "Start-up not required. Engine is already started." );
            return;
        }
        plugin.start();
        if( plugin.isStarted() ){
            log.info( "Plugin started successfully. Starting also other engine services." );
            queue.start();
            consumerJob.start();
            if( lockService.eagerAcquire() ){
                producerJob.start();
            }
            isStarted.set( true );
        }
    }

    private void stopIfRunning(){
        if( !isStarted.get() ){
            log.info( "Shut-down not required. Engine is not started." );
            return;
        }
        if( lockService.refreshOwnLock() ){
            producerJob.stop();
            lockService.releaseOwnLock();
        }
        consumerJob.stop();
        queue.stop();
        plugin.stop();
        isStarted.set( false );
    }

    private void healFailedNodesIfLockedOwned(){
        if( lockService.eagerAcquire() ){
            // this is the master node, try to fix everything
            healthCheckService.healFailedNodes();

            // check if there are still "stuck" workflow instances and log error to draw manual attention
            healthCheckService.checkForStuckWorkflows();
        }
    }

}
