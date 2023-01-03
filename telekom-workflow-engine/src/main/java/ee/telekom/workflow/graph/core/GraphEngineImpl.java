package ee.telekom.workflow.graph.core;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import ee.telekom.workflow.api.AutoRecovery;
import ee.telekom.workflow.graph.BeanResolver;
import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphInstanceEventListener;
import ee.telekom.workflow.graph.GraphRepository;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.GraphWorkItemEventListener;
import ee.telekom.workflow.graph.NewGraphInstanceCreator;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.NodeEventListener;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.Transition;
import ee.telekom.workflow.graph.WorkItemStatus;
import ee.telekom.workflow.graph.WorkflowException;

public class GraphEngineImpl implements GraphEngine{

    private GraphRepository repository = new GraphRepositoryImpl();
    private BeanResolver beanResolver;
    private NewGraphInstanceCreator newGraphInstanceCreator;
    private EventNotifier notifier = new EventNotifier();

    @Override
    public GraphInstanceImpl start( String graphName ){
        return start( graphName, null, null, null );
    }

    @Override
    public GraphInstanceImpl start( String graphName, Integer version ){
        return start( graphName, null, null );
    }

    @Override
    public GraphInstanceImpl start( String graphName, Integer version, Environment initialEnvironment ){
        return start( graphName, version, initialEnvironment, null );
    }

    @Override
    public GraphInstanceImpl start( String graphName, Integer version, Environment initialEnvironment, Long externalId ){
        if( graphName == null ){
            throw new WorkflowException( "Can not start workflow for a null graph name" );
        }
        Graph graph = getRepository().getGraph( graphName, version );
        if( graph == null ){
            throw new WorkflowException( "No graph found with name '" + graphName + "'" );
        }
        return start( graph, initialEnvironment, externalId );
    }

    @Override
    public GraphInstanceImpl start( Graph graph ){
        return start( graph, null );
    }

    @Override
    public GraphInstanceImpl start( Graph graph, Environment initialEnvironment ){
        return start( graph, initialEnvironment, null );
    }

    @Override
    public GraphInstanceImpl start( Graph graph, Environment initialEnvironment, Long externalId ){
        if( graph == null ){
            throw new WorkflowException( "Can not workflow for a null graph" );
        }
        GraphInstanceImpl instance = new GraphInstanceImpl( graph );
        instance.setExternalId( externalId );
        if( initialEnvironment != null ){
            instance.getEnvironment().importEnvironment( initialEnvironment );
        }
        notifier.fireCreated( instance );
        start( instance );
        return instance;
    }

    private void start( GraphInstance instance ){
        Node node = instance.getGraph().getStartNode();
        if( node != null ){
            Token mainToken = addToken( instance, node, null );
            instance.addToExecutionQueue( mainToken );
        }
        instance.addToHistory( "start" );
        execute( instance );
        notifier.fireStarted( instance );
        if( instance.isCompleted() ){
            notifier.fireCompleted( instance );
        }
    }

    @Override
    public void abort( GraphInstance instance ){
        notifier.fireAborting( instance );
        instance.addToHistory( "abort" );
        for( Token token : instance.getActiveTokens() ){
            // preventing double-cancelling
            if( token.isActive() ){
                token.getNode().cancel( this, token );
                token.markInactive();
            }
        }
        instance.addToHistory( "aborted" );
        notifier.fireAborted( instance );
    }

    @Override
    public void complete( GraphWorkItem workItem ){
        Token token = workItem.getToken();
        GraphInstance instance = token.getInstance();
        instance.addToHistory( "continue:" + token.getId() );
        instance.addToExecutionQueue( token );
        complete( token, workItem.getResult() );
        execute( instance );
        if( instance.isCompleted() ){
            notifier.fireCompleted( instance );
        }
    }

    @Override
    public GraphRepository getRepository(){
        return repository;
    }

    public void setRepository( GraphRepository repository ){
        this.repository = repository;
    }

    @Override
    public BeanResolver getBeanResolver(){
        if( beanResolver == null ){
            throw new WorkflowException( "This engine does not provide a BeanResolver." );
        }
        return beanResolver;
    }

    public void setBeanResolver( BeanResolver beanResolver ){
        this.beanResolver = beanResolver;
    }

    @Override
    public NewGraphInstanceCreator getNewGraphInstanceCreator(){
        if( newGraphInstanceCreator == null ){
            throw new WorkflowException( "This engine does not provide a NewGraphInstanceCreator." );
        }
        return newGraphInstanceCreator;
    }

    public void setNewGraphInstanceCreator( NewGraphInstanceCreator newGraphInstanceCreator ){
        this.newGraphInstanceCreator = newGraphInstanceCreator;
    }

