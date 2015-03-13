package ee.telekom.workflow.graph.node.gateway;

import org.apache.commons.lang3.tuple.Pair;

import ee.telekom.workflow.graph.GraphEngine;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.node.gateway.condition.Condition;

/**
 * Gateway, that is implements the "exclusive choice" workflow pattern and the "structured loop" pattern. At the same time it can be used
 * as a means to jump within the workflow graph similar to "goto" statements. It routes the incoming token based on the evaluation of
 * runtime conditions to a subsequent branch.
 * <p>
 * {@link XorFork}s do not need to be used necessarily in a balanced fork-join block as {@link AndFork}/{@link OrFork}s do in connection 
 * with {@link AndJoin} and {@link CancellingDiscriminator}s. They may be used in pair with a {@link XorJoin}, but do not have to be. In 
 * that respect, the {@link XorFork} may be understood as a "goto" jump statement known from older programming languages. As such, it may
 * be used to implement classic if-elseif-else, do-while/repeat-until or while/do constructs.<br>
 * The underlying token model slightly restricts the use of "random" goto jumps. Namely, you may NOT cross balanced fork-join block 
 * structure boundaries of {@link AndFork} and {@link OrFork}s. That means, it is neither allowed to jump into a fork-join block from nodes
 * outside the block nor is it allowed to exit a fork-join with an {@link XorFork} jump.
 * <p>
 * The figure below depicts the five valid usage patterns of {@link XorFork}s: 
 * <ol>
 * <li>A {@link XorFork}-{@link XorJoin} block modelling an if-elseif/if-else block
 * <li>A single {@link XorFork} modelling an if-elseif/if-else block (semantically identical to the previous usage pattern)
 * <li>A single {@link XorFork} modelling an if-elseif/if-else block with each subsequent branch ending implicitly.
 * <li>A repeat-until/do-while loop
 * <li>A while-do loop
 * <li>A random "goto" jump.
 * 
 * <pre>
 *          +-(condition1)-[3]--+
 * [1]--[XOR]                   [XOR]--[6]
 *          +-(condition2)-[4]--+
 * 
 *          +-(condition1)-[3]--+
 * [1]--[XOR]                   [6]
 *          +-(condition2)-[4]--+
 * 
 *          +-(condition1)-[3]
 * [1]--[XOR]
 *          +-(condition2)-[4]
 *          
 * [1]--[2]--[XOR]-(else)-[4]
 *  |         |(condition1)
 *  +----<----+ 
 *  
 *        +-(else)-[5]
 *        |
 * [1]--[XOR]-(condition1)-[3]--[4]
 *        |                      |
 *        +----------<-----------+
 * 
 *                                +-(goto)-[5]
 *                                |
 * [1]--[XOR]-(condition1)-[3]--[XOR]--[4]
 *        |                             |
 *        +--------------<--------------+       
 * </pre>
 * Note that while an {@link OrFork} executes 0 to all subsequent branches, this kind of gateway selects 0 to 1 
 * subsequent branches depending on the transition conditions. If no subsequent branches is selected, the incoming
 * token is terminated.
 */
public class XorFork extends AbstractConditionalGateway{

    public XorFork( int id ){
        super( id );
    }

    public XorFork( int id, String name ){
        super( id, name );
    }

    @Override
    public void execute( GraphEngine engine, Token token ){
        GraphInstance instance = token.getInstance();

        for( Pair<Condition, String> pair : getConditions() ){
            Condition condition = pair.getLeft();
            String transitionName = pair.getRight();
            if( condition == null || condition.evaluate( instance ) ){
                engine.complete( token, null, transitionName );
                return;
            }
        }

        // No suitable branch was found. Terminating the token's execution
        engine.terminate( token );
    }

    @Override
    public void cancel( GraphEngine engine, Token token ){
        // Tokens cannot "wait" at this kind of node since the execution
        // is synchronous. Hence, no "cancel" action is required.
    }

}
