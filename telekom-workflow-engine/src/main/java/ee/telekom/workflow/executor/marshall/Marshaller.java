package ee.telekom.workflow.executor.marshall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.core.workitem.WorkItemType;
import ee.telekom.workflow.facade.util.HistoryUtil;
import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.core.EnvironmentImpl;
import ee.telekom.workflow.graph.core.GraphInstanceImpl;
import ee.telekom.workflow.graph.core.GraphWorkItemImpl;
import ee.telekom.workflow.graph.core.TokenImpl;
import ee.telekom.workflow.util.JsonUtil;

/**
 * Helper class to convert between persisted and non-persisted entities.
 *
 * @author Christian Klock
 */
public class Marshaller{

    public static void marshall( GraphInstance instance, WorkflowInstance woin, List<WorkItem> woits, WorkflowInstanceStatus completeStatus ){
        woin.setRefNum( instance.getExternalId() );
        woin.setWorkflowName( instance.getGraph().getName() );
        woin.setWorkflowVersion( instance.getGraph().getVersion() );
        woin.setAttributes( serializeAttributes( instance.getEnvironment().getAttributesAsMap() ) );
		woin.setHistory( instance.getGraph().getKeepHistory() ? instance.getHistory() : HistoryUtil.deleteHistory( instance.getHistory() ) );
		woin.setState( serializeTokens( instance.getTokens(), instance.getGraph().getKeepHistory() ) );
        woin.setStatus( instance.isCompleted() ? completeStatus : WorkflowInstanceStatus.EXECUTING );
        for( GraphWorkItem wi : instance.getWorkItems() ){
            woits.add( marshall( wi ) );
        }
    }

    public static GraphInstance unmarshall( WorkflowInstance woin, List<WorkItem> woits, Graph graph ){
        GraphInstanceImpl instance = new GraphInstanceImpl();
        instance.setExternalId( woin.getRefNum() );
        instance.setGraph( graph );

        EnvironmentImpl env = deserializeEnv( woin.getAttributes() );
        instance.setEnvironment( env );

        instance.setHistory( woin.getHistory() );

        List<Token> tokens = deserializeTokens( woin.getState(), instance );
        instance.setTokens( tokens );

        int tokenIdSequence = 0;
        for( Token token : tokens ){
            if( token.getId() > tokenIdSequence ){
                tokenIdSequence = token.getId();
            }
        }
        instance.setTokenIdSquence( tokenIdSequence );
        List<GraphWorkItem> workItems = new ArrayList<>();
        for( WorkItem woit : woits ){
            workItems.add( unmarshall( woit, instance ) );
        }
        instance.setWorkItems( workItems );
        return instance;
    }

    private static WorkItem marshall( GraphWorkItem workItem ){
        WorkItem woit = new WorkItem();
        woit.setRefNum( workItem.getExternalId() );
        woit.setWoinRefNum( workItem.getExternalGraphInstanceId() );
        woit.setTokenId( workItem.getToken().getId() );
        woit.setStatus( workItem.getStatus() );
        woit.setSignal( workItem.getSignal() );
        woit.setDueDate( workItem.getDueDate() );
        woit.setBean( workItem.getBean() );
        woit.setMethod( workItem.getMethod() );
        woit.setRole( workItem.getRole() );
        woit.setUserName( workItem.getUser() );
        if( WorkItemType.TASK.equals( woit.getType() ) ){
            woit.setArguments( JsonUtil.serialize( workItem.getTaskArguments(), false ) );
        }
        else if( WorkItemType.HUMAN_TASK.equals( woit.getType() ) ){
            woit.setArguments( JsonUtil.serialize( workItem.getHumanTaskArguments(), false ) );
        }
        woit.setResult( serializeResult( workItem.getResult() ) );
        return woit;
    }

    public static String serializeResult( Object result ){
        return JsonUtil.serialize( result, true );
    }

