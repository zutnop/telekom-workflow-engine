package ee.telekom.workflow.graph.node.gateway;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.node.AbstractNode;

/**
 * Abstract gateway node that implements common gateway functionality, 
 * i.e. that gateways do not produce a return value that is to be stored
 * into the environment.
 */
public abstract class AbstractGateway extends AbstractNode{

    public AbstractGateway( int id ){
        super( id );
    }

    public AbstractGateway( int id, String name ){
        super( id, name );
    }

    @Override
    public void store( Environment environment, Object result ){
        // Gateways do not produce a result and thus do not need to store  anything
    }

}
