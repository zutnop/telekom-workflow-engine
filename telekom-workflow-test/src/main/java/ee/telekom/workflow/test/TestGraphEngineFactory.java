package ee.telekom.workflow.test;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.executor.GraphEngineFactory;
import ee.telekom.workflow.executor.plugin.WorkflowEnginePlugin;
import ee.telekom.workflow.graph.core.GraphEngineImpl;

/**
 * @author Raido Türk
 */
public class TestGraphEngineFactory extends GraphEngineFactory{

    public TestGraphEngineFactory( GraphEngineImpl graphEngine, WorkflowEngineConfiguration configuration, WorkflowEnginePlugin plugin ){
        singleton = graphEngine;
        this.configuration = configuration;
        this.plugin = plugin;
    }

}
