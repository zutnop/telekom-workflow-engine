package ee.telekom.workflow.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import ee.telekom.workflow.api.WorkflowDefinition;
import ee.telekom.workflow.api.WorkflowFactoryImpl;
import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.executor.plugin.WorkflowEnginePlugin;
import ee.telekom.workflow.graph.BeanResolver;
import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphInstanceEventListener;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.GraphWorkItemEventListener;
import ee.telekom.workflow.graph.NewGraphInstanceCreator;
import ee.telekom.workflow.graph.WorkItemStatus;
import ee.telekom.workflow.graph.WorkflowException;
import ee.telekom.workflow.graph.core.EnvironmentImpl;
import ee.telekom.workflow.graph.core.GraphEngineImpl;
import ee.telekom.workflow.graph.core.GraphRepositoryImpl;
import ee.telekom.workflow.util.CallUtil;

/**
 * Helper class for Workflow Unit tests
 *
 * @author Raido Türk
 */
public abstract class AbstractWorkflowApiTest{

    @Spy
    @InjectMocks
    protected AdapterBeanResolver beanResolver = new AdapterBeanResolver();

    GraphEngineImpl graphEngine;
    TestGraphEngineFactory factory;
    GraphInstance instance;
    @Mock
    private AdapterNewGraphInstanceCreator newGraphInstanceCreator;
    @Mock
    private AdapterEventListener listener;

    @Before
    public void setUpEngine(){
        instance = null;
        graphEngine = new GraphEngineImpl();
        graphEngine.setBeanResolver( beanResolver );
        graphEngine.setRepository( new GraphRepositoryImpl() );
        graphEngine.setNewGraphInstanceCreator( newGraphInstanceCreator );
        graphEngine.registerInstanceEventListener( listener );
        graphEngine.registerWorkItemEventListener( listener );
        factory = new TestGraphEngineFactory( graphEngine, mock( WorkflowEngineConfiguration.class ), mock( WorkflowEnginePlugin.class ) );
    }

    protected void addGraphAndStartInstance( WorkflowDefinition definition, Map<String, Object> inputAttributes ){
        addGraph( definition );
        instance = factory.getSingletonInstance().start( definition.getName(), definition.getVersion(), createEnvironment( inputAttributes ), 14l );
    }

    protected void terminateTimer(){
        GraphWorkItem item = findActiveTimer( instance );
        graphEngine.complete( item );
    }

    protected void sendSignal( String signal ){
        GraphWorkItem item = findActiveSignal( instance, signal );
        graphEngine.complete( item );
    }

    protected void sendSignalWithResult( String signal, Object result ){
        GraphWorkItem item = findActiveSignal( instance, signal );
        item.setResult( result );
        graphEngine.complete( item );
    }

    protected void invokeAsyncCall(){
        GraphWorkItem item = findActiveAsyncCall( instance );
        Object returnValue = CallUtil.call( beanResolver.getBean( item.getBean() ), item.getMethod(), item.getTaskArguments() );
        item.setResult( returnValue );
        graphEngine.complete( item );
    }

    protected void completeHumanTask(){
        GraphWorkItem item = findActiveHumanTask( instance );
        graphEngine.complete( item );
    }

    protected void completeHumanTaskWithResult( Object result ){
        GraphWorkItem item = findActiveHumanTask( instance );
        item.setResult( result );
        graphEngine.complete( item );
    }

    protected void assertInstanceCompleted(){
        assertTrue( instance.isCompleted() );
    }

    protected void assertActiveWorkItemsCount( int count ){
        assertEquals( count, getActiveWorkItems( instance ).size() );
    }

    protected void assertEnvironmentContainsValues( Map<String, Object> expectedEnvironmentValues ){
        Environment environment = instance.getEnvironment();
        for( String key : expectedEnvironmentValues.keySet() ){
            if( environment.containsAttribute( key ) ){
                assertEquals( expectedEnvironmentValues.get( key ), environment.getAttribute( key ) );
            }
        }
    }

