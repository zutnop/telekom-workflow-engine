package ee.telekom.workflow.graph.node.gateway;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import ee.telekom.workflow.graph.node.gateway.condition.Condition;

/**
 * Abstract gateway that is providing common functionality for gateways
 * that support conditional transitions.
 */
public abstract class AbstractConditionalGateway extends AbstractGateway{

    private List<Pair<Condition, String>> conditions = new ArrayList<Pair<Condition, String>>();

    public AbstractConditionalGateway( int id ){
        super( id );
    }

    public AbstractConditionalGateway( int id, String name ){
        super( id, name );
    }

    public List<Pair<Condition, String>> getConditions(){
        return conditions;
    }

    /**
     * A <code>null</code> condition is interpreted as a <i>default</i> condition.
     */
    public void addCondition( Condition condition, String transitionName ){
        conditions.add( Pair.of( condition, transitionName ) );
    }
}
