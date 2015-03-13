package ee.telekom.workflow.graph.node.gateway;

import java.util.Collection;

import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.Token;

/**
 * Gateway, that implements the "structured synchronizing merge" workflow pattern. That means, it synchronises (re-joins)
 * the threads of execution that are originating from the paired {@link AndFork} or {@link OrFork}. Only when all threads
 * of execution arrived at the {@link AndJoin}, execution continues along the subsequent branch. 
 * <p>
 * The underlying token model restricts the usage of {@link AndJoin}s to be in balanced pairs with {@link AndFork} or {@link OrFork}s.
 */
public class AndJoin extends AbstractGateway{

    public AndJoin( int id ){
        super( id );
    }

    public AndJoin( int id, String name ){
        super( id, name );
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        GraphInstance instance = token.getInstance();
        Token parent = token.getParent();

        token.markInactive();
        // Making use of the fact that all sibling tokens originate from the same fork. 
        Collection<Token> activeSiblings = instance.getActiveChildTokens( token.getParent() );
        if( activeSiblings.isEmpty() ){
            // This token is the last token of the group to arrive
            parent.setNode( this );
            engine.complete( parent, null );
        }
    }

    @Override
    public void cancel( GraphEngine engine, Token token ){
        // Tokens cannot "wait" at this kind of node. Hence, no "cancel" action is required.
    }

}
