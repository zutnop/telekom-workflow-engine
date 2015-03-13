package ee.telekom.workflow.graph.node.event;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.node.AbstractNode;

/**
 * Throw event, cancelling all active token siblings. This kind of event can be used to
 * model escalation boundaries known from BPMN where different actions need to be taken
 * depending on whether the main activity succeeded or an escalation event cancelled the
 * main activity.<br> 
 * The following figure depicts such a control flow
 * <pre>
 *          +--[main]-------------[escalation]--[success-activity]--------+
 * [1]--[AND]--[escalate-event1]--[escalation]--[escalation1-resolution]--[AND]--[13]
 *          +--[escalate-event2]--[escalation]--[escalation2-resolution]--+
 * </pre> 
 */
public class ThrowEscalation extends AbstractNode{

    public ThrowEscalation( int id ){
        super( id );
    }

    public ThrowEscalation( int id, String name ){
        super( id, name );
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        for( Token other : token.getInstance().getActiveChildTokens( token.getParent() ) ){
            if( other != token ){
                engine.cancel( other );
            }
        }
        engine.complete( token, null );
    }

    @Override
    public void cancel( GraphEngine engine, Token token ){
        // Tokens cannot "wait" at this kind of node since the execution
        // is synchronous. Hence, no "cancel" action is required.
    }

    @Override
    public void store( Environment environment, Object result ){
        // This type of node does not produce or expect a result
    }

}
