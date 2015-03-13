package ee.telekom.workflow.graph.node.gateway;

import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.Token;

/**
 * Gateway, that may optionally be used in connection with {@link XorFork}s to increase the readability of graph definitions.
 */
public class XorJoin extends AbstractGateway{

    public XorJoin( int id ){
        super( id );
    }

    public XorJoin( int id, String name ){
        super( id, name );
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        engine.complete( token, null );
    }

    @Override
    public void cancel( GraphEngine engine, Token token ){
        // Tokens cannot "wait" at this kind of node. Hence, no "cancel" action is required.
    }

}
