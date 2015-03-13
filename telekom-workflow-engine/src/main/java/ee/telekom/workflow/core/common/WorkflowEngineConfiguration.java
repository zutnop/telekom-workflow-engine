package ee.telekom.workflow.core.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hazelcast.config.MulticastConfig;

/**
 * Provides a common place to access configuration parameters.
 * <p>
 * Also provides default parameters for some of the values.
 * 
 * @author Christian Klock
 */
@Component
public class WorkflowEngineConfiguration{

    private static final String DEFAULT_CLUSTER_MULTICAST_GROUP = MulticastConfig.DEFAULT_MULTICAST_GROUP;
    private static final int DEFAULT_CLUSTER_MULTICAST_PORT = MulticastConfig.DEFAULT_MULTICAST_PORT;
    private static final int DEFAULT_CLUSTER_MULTICAST_TTL = 0;

    @Value("${workflowengine.cluster.name}")
    private String clusterName;
    @Value("${workflowengine.cluster.multicast.group}")
    private String clusterMulticastGroup;
    @Value("${workflowengine.cluster.multicast.port}")
    private String clusterMulticastPortText;
    private int clusterMulticastPort;
    @Value("${workflowengine.cluster.multicast.ttl}")
    private String clusterMulticastTtlText;
    private int clusterMulticastTtl;
    @Value("${workflowengine.node.name}")
    private String nodeName;
    @Value("${workflowengine.heartbeat.intervalSeconds}")
    private int heartbeatInterval;
    @Value("${workflowengine.heartbeat.maximumPauseSeconds}")
    private int heartbeatMaximumPauseSeconds;
    @Value("${workflowengine.maximumNodeAssignementTimeSeconds}")
    private int maximumNodeAssignmentTimeSeconds;
    @Value("${workflowengine.producer.intervalSeconds}")
    private int producerIntervalSeconds;
    @Value("${workflowengine.consumer.threads}")
    private int numberOfConsumerThreads;
    @Value("${workflowengine.pluginApplicationContextFile}")
    private String pluginApplicationContextFile;
    @Value("${workflowengine.developmentMode}")
    private boolean developmentMode;

    @PostConstruct
    public void init(){
        clusterMulticastGroup = getFirstNotEmpty( clusterMulticastGroup, DEFAULT_CLUSTER_MULTICAST_GROUP );
        clusterMulticastPort = getFirstNotEmpty( clusterMulticastPortText, DEFAULT_CLUSTER_MULTICAST_PORT );
        clusterMulticastTtl = getFirstNotEmpty( clusterMulticastTtlText, DEFAULT_CLUSTER_MULTICAST_TTL );
        nodeName = getFirstNotEmptyNodeName( nodeName );
    }

    /**
     * This parameter is used as the Hazelcast cluster group name. It is also used to
     * group workflow instances into groups. This leverages several clusters to run
     * against one database (e.g. a cluster for every developer and a test instance and
     * a prelive instance).
     */
    public String getClusterName(){
        return clusterName;
    }

    /**
     * This parameter is used for Hazelcasts cluster member auto-detection
     * and defaults to Hacelcasts multicast group default "224.2.2.3".
     */
    public String getClusterMulticastGroup(){
        return clusterMulticastGroup;
    }

    /**
     * This parameter is used for Hazelcasts cluster member auto-detection
     * and defaults to Hacelcasts multicast port default 54327.
     */
    public int getClusterMulticastPort(){
        return clusterMulticastPort;
    }

    /**
     * This parameter is used for Hazelcasts cluster member auto-detection
     * and defaults to 0. That means that multicast requests are restricted
     * to the same host.
     */
    public int getClusterMulticastTtl(){
        return clusterMulticastTtl;
    }

    /**
     * This parameter is used as name when indicating which cluster node is
     * currently executing a workflow instance.
     */
    public String getNodeName(){
        return nodeName;
    }

    /**
     * Must be a value less than workflowengine.heartbeat.maximumPauseSeconds.<br/>
     * Used as interval in seconds to run health check.
     * Update node's heart beat field is to current date (see database table and field node.heartbeat)
     * If master:
     * Refresh locks expire time (sets the lock expire time to current time + workflowengine.heartbeat.maximumPauseSeconds)
     * Find other nodes that are did not have a heartbeat for longer than workflowengine.heartbeat.maximumPauseSeconds and mark their status as failed
     * If slave: Test whether lock has expired and attempt to acquire it
     * If master: Run recovery
     */
    public int getHeartbeatInterval(){
        return heartbeatInterval;
    }

    /**
     * Must be a value greater than workflowengine.heartbeat.intervalSeconds.
     * Used as interval in seconds  to declare a node as FAILED if it is in ENALBED
     * state but did not have a heartbeat in the given interval
     * that the master lock is valid (afterwards it expires)
     *
     * @return
     */
    public int getHeartbeatMaximumPauseSeconds(){
        return heartbeatMaximumPauseSeconds;
    }

    /**
     * The maximum time a consumer is granted to update a workflow instance's
     * node_name field after taking a work unit from the queue.
     */
    public int getMaximumNodeAssignmentTimeSeconds(){
        return maximumNodeAssignmentTimeSeconds;
    }

    /**
     * The interval in seconds after which the poller attempts to find new work units.
     */
    public int getProducerIntervalSeconds(){
        return producerIntervalSeconds;
    }

    /**
     * The number of consumer thread started when the engine starts.
     */
    public int getNumberOfConsumerThreads(){
        return numberOfConsumerThreads;
    }

    /**
     * The Spring application context file for plugins. The context is started when
     * the engine starts and closed when it shuts down. It is used to resolve beans
     * by name during call and callAsync executions, find listeners to engine events,
     * find workflow definitions.
     */
    public String getPluginApplicationContextFile(){
        return pluginApplicationContextFile;
    }

    /**
     * Determines wether the engine is deployed in development mode (as opposed to production).
     */
    public boolean isDevelopmentMode(){
        return developmentMode;
    }

    private String getFirstNotEmpty( String configValue, String defaultValue ){
        return StringUtils.isNotBlank( configValue ) ? configValue : defaultValue;
    }

    private int getFirstNotEmpty( String configValue, int defaultValue ){
        return StringUtils.isNotBlank( configValue ) ? Integer.valueOf( configValue ) : defaultValue;
    }

    private String getFirstNotEmptyNodeName( String configValue ){
        return StringUtils.isNotBlank( configValue ) ? configValue : getHostName();
    }

    private static String getHostName(){
        try{
            return InetAddress.getLocalHost().getHostName();
        }
        catch( UnknownHostException e ){
            throw new RuntimeException( "Cannot determine host for this node", e );
        }
    }

}
