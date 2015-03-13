package ee.telekom.workflow.graph.node.gateway;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.Transition;

/**
 * Gateway, that implements the "parallel split" workflow pattern. That means, based on the incoming
 * token, it generates a new child token per subsequent branch and initiates the execution of the child
 * tokens. At the same time, the incoming token remains at this gateway.
 * <p>
 * The underlying token model implies restraints on the subsequent branches. The following figure illustrates
 * the two types of valid usages.<br>
 * One valid pattern is a balanced fork-join block which is built of a fork and a join. It synchronises (re-joins) 
 * all branches leaving the fork in the paired join. As a join, either an {@link AndJoin} or a 
 * {@link CancellingDiscriminator} may be used but not an {@link XorJoin}.<br>
 * The other valid pattern leaves all subsequent branches independently and never synchronises (re-joins) 
 * those back. In that case, every subsequent branch is ended implicitly by a node without outgoing 
 * transitions.
 * <pre>
 *          +--[3]--+
 * [1]--[AND]       [AND/CD]--[6]
 *          +--[4]--+
 *          
 *          +--[3]
 * [1]--[AND]
 *          +--[4]
 * </pre>
 * Note that an {@link AndFork} behaves identical to an {@link OrFork} with a <code>null</code> condition
 * for every subsequent branch.
 */
public class AndFork extends AbstractGateway{

    public AndFork( int id ){
        super( id );
    }

    public AndFork( int id, String name ){
        super( id, name );
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        GraphInstance instance = token.getInstance();
        Graph graph = instance.getGraph();
        Node node = token.getNode();
        List<Transition> transitions = graph.getOutputTransitions( node );

        List<Pair<Token, String>> pairs = new LinkedList<Pair<Token, String>>();
        for( Transition transition : transitions ){
            Token child = engine.addToken( instance, node, token );
            String transitionName = transition.getName();
            pairs.add( Pair.of( child, transitionName ) );
        }

        for( Pair<Token, String> pair : pairs ){
            if( pair.getLeft().isActive() ){
                engine.complete( pair.getLeft(), null, pair.getRight() );
            }
        }
    }

    // This given token can either be a "parent" token or a leave token.
    //
    // The parent token remains at this node until the created leave tokens
    // are synchronised (either by an AndJoin or XorJoin node).
    //
    // Leave tokens remain at this node until their execution begins. Note,
    // that is is possible that a leave token is cancelled before its execution
    // begins. This happens if an earlier executed thread reaches a cancelling
    // discriminator.
    @Override
    public void cancel( GraphEngine engine, Token token ){
        GraphInstance instance = token.getInstance();
        Collection<Token> childTokens = instance.getActiveChildTokens( token );
        for( Token childToken : childTokens ){
            engine.cancel( childToken );
        }
    }

}
