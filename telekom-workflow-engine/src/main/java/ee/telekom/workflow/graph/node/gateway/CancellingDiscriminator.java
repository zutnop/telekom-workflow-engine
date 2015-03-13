package ee.telekom.workflow.graph.node.gateway;

import java.util.Collection;

import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.Token;

/**
 * Gateway, that implements the "cancelling discriminator" workflow pattern. That means, it continues the execution of the
 * subsequent branch once the first thread of execution arrives and cancels all other threads of execution that are originating
 * from the same fork.
 * <p>
 * The underlying token model restricts the usage of {@link AndJoin}s to be in balanced pairs with {@link AndFork} or {@link OrFork}s.
 */
public class CancellingDiscriminator extends AbstractGateway{

    public CancellingDiscriminator( int id ){
        super( id );
    }

    public CancellingDiscriminator( int id, String name ){
        super( id, name );
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        GraphInstance instance = token.getInstance();
        Token parent = token.getParent();

        token.markInactive();
        // Making use of the fact that all sibling tokens originate from the same fork. 
        Collection<Token> siblings = instance.getActiveChildTokens( parent );
        for( Token sibling : siblings ){
            engine.cancel( sibling );
        }

        parent.setNode( this );
        engine.complete( parent, null );
    }

    @Override
    public void cancel( GraphEngine engine, Token token ){
        // Tokens cannot "wait" at this kind of node. Hence, no "cancel" action is required.
    }

}
