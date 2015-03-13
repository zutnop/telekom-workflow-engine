package ee.telekom.workflow.graph.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.ObjectUtils;

import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.WorkItemStatus;

public class GraphInstanceImpl implements GraphInstance{

    private Long externalId;
    private Graph graph;
    private EnvironmentImpl environment = new EnvironmentImpl();
    private List<Token> tokens = new LinkedList<Token>();
    private int tokenIdSequence = 0;
    private List<GraphWorkItem> workItems = new ArrayList<>();
    private StringBuilder history = new StringBuilder();
    private Queue<Token> queue = new LinkedList<>();

    public GraphInstanceImpl(){
    }

    public GraphInstanceImpl( Graph graph ){
        this.graph = graph;
    }

    @Override
    public Long getExternalId(){
        return externalId;
    }

    @Override
    public Graph getGraph(){
        return graph;
    }

    @Override
    public EnvironmentImpl getEnvironment(){
        return environment;
    }

    @Override
    public List<Token> getTokens(){
        return tokens;
    }

    @Override
    public List<Token> getActiveTokens(){
        List<Token> result = new LinkedList<Token>();
        for( Token token : tokens ){
            if( token.isActive() ){
                result.add( token );
            }
        }
        return result;
    }

    @Override
    public List<Token> getActiveChildTokens( Token parent ){
        List<Token> result = new LinkedList<Token>();
        for( Token token : tokens ){
            if( ObjectUtils.equals( token.getParent(), parent ) && token.isActive() ){
                result.add( token );
            }
        }
        return result;
    }

    @Override
    public void addToken( Token token ){
        tokens.add( token );
    }

    @Override
    public boolean isCompleted(){
        for( Token token : tokens ){
            if( token.isActive() ){
                return false;
            }
        }
        return true;
    }

    @Override
    public int nextTokenId(){
        return ++tokenIdSequence;
    }

    @Override
    public void addWorkItem( GraphWorkItem workItem ){
        workItems.add( workItem );
    }

    @Override
    public GraphWorkItem getActiveWorkItem( Token token ){
        for( GraphWorkItem wi : workItems ){
            if( wi.getToken().getId() == token.getId()
                    && !WorkItemStatus.COMPLETED.equals( wi.getStatus() )
                    && !WorkItemStatus.CANCELLED.equals( wi.getStatus() ) ){
                return wi;
            }
        }
        return null;
    }

    public void setExternalId( Long externalId ){
        this.externalId = externalId;
    }

    public void setGraph( Graph graph ){
        this.graph = graph;
    }

    public void setEnvironment( EnvironmentImpl environment ){
        this.environment = environment;
    }

    public void setTokens( List<Token> tokens ){
        this.tokens = tokens;
    }

    public int getTokenIdSequence(){
        return tokenIdSequence;
    }

    public void setTokenIdSquence( int tokenIdSequence ){
        this.tokenIdSequence = tokenIdSequence;
    }

    @Override
    public List<GraphWorkItem> getWorkItems(){
        return workItems;
    }

    public void setWorkItems( List<GraphWorkItem> workItems ){
        this.workItems = workItems;
    }

    @Override
    public void addToHistory( String event ){
        history.append( event ).append( "|" );
    }

    public void setHistory( String history ){
        this.history = new StringBuilder( history );
    }

    public String getHistory(){
        return history.toString();
    }

    @Override
    public Token getFirstFromExecutionQueue(){
        return queue.peek();
    }

    @Override
    public void addToExecutionQueue( Token token ){
        queue.offer( token );
    }

    @Override
    public void removeFirstFromExecutionQueue(){
        queue.poll();
    }

}
