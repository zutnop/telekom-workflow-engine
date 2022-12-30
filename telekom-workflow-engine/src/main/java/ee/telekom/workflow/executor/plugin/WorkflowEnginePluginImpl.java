package ee.telekom.workflow.executor.plugin;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.api.WorkflowDefinition;
import ee.telekom.workflow.api.WorkflowFactoryImpl;
import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphRepository;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.core.GraphRepositoryImpl;
import ee.telekom.workflow.listener.HumanTaskEvent;
import ee.telekom.workflow.listener.HumanTaskEventListener;
import ee.telekom.workflow.listener.WorkflowInstanceEvent;
import ee.telekom.workflow.listener.WorkflowInstanceEventListener;
import ee.telekom.workflow.util.JsonUtil;

@Component(WorkflowEnginePluginImpl.BEAN)
@Transactional
public class WorkflowEnginePluginImpl implements WorkflowEnginePlugin{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    public static final String BEAN = "plugin";

    @Autowired
    private ApplicationContext engineApplicationContext;
    @Autowired
    private WorkflowEngineConfiguration config;

    private volatile ClassPathXmlApplicationContext pluginApplicationContext;
    private GraphRepository repository;
    private Collection<WorkflowInstanceEventListener> workflowInstanceEventListeners;
    private Collection<HumanTaskEventListener> humanTaskEventListeners;