    private static GraphWorkItem unmarshall( WorkItem woit, GraphInstance instance ){
        GraphWorkItemImpl workItem = new GraphWorkItemImpl();
        workItem.setExternalId( woit.getRefNum() );
        workItem.setExternalGraphInstanceId( woit.getWoinRefNum() );
        workItem.setToken( findToken( instance.getTokens(), woit.getTokenId() ) );
        workItem.setStatus( woit.getStatus() );
        workItem.setSignal( woit.getSignal() );
        workItem.setDueDate( woit.getDueDate() );
        workItem.setBean( woit.getBean() );
        workItem.setMethod( woit.getMethod() );
        workItem.setRole( woit.getRole() );
        workItem.setUser( woit.getUserName() );
        if( WorkItemType.TASK.equals( woit.getType() ) ){
            workItem.setArguments( deserializeTaskArguments( woit.getArguments() ) );
        }
        else if( WorkItemType.HUMAN_TASK.equals( woit.getType() ) ){
            workItem.setArguments( deserializeHumanTaskArguments( woit.getArguments() ) );
        }
        workItem.setResult( JsonUtil.deserialize( woit.getResult() ) );
        return workItem;
    }

    public static Object[] deserializeTaskArguments( String json ){
        return JsonUtil.deserialize( json, Object[].class );
    }

    public static Map<String, Object> deserializeHumanTaskArguments( String json ){
        return JsonUtil.deserializeHashMap( json, String.class, Object.class );
    }

    private static TokenState marshall( Token token ){
        TokenState state = new TokenState();
        state.setId( token.getId() );
        state.setNodeId( token.getNode().getId() );
        state.setParentId( token.getParent() == null ? null : token.getParent().getId() );
        state.setActive( token.isActive() );
        return state;
    }

    public static String serializeAttributes( Map<String, Object> attributes ){
        return (attributes == null || attributes.isEmpty()) ? null : JsonUtil.serialize( attributes, false );
    }

    public static Map<String, Object> deserializeAttributes( String json ){
        if( json == null || json.isEmpty() ){
            return new HashMap<String, Object>();
        }
        Map<String, Object> attributes = JsonUtil.deserializeHashMap( json, String.class, Object.class );
        return attributes;
    }

    public static EnvironmentImpl deserializeEnv( String json ){
        EnvironmentImpl env = new EnvironmentImpl();
        Map<String, Object> attributes = deserializeAttributes( json );
        setAttributesMap( env, attributes );
        return env;
    }

	private static String serializeTokens( Collection<Token> tokens, boolean keepHistory ) {
		List<TokenState> states = new ArrayList<TokenState>();
		for ( Token token : tokens ) {
			if ( !keepHistory && !token.isActive() ) {
				continue;
			} 
			states.add( marshall(token) );
		}
		return serializeTokenStates( states );
	}

    private static List<Token> deserializeTokens( String json, GraphInstance instance ){
        Collection<TokenState> states = deserializeTokenStates( json );
        List<Token> tokens = new ArrayList<Token>();
        Map<Integer, Token> tokenById = new TreeMap<Integer, Token>();
        for( TokenState state : states ){
            TokenImpl token = new TokenImpl();
            token.setId( state.getId() );
            token.setNode( findeNode( instance.getGraph(), state.getNodeId() ) );
            token.setInstance( instance );
            if( state.getParentId() != null ){
                Token parent = tokenById.get( state.getParentId() );
                if( parent == null ){
                    // Should never happen if tokens are (de)-serialized in
                    // ascending token id order
                    throw new RuntimeException(
                            "Token missing or not yet deserialized" );
                }
                token.setParent( parent );
            }
            token.setActive( state.isActive() );
            tokenById.put( token.getId(), token );
            tokens.add( token );
        }
        return tokens;
    }

    public static String serializeTokenStates( Collection<TokenState> states ){
        return JsonUtil.serializeCollection( states, false, false );
    }

    public static Collection<TokenState> deserializeTokenStates( String json ){
        return JsonUtil.deserializeCollection( json, ArrayList.class, TokenState.class );
    }

    private static Node findeNode( Graph graph, int id ){
        Node result = graph.getNode( id );
        if( result != null ){
            return result;
        }
        throw new RuntimeException( "Cannot find node " + id + " on graph "
                + graph.getName() + ":" + graph.getVersion() );
    }

    private static Token findToken( Collection<Token> tokens, int tokenId ){
        for( Token token : tokens ){
            if( token.getId() == tokenId ){
                return token;
            }
        }
        throw new RuntimeException( "Cannot find token with id " + tokenId );
    }

    private static void setAttributesMap( Environment env, Map<String, Object> attributes ){
        for( Map.Entry<String, Object> entry : attributes.entrySet() ){
            env.setAttribute( entry.getKey(), entry.getValue() );
        }
    }

}
