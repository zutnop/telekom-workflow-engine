package ee.telekom.workflow.executor;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.executor.plugin.WorkflowEnginePlugin;
import ee.telekom.workflow.graph.BeanResolver;
import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphEngineFacade;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphInstanceEventListener;
import ee.telekom.workflow.graph.GraphRepository;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.GraphWorkItemEventListener;
import ee.telekom.workflow.graph.NewGraphInstanceCreator;
import ee.telekom.workflow.graph.core.GraphEngineImpl;

/**
 * Provides a factory for a {@link GraphEngine} singleton and convenience methods to manipulate that
 * singleton.
 * <p>
 * The returned singleton graph engine links the non-persisted "graph world" from the ee.telekom.workflow.graph packages 
 * with the persisted "core world" form the ee.telekom.workflow.core packages. It provides adapters for the 
 * engine's customization hooks that are provided by the plugin.
 *  
 * @author Christian Klock
 */
@Component
public class GraphEngineFactory{

    @Autowired
    protected WorkflowEnginePlugin plugin;
    @Autowired
    protected WorkflowInstanceService workflowInstanceService;
    @Autowired
    protected WorkflowEngineConfiguration configuration;

    protected GraphEngineImpl singleton;
    private AdapterGraphRepository repository = new AdapterGraphRepository();
    private AdapterBeanResolver resolver = new AdapterBeanResolver();
    private AdapterNewGraphInstanceCreator instanceCreator = new AdapterNewGraphInstanceCreator();
    private AdapterEventListener listener = new AdapterEventListener();

    public GraphEngineFactory(){
        singleton = new GraphEngineImpl();
        singleton.setRepository( repository );
        singleton.setBeanResolver( resolver );
        singleton.setNewGraphInstanceCreator( instanceCreator );
        singleton.registerInstanceEventListener( listener );
        singleton.registerWorkItemEventListener( listener );
    }

    public GraphEngineFacade getSingletonInstance(){
        return singleton;
    }

    public Graph getGraph( String name, Integer version ){
        if( configuration.isDevelopmentMode() ){
            plugin.reloadWorkflowDefinitions();
        }
        return repository.getGraph( name, version );
    }

    public Set<Graph> getGraphs(){
        if( configuration.isDevelopmentMode() ){
            plugin.reloadWorkflowDefinitions();
        }
        if( plugin.getGraphRepository() != null ){
            return plugin.getGraphRepository().getGraphs();
        }
        else{
            return Collections.emptySet();
        }
    }

    private class AdapterGraphRepository implements GraphRepository{

        @Override
        public Graph getGraph( String name, Integer version ){
            return plugin.getGraphRepository().getGraph( name, version );
        }

        @Override
        public Set<Graph> getGraphs( String name ){
            return plugin.getGraphRepository().getGraphs( name );
        }

        @Override
        public Set<Graph> getGraphs(){
            return plugin.getGraphRepository().getGraphs();
        }

        @Override
        public void addGraph( Graph graph ){
            plugin.getGraphRepository().addGraph( graph );
        }

    }

    private class AdapterBeanResolver implements BeanResolver{

        @Override
        public Object getBean( String name ){
            return plugin.getBean( name );
        }

    }

    private class AdapterNewGraphInstanceCreator implements NewGraphInstanceCreator{

        @Override
        public void create( String graphName, Integer graphVersion, String label1, String label2, Environment initialEnvironment ){
            Map<String, Object> attributes = initialEnvironment.getAttributesAsMap();
            workflowInstanceService.create( graphName, graphVersion, attributes, label1, label2 );
        }

    }

    private class AdapterEventListener implements GraphInstanceEventListener, GraphWorkItemEventListener{

        @Override
        public void onCreated( GraphInstance instance ){
            plugin.onWorkflowInstanceCreated( instance );
        }

        @Override
        public void onStarted( GraphInstance instance ){
            // Ignore
        }

        @Override
        public void onAborting( GraphInstance instance ){
            // Ignore
        }

        @Override
        public void onAborted( GraphInstance instance ){
            plugin.onWorkflowInstanceAborted( instance );
        }

        @Override
        public void onCompleted( GraphInstance instance ){
            plugin.onWorkflowInstanceCompleted( instance );
        }

        @Override
        public void onCreated( GraphWorkItem workItem ){
            if( isHumanTaskWorkItem( workItem ) ){
                plugin.onHumanTaskCreated( workItem );
            }
        }

        @Override
        public void onCancelled( GraphWorkItem workItem ){
            if( isHumanTaskWorkItem( workItem ) ){
                plugin.onHumanTaskCancelled( workItem );
            }
        }

        @Override
        public void onCompleted( GraphWorkItem workItem ){
            if( isHumanTaskWorkItem( workItem ) ){
                plugin.onHumanTaskCompleted( workItem );
            }
        }

        private boolean isHumanTaskWorkItem( GraphWorkItem workItem ){
            return workItem.getRole() != null || workItem.getUser() != null;
        }

    }

}