    @Override
    public void registerInstanceEventListener( GraphInstanceEventListener listener ){
        notifier.registerInstanceEventListener( listener );
    }

    @Override
    public void registerWorkItemEventListener( GraphWorkItemEventListener listener ){
        notifier.registerWorkItemEventListener( listener );
    }

    @Override
    public void registerNodeEventListener( NodeEventListener listener ){
        notifier.registerNodeEventListener( listener );
    }

    private void execute( GraphInstance instance ){
        Token token = instance.getFirstFromExecutionQueue();
        while( token != null ){
            boolean execute = true;
            while( execute ){
                if( token.isActive() ){
                    Node node = token.getNode();
                    notifier.fireEntering( token, node );
                    instance.addToHistory( token.getId() + ":" + node.getId() + "!" );
                    node.execute( this, token );
                    execute = node != token.getNode();
                }
                else{
                    execute = false;
                }
            }
            instance.removeFirstFromExecutionQueue();
            token = instance.getFirstFromExecutionQueue();
        }
        if( instance.isCompleted() ){
            instance.addToHistory( "executed" );
        }
        else{
            instance.addToHistory( "waitstate" );
        }
    }

    @Override
    public TokenImpl addToken( GraphInstance instance, Node node, Token parent ){
        TokenImpl token = new TokenImpl();
        token.setId( instance.nextTokenId() );
        token.setNode( node );
        token.setInstance( instance );
        token.setParent( parent );
        token.setActive( true );
        instance.addToken( token );
        return token;

    }

    @Override
    public GraphWorkItem addSignalItem( GraphInstance instance, Token token, String signal ){
        GraphWorkItem wi = GraphWorkItemImpl.createSignalItem( token, signal );
        instance.addWorkItem( wi );
        notifier.fireCreated( wi );
        return wi;
    }

    @Override
    public GraphWorkItem addTimerItem( GraphInstance instance, Token token, Date dueDate ){
        GraphWorkItem wi = GraphWorkItemImpl.createTimerItem( token, dueDate );
        instance.addWorkItem( wi );
        notifier.fireCreated( wi );
        return wi;
    }

    @Override
    public GraphWorkItem addTaskItem( GraphInstance instance, Token token, String bean, String method, AutoRecovery autoRecovery, Object[] arguments ){
        GraphWorkItem wi = GraphWorkItemImpl.createTaskItem( token, bean, method, autoRecovery, arguments );
        instance.addWorkItem( wi );
        notifier.fireCreated( wi );
        return wi;
    }

    @Override
    public GraphWorkItem addHumanTaskItem( GraphInstance instance, Token token, String role, String user, Map<String, Object> arguments ){
        GraphWorkItem wi = GraphWorkItemImpl.createHumanTaskItem( token, role, user, arguments );
        instance.addWorkItem( wi );
        notifier.fireCreated( wi );
        return wi;
    }

    @Override
    public void cancelWorkItem( Token token ){
        GraphInstance instance = token.getInstance();
        GraphWorkItem wi = instance.getActiveWorkItem( token );
        if( wi != null ){
            wi.setStatus( WorkItemStatus.CANCELLED );
            notifier.fireCancelled( wi );
        }
    }

    @Override
    public void complete( Token token, Object result ){
        complete( token, result, Transition.DEFAULT_TRANSITION_NAME );
    }

    @Override
    public void complete( Token token, Object result, String transitionName ){
        GraphInstance instance = token.getInstance();
        Graph graph = instance.getGraph();
        Node node = token.getNode();

        // completing work item
        GraphWorkItem wi = instance.getActiveWorkItem( token );
        if( wi != null ){
            wi.setStatus( WorkItemStatus.COMPLETED );
            notifier.fireCompleted( wi );
        }

        // storing result in environment
        node.store( instance.getEnvironment(), result );

        // finding next node
        Transition transition = graph.getOutputTransitions( node, transitionName );
        Node nextNode = transition == null ? null : transition.getEndNode();
        if( nextNode != null ){
            instance.addToHistory( token.getId() + ":" + node.getId() + "=>" + nextNode.getId() );
            token.setNode( nextNode );
            if( token != instance.getFirstFromExecutionQueue() ){
                instance.addToExecutionQueue( token );
            }
        }
        else if( graph.getOutputTransitions( node ).isEmpty() ){
            // Node is implicit end node
            terminate( token );
        }
        notifier.fireLeft( token, node );
    }

    @Override
    public void terminate( Token token ){
        token.markInactive();
        Token parent = token.getParent();
        if( parent != null ){
            Collection<Token> siblings = token.getInstance().getActiveChildTokens( parent );
            if( siblings.isEmpty() ){
                terminate( parent );
            }
        }
    }

    @Override
    public void cancel( Token token ){
        token.getNode().cancel( this, token );
        token.markInactive();
    }

}