    protected void verifyNewInstanceCreation( String instanceName ){
        verify( newGraphInstanceCreator, times( 1 ) ).create( eq( instanceName ), anyInt(), anyString(), anyString(), any( Environment.class ) );
    }

    private void addGraph( WorkflowDefinition definition ){
        WorkflowFactoryImpl workflowFactory = new WorkflowFactoryImpl( definition.getName(), definition.getVersion() );
        definition.configureWorkflowDefinition( workflowFactory );
        Graph graph = workflowFactory.buildGraph();
        factory.getSingletonInstance().getRepository().addGraph( graph );
    }

    private List<GraphWorkItem> getActiveWorkItems( GraphInstance instance ){
        List<GraphWorkItem> result = new LinkedList<>();
        for( GraphWorkItem wi : instance.getWorkItems() ){
            if( !WorkItemStatus.COMPLETED.equals( wi.getStatus() )
                    && !WorkItemStatus.CANCELLED.equals( wi.getStatus() ) ){
                result.add( wi );
            }
        }
        return result;
    }

    private GraphWorkItem findActiveSignal( GraphInstance graphInstance, String signal ){
        for( GraphWorkItem workItem : getActiveWorkItems( graphInstance ) ){
            if( signal != null && signal.equals( workItem.getSignal() ) ){
                return workItem;
            }
        }
        throw new WorkflowException( "No active signal work item found" );
    }


    private GraphWorkItem findActiveTimer( GraphInstance graphInstance ){
        for( GraphWorkItem workItem : getActiveWorkItems( graphInstance ) ){
            if( workItem.getDueDate() != null ){
                return workItem;
            }
        }
        throw new WorkflowException( "No active timer work item found" );
    }

    private GraphWorkItem findActiveAsyncCall( GraphInstance graphInstance ){
        for( GraphWorkItem workItem : getActiveWorkItems( graphInstance ) ){
            if( workItem.getBean() != null && workItem.getMethod() != null ){
                return workItem;
            }
        }
        throw new WorkflowException( "No active async call work item found" );
    }


    private GraphWorkItem findActiveHumanTask( GraphInstance graphInstance ){
        for( GraphWorkItem workItem : getActiveWorkItems( graphInstance ) ){
            if( workItem.getRole() != null || workItem.getUser() != null ){
                return workItem;
            }
        }
        throw new WorkflowException( "No active human task work item found" );
    }

    private EnvironmentImpl createEnvironment( Map<String, Object> inputAttributes ){
        EnvironmentImpl environment = new EnvironmentImpl();
        if( inputAttributes != null ){
            for( String key : inputAttributes.keySet() ){
                environment.setAttribute( key, inputAttributes.get( key ) );
            }
        }
        return environment;
    }

    protected class AdapterBeanResolver implements BeanResolver{

        @Override
        public Object getBean( String name ){
            throw new IllegalStateException( "Bean '" + name + "' has not been mocked!" );
        }

    }

    private class AdapterNewGraphInstanceCreator implements NewGraphInstanceCreator{

        @Override
        public void create( String graphName, Integer graphVersion, String label1, String label2, Environment initialEnvironment ){
        }

    }

    private class AdapterEventListener implements GraphInstanceEventListener, GraphWorkItemEventListener{

        @Override
        public void onCreated( GraphInstance instance ){
        }

        @Override
        public void onStarted( GraphInstance instance ){
        }

        @Override
        public void onAborting( GraphInstance instance ){
        }

        @Override
        public void onAborted( GraphInstance instance ){
        }

        @Override
        public void onCompleted( GraphInstance instance ){
        }

        @Override
        public void onCreated( GraphWorkItem workItem ){
        }

        @Override
        public void onCancelled( GraphWorkItem workItem ){
        }

        @Override
        public void onCompleted( GraphWorkItem workItem ){
        }

    }

}
