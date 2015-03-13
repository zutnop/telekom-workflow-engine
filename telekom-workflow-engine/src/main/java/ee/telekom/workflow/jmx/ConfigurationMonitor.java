package ee.telekom.workflow.jmx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;

@Component(ConfigurationMonitor.BEAN)
@ManagedResource
public class ConfigurationMonitor{

    // The bean's name is used to reference the bean in an XML application context file.
    // Therefore, we explicitly set the bean name to a constant.
    public static final String BEAN = "configurationMonitor";

    @Autowired
    private WorkflowEngineConfiguration config;

    @ManagedAttribute(description = "Cluster name")
    public String getClusterName(){
        return config.getClusterName();
    }

    @ManagedAttribute(description = "Cluster multicast group")
    public String getClusterMulticastGroup(){
        return config.getClusterMulticastGroup();
    }

    @ManagedAttribute(description = "Cluster multicast port")
    public int getClusterMulticastPort(){
        return config.getClusterMulticastPort();
    }

    @ManagedAttribute(description = "Cluster multicast TTL")
    public int getClusterMulticastTtl(){
        return config.getClusterMulticastTtl();
    }

    @ManagedAttribute(description = "Heartbeat interval in seconds")
    public int getHeartbeatInterval(){
        return config.getHeartbeatInterval();
    }

    @ManagedAttribute(description = "Heartbeat maximum pause in seconds")
    public int getHeartbeatMaximumPause(){
        return config.getHeartbeatMaximumPauseSeconds();
    }

    @ManagedAttribute(description = "Maximum node assignment time in seconds")
    public int getMaximumNodeAssignmentTime(){
        return config.getMaximumNodeAssignmentTimeSeconds();
    }

    @ManagedAttribute(description = "Producer interval in seconds")
    public int getProducerInterval(){
        return config.getProducerIntervalSeconds();
    }

    @ManagedAttribute(description = "Number of consumer threads")
    public int getNumberOfConsumerThreads(){
        return config.getNumberOfConsumerThreads();
    }

    @ManagedAttribute(description = "Plugin application context file name")
    public String getPluginApplicationContextFile(){
        return config.getPluginApplicationContextFile();
    }

    @ManagedAttribute(description = "Engine deployed in development mode?")
    public boolean isDevelopmentMode(){
        return config.isDevelopmentMode();
    }

}