    @Override
    public synchronized void start(){
        if( isPluginApplicationContextAvailable() ){
            try{
                pluginApplicationContext = new ClassPathXmlApplicationContext( new String[]{config.getPluginApplicationContextFile()}, engineApplicationContext );
                repository = createGraphRepository();
                workflowInstanceEventListeners = getAllBeansOfType( WorkflowInstanceEventListener.class );
                humanTaskEventListeners = getAllBeansOfType( HumanTaskEventListener.class );
            }
            catch( Exception e ){
                log.error( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
                log.error( "!! The workflow engine could not start the configured plugin !!" );
                log.error( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
                log.error( e.getMessage(), e );
            }
        }
        else{
            log.warn( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
            log.warn( "!! The workflow engine is deployed without a plugin application context.                             !!" );
            log.warn( "!! In a production environment, this is most likely a packaging problem or a configuration error.    !!" );
            log.warn( "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
        }
    }

    private boolean isPluginApplicationContextAvailable(){
        ClassPathResource r = new ClassPathResource( config.getPluginApplicationContextFile() );
        if( r.exists() ){
            try{
                log.debug( "Found plugin application context at " + r.getURL().toExternalForm() );
            }
            catch( IOException e ){
                log.debug( "Cannot find/access plugin application context file at " + config.getPluginApplicationContextFile() );
            }
        }
        return r.exists();
    }

    @Override
    public synchronized void stop(){
        if( isStarted() ){
            pluginApplicationContext.close();
            pluginApplicationContext = null;
            repository = null;
            workflowInstanceEventListeners = null;
            humanTaskEventListeners = null;
        }
    }

    @Override
    public synchronized boolean isStarted(){
        return pluginApplicationContext != null;
    }

    @Override
    public GraphRepository getGraphRepository(){
        return repository;
    }

    @Override
    public Object getBean( String name ){
        return pluginApplicationContext.getBean( name );
    }

    @Override
    public void reloadWorkflowDefinitions(){
        repository = createGraphRepository();
    }

    @Override
    public void onWorkflowInstanceCreated( GraphInstance instance ){
        for( WorkflowInstanceEventListener listener : getWorkflowInstanceEventListeners() ){
            listener.onStarting( createEvent( instance ) );
        }
    }

    @Override
    public void onWorkflowInstanceCompleted( GraphInstance instance ){
        for( WorkflowInstanceEventListener listener : getWorkflowInstanceEventListeners() ){
            listener.onExecuted( createEvent( instance ) );
        }
    }

    @Override
    public void onWorkflowInstanceAborted( GraphInstance instance ){
        for( WorkflowInstanceEventListener listener : getWorkflowInstanceEventListeners() ){
            listener.onAborted( createEvent( instance ) );
        }
    }

    @Override
    public void onHumanTaskCreated( GraphWorkItem workItem ){
        for( HumanTaskEventListener listener : getHumanTaskEventListeners() ){
            listener.onCreated( createEvent( workItem ) );
        }
    }

    @Override
    public void onHumanTaskCompleted( GraphWorkItem workItem ){
        for( HumanTaskEventListener listener : getHumanTaskEventListeners() ){
            listener.onCompleted( createEvent( workItem ), workItem.getResult() );
        }
    }

    @Override
    public void onHumanTaskCancelled( GraphWorkItem workItem ){
        for( HumanTaskEventListener listener : getHumanTaskEventListeners() ){
            listener.onCancelled( createEvent( workItem ) );
        }
    }

    @Override
    public void onHumanTaskCancelled( WorkflowInstance workflowInstance, WorkItem workItem ){
        for( HumanTaskEventListener listener : getHumanTaskEventListeners() ){
            listener.onCancelled( createEvent( workflowInstance, workItem ) );
        }
    }

    private GraphRepository createGraphRepository(){
        GraphRepository repository = new GraphRepositoryImpl();
        Collection<WorkflowDefinition> workflowDefinitions = getAllBeansOfType( WorkflowDefinition.class );
        for( WorkflowDefinition workflowDefinition : workflowDefinitions ){
            WorkflowFactoryImpl factory = new WorkflowFactoryImpl( workflowDefinition.getName(), workflowDefinition.getVersion(),
                    workflowDefinition.getKeepHistory(), workflowDefinition.getArchiveDuration() );
            workflowDefinition.configureWorkflowDefinition( factory );
            Graph graph = factory.buildGraph();
            repository.addGraph( graph );
        }
        return repository;
    }

    private WorkflowInstanceEvent createEvent( GraphInstance instance ){
        long woinRefNum = instance.getExternalId();
        String workflowName = instance.getGraph().getName();
        Integer workflowVersion = instance.getGraph().getVersion();
        Map<String, Object> attributes = instance.getEnvironment().getAttributesAsMap();
        return new WorkflowInstanceEvent( woinRefNum, workflowName, workflowVersion, attributes );
    }

    private HumanTaskEvent createEvent( GraphWorkItem workItem ){
        long woinRefNum = workItem.getExternalGraphInstanceId();
        String workflowName = workItem.getToken().getInstance().getGraph().getName();
        Integer workflowVersion = workItem.getToken().getInstance().getGraph().getVersion();
        // We cannot use workItem.getExternalId() here because this is not yet known for onCreated events.
        // This is because the external id is assigned during marshaling AFTER this listener is called.
        // Nevertheless, we may use the fact that their is only one active work item per token. Thus,
        // the token id gives us a unique identifier.
        int tokenId = workItem.getToken().getId();
        String role = workItem.getRole();
        String user = workItem.getUser();
        Map<String, Object> arguments = workItem.getHumanTaskArguments();
        return new HumanTaskEvent( woinRefNum, workflowName, workflowVersion, tokenId, role, user, arguments );
    }

    private HumanTaskEvent createEvent( WorkflowInstance workflowInstance, WorkItem workItem ){
        long woinRefNum = workItem.getWoinRefNum();
        String workflowName = workflowInstance.getWorkflowName();
        Integer workflowVersion = workflowInstance.getWorkflowVersion();
        int tokenId = workItem.getTokenId();
        String role = workItem.getRole();
        String user = workItem.getUserName();
        Map<String, Object> arguments = JsonUtil.deserializeHashMap( workItem.getArguments(), String.class, Object.class );
        return new HumanTaskEvent( woinRefNum, workflowName, workflowVersion, tokenId, role, user, arguments );
    }

    private Collection<WorkflowInstanceEventListener> getWorkflowInstanceEventListeners(){
        return workflowInstanceEventListeners;
    }

    private Collection<HumanTaskEventListener> getHumanTaskEventListeners(){
        return humanTaskEventListeners;
    }

    private <T> Collection<T> getAllBeansOfType( Class<T> clazz ){
        if( pluginApplicationContext != null ){
            return pluginApplicationContext.getBeansOfType( clazz ).values();
        }
        return Collections.emptyList();
    }

}
