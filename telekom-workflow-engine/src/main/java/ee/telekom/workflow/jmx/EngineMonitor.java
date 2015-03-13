package ee.telekom.workflow.jmx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.executor.consumer.WorkConsumerService;
import ee.telekom.workflow.executor.plugin.WorkflowEnginePlugin;
import ee.telekom.workflow.executor.producer.WorkProducerJob;
import ee.telekom.workflow.executor.queue.WorkQueue;

@Component(EngineMonitor.BEAN)
@ManagedResource
public class EngineMonitor{

    // The bean's name is used to reference the bean in an XML application context file.
    // Therefore, we explicitly set the bean name to a constant.
    public static final String BEAN = "engineMonitor";

    @Autowired
    private WorkflowEngineConfiguration config;
    @Autowired
    private WorkflowEnginePlugin plugin;
    @Autowired
    private WorkQueue queue;
    @Autowired
    private WorkProducerJob producerJob;
    @Autowired
    private WorkConsumerService consumerService;

    @ManagedAttribute(description = "Is plugin started")
    public boolean isPluginStarted(){
        return plugin.isStarted();
    }

    @ManagedAttribute(description = "Is queue started")
    public boolean isQueueStarted(){
        return queue.isStarted();
    }

    @ManagedAttribute(description = "Is producer started")
    public boolean isProducerStarted(){
        return producerJob.isStarted();
    }

    @ManagedAttribute(description = "Is producer suspended")
    public boolean isProducerSuspended(){
        return producerJob.isSuspended();
    }

    @ManagedAttribute(description = "Consumed work units")
    public long getConsumedWorkUnits(){
        return consumerService.getConsumedWorkUnits();
    }

}
