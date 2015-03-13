package ee.telekom.workflow.api;

import ee.telekom.workflow.api.Element.Type;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.Transition;
import ee.telekom.workflow.graph.core.GraphImpl;
import ee.telekom.workflow.graph.core.TransitionImpl;
import ee.telekom.workflow.graph.node.gateway.AbstractConditionalGateway;
import ee.telekom.workflow.graph.node.gateway.AndFork;
import ee.telekom.workflow.graph.node.gateway.OrFork;
import ee.telekom.workflow.graph.node.gateway.condition.Condition;

/**
 * Builder that can create a {@link Transition} for the given tree node.
 *
 */
public class TransitionBuilder{

    /**
     * Creates outgoing transitions from the given nodes pointer.
     */
    public void createTransitions( GraphImpl graph, Tree<Row> pointer ){
        Row row = pointer.getContent();
        Element mainToken = row.getMainElement();
        Type type = mainToken.getType();
        Tree<Row> next;
        switch( type ) {
            case START:
            case END:
                return;
            case VALUE:
            case CALL:
            case CALL_ASYNC:
            case HUMAN_TASK:
            case WAIT_SIGNAL:
            case WAIT_TIMER:
            case WAIT_UNTIL_DATE:
            case CREATE_INSTANCE:
            case VALIDATE_INPUT_VARIABLE:
            case ESCALATE:
            case JOIN_FIRST:
            case JOIN_ALL:
                next = findNext( pointer );
                if( next != null ){
                    addTransition( graph, pointer, next );
                }
                return;
            case WHILE_DO_BEGIN:
                // transition for loop entry
                next = pointer.hasChildren() ? pointer.getFirstChild() : pointer.getNext();
                addConditionalTransition( graph, pointer, next, pointer );
                // transition for loop exit
                Tree<Row> whileDoEnd = pointer.getNext();
                next = findNext( whileDoEnd );
                if( next != null ){
                    addConditionalTransition( graph, pointer, next, null );
                }
                return;
            case WHILE_DO_END:
                // transition to loop begin
                Tree<Row> whileDoBegin = pointer.getPrevious();
                addTransition( graph, pointer, whileDoBegin );
                return;
            case DO_WHILE_BEGIN:
                next = pointer.hasChildren() ? pointer.getFirstChild() : pointer.getNext();
                addTransition( graph, pointer, next );
                return;
            case DO_WHILE_END:
                // transition to loop begin
                Tree<Row> doWhileBegin = pointer.getPrevious();
                addConditionalTransition( graph, pointer, doWhileBegin, pointer );
                // transition to next
                next = findNext( pointer );
                if( next != null ){
                    addConditionalTransition( graph, pointer, next, null );
                }
                return;
            case IF:
                next = pointer.hasChildren() ? pointer.getFirstChild() : findEndIf( pointer );
                addConditionalTransition( graph, pointer, next, pointer );
                return;
            case ELSE_IF:
                next = pointer.hasChildren() ? pointer.getFirstChild() : findEndIf( pointer );
                addConditionalTransition( graph, findIf( pointer ), next, pointer );
                return;
            case ELSE:
                next = pointer.hasChildren() ? pointer.getFirstChild() : findEndIf( pointer );
                addConditionalTransition( graph, findIf( pointer ), next, null );
                return;
            case END_IF:
                // adding empty ELSE if no such block was given
                if( !Type.ELSE.equals( pointer.getPrevious().getContent().getType() ) ){
                    addConditionalTransition( graph, findIf( pointer ), pointer, null );
                }
                // transition to next
                next = findNext( pointer );
                if( next != null ){
                    addTransition( graph, pointer, next );
                }
                return;
            case SPLIT:
                return;
            case BRANCH:
                Tree<Row> split = pointer.getParent();
                next = pointer.hasChildren() ? pointer.getFirstChild() : split.getNext();
                if( isOrFork( split ) ){
                    addConditionalTransition( graph, split, next, pointer );
                }
                else{
                    addNamedTransition( graph, split, next );
                }
                return;
            default :
                throw new IllegalStateException();
        }
    }

    private void addTransition( GraphImpl graph, Tree<Row> start, Tree<Row> end ){
        Node startNode = start.getContent().getNode();
        Node endNode = end.getContent().getNode();
        graph.addTransition( new TransitionImpl( startNode, endNode ) );
    }

    private void addNamedTransition( GraphImpl graph, Tree<Row> start, Tree<Row> end ){
        Node startNode = start.getContent().getNode();
        Node endNode = end.getContent().getNode();
        String name = startNode.getId() + "_" + endNode.getId();
        if( graph.getOutputTransitions( startNode, name ) == null ){
            graph.addTransition( new TransitionImpl( name, startNode, endNode ) );
        }
    }

    private void addConditionalTransition( GraphImpl graph, Tree<Row> start, Tree<Row> end, Tree<Row> condition ){
        AbstractConditionalGateway startNode = (AbstractConditionalGateway)start.getContent().getNode();
        Node endNode = end.getContent().getNode();
        Condition c;
        if( condition != null ){
            c = ElementUtil.createCondition( condition.getContent().getMainElement() );
        }
        else{
            c = null;
        }
        String name = startNode.getId() + "_" + endNode.getId();
        startNode.addCondition( c, name );
        if( graph.getOutputTransitions( startNode, name ) == null ){
            graph.addTransition( new TransitionImpl( name, startNode, endNode ) );
        }
    }

    /**
     * Finds the associate IF for an ELSE_IF, ELSE or END_IF pointer.
     */
    private Tree<Row> findIf( Tree<Row> pointer ){
        while( !Type.IF.equals( pointer.getContent().getType() ) ){
            pointer = pointer.getPrevious();
        }
        return pointer;
    }

    /**
     * Finds the associate END_IF for an IF, ELSE_IF or ELSE pointer
     */
    private Tree<Row> findEndIf( Tree<Row> pointer ){
        while( !Type.END_IF.equals( pointer.getContent().getType() ) ){
            pointer = pointer.getNext();
        }
        return pointer;
    }

    /**
     * Returns whether the given split is implemented by an {@link OrFork}. 
     * Note: a split may also be implemented by an {@link AndFork} if it has no
     *       conditional branches.
     */
    private boolean isOrFork( Tree<Row> split ){
        return split.getContent().getNode() instanceof OrFork;
    }

    /**
     * Finds the following node which to create a transition to.
     * Supports tree-nodes that are not the last in their block and those which are last.
     */
    private Tree<Row> findNext( Tree<Row> pointer ){
        if( pointer.hasNext() ){
            // The given tree-node is not the last in its block.
            if( Type.END.equals( pointer.getNext().getContent().getType() ) ){
                return null;
            }
            else{
                return pointer.getNext();
            }
        }
        // The given tree-node was last in its block. Then we return the
        // the block closing tree-node.
        Tree<Row> parent = pointer.getParent();
        Type parentType = parent.getContent().getType();
        if( Type.WHILE_DO_BEGIN.equals( parentType ) ){

            return parent.getNext();
        }
        else if( Type.DO_WHILE_BEGIN.equals( parentType ) ){
            return parent.getNext();
        }
        if( Type.BRANCH.equals( parentType ) ){
            Tree<Row> join = parent.getParent().getNext();
            return join;
        }
        else if( Type.IF.equals( parentType ) || Type.ELSE_IF.equals( parentType ) || Type.ELSE.equals( parentType ) ){
            Tree<Row> endIf = findEndIf( parent );
            return endIf;
        }
        else{
            throw new IllegalStateException( "Unknown containting block type for row " + parent.getContent() );
        }
    }
}
