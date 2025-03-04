package ee.telekom.workflow.core.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
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

    private static final String DEFAULT_HAZELCAST_INSTANCE_NAME = "telekomWorkflowEngineHazelcast";
    private static final String DEFAULT_CLUSTER_MULTICAST_GROUP = MulticastConfig.DEFAULT_MULTICAST_GROUP;
    private static final int DEFAULT_CLUSTER_MULTICAST_PORT = MulticastConfig.DEFAULT_MULTICAST_PORT;
    private static final int DEFAULT_CLUSTER_MULTICAST_TTL = 0;

    @Value("${workflowengine.enabled:true}")
    private boolean enabled;
    @Value("${database.workflowengine.schema:engine}")
    private String schema;
    @Value("${workflowengine.cluster.hazelcast.name}")
    private String clusterHazelcastName;
    @Value("${workflowengine.cluster.name}")
    private String clusterName;
    @Value("${workflowengine.cluster.multicast.group}")
    private String clusterMulticastGroup;
    @Value("${workflowengine.cluster.multicast.port}")
    private String clusterMulticastPortText;
    @Value("${workflowengine.cluster.multicast.ttl}")
    private String clusterMulticastTtlText;
    @Value("${workflowengine.node.name}")
    private String nodeName;
    private String hostName;
    @Value("${workflowengine.heartbeat.intervalSeconds}")
    private int heartbeatInterval;
    @Value("${workflowengine.heartbeat.maximumPauseSeconds}")
    private int heartbeatMaximumPauseSeconds;
    @Value("${workflowengine.maximumNodeAssignementTimeSeconds}")
    private int maximumNodeAssignmentTimeSeconds;
    @Value("${workflowengine.workItemExecutionTimeWarnSeconds}")
    private int workItemExecutionTimeWarnSeconds;
    @Value("${workflowengine.producer.intervalSeconds}")
    private int producerIntervalSeconds;
    @Value("${workflowengine.consumer.threads}")
    private int numberOfConsumerThreads;
    @Value("${workflowengine.pluginApplicationContextFile}")
    private String pluginApplicationContextFile;
    @Value("${workflowengine.developmentMode}")
    private boolean developmentMode;
    @Value("${workflowengine.console.mapping.prefix:}")
    private String consoleMappingPrefix;
    @Value("${workflowengine.environment}")
    private String environment;
    @Value("${workflowengine.embeddedNavigationMode}")
    private boolean embeddedNavigationMode;

    @PostConstruct
    public void init(){
        hostName = getHostName();
    }

    /**
     * Determines if during Spring Framework bootup the lifecycle services will be started and the engine will start working
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the PostgreSQL schema name + "." for workflow engine tables or an empty string if not configured. Used for constructing SQL queries.
     */
    public String getSchema() {
        return StringUtils.isNotBlank(schema) ? (schema + ".") : "";
    }

    /**
     * This parameter is used as the Hazelcast instance bean name.
     */
    public String getClusterHazelcastName(){
        return StringUtils.isNotBlank(clusterHazelcastName) ? clusterHazelcastName : DEFAULT_HAZELCAST_INSTANCE_NAME;
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
        return StringUtils.isNotBlank(clusterMulticastGroup) ? clusterMulticastGroup : DEFAULT_CLUSTER_MULTICAST_GROUP;
    }

    /**
     * This parameter is used for Hazelcasts cluster member auto-detection
     * and defaults to Hacelcasts multicast port default 54327.
     */
    public int getClusterMulticastPort(){
        return StringUtils.isNotBlank( clusterMulticastPortText ) ? Integer.valueOf( clusterMulticastPortText ) : DEFAULT_CLUSTER_MULTICAST_PORT;
    }

    /**
     * This parameter is used for Hazelcasts cluster member auto-detection
     * and defaults to 0. That means that multicast requests are restricted
     * to the same host.
     */
    public int getClusterMulticastTtl(){
        return StringUtils.isNotBlank( clusterMulticastTtlText ) ? Integer.valueOf( clusterMulticastTtlText ) : DEFAULT_CLUSTER_MULTICAST_TTL;
    }

    /**
     * This parameter is used as name when indicating which cluster node is
     * currently executing a workflow instance.
     */
    public String getNodeName(){
        return StringUtils.isNotBlank(nodeName) ? nodeName : hostName;
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
     * The threshold time limit, if the regular health check finds that there are workflow instances that are in the locked state without updates (meaning that
     * a node is executing some work item for this instance) for longer than this time, then an ERROR is logged to draw attention to potentially stuck workflows.
     *
     * NB! The workflow instance execution itself is NOT affected by this mechanism, just logging.
     */
    public int getWorkItemExecutionTimeWarnSeconds(){
        return workItemExecutionTimeWarnSeconds;
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
     * Determines whether the engine is deployed in development mode (as opposed to production).
     */
    public boolean isDevelopmentMode(){
        return developmentMode;
    }

    public String getConsoleMappingPrefix() {
        return consoleMappingPrefix != null ? consoleMappingPrefix : "";
    }

    /**
     * Environment name, will be displayed (when not empty) in web console after application name
     */
    public String getEnvironment(){
        return environment;
    }

    /**
     * Determines if the header logo and navigation links act as a stand-alone web app (default), or as embedded web screens.
     */
    public boolean isEmbeddedNavigationMode() {
		return embeddedNavigationMode;
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
