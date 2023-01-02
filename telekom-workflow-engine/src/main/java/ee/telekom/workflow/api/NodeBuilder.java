package ee.telekom.workflow.api;

import java.util.Date;
import java.util.List;

import ee.telekom.workflow.api.Element.Type;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.el.ElUtil;
import ee.telekom.workflow.graph.node.activity.BeanAsyncCallActivity;
import ee.telekom.workflow.graph.node.activity.BeanCallActivity;
import ee.telekom.workflow.graph.node.activity.CreateNewInstanceActivity;
import ee.telekom.workflow.graph.node.activity.HumanTaskActivity;
import ee.telekom.workflow.graph.node.activity.SetAttributeActivity;
import ee.telekom.workflow.graph.node.activity.ValidateAttributeActivity;
import ee.telekom.workflow.graph.node.event.CatchSignal;
import ee.telekom.workflow.graph.node.event.CatchTimer;
import ee.telekom.workflow.graph.node.event.ThrowEscalation;
import ee.telekom.workflow.graph.node.gateway.AndFork;
import ee.telekom.workflow.graph.node.gateway.AndJoin;
import ee.telekom.workflow.graph.node.gateway.CancellingDiscriminator;
import ee.telekom.workflow.graph.node.gateway.OrFork;
import ee.telekom.workflow.graph.node.gateway.XorFork;
import ee.telekom.workflow.graph.node.gateway.XorJoin;
import ee.telekom.workflow.graph.node.input.ConstantMapping;
import ee.telekom.workflow.graph.node.input.ExpressionLanguageMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.input.MapMapping;
import ee.telekom.workflow.graph.node.output.OutputMapping;

/**
 * Builder that can create a {@link Node} for the given tree node.
 */
public class NodeBuilder{

    /**
     * Creates a {@link Node} based on the give tree node.
     * @return the created {@link Node} or <code>null</code> if the tree node does not describe a node.
     */
    public Node createNode( Tree<Row> current ){
        Row row = current.getContent();
        Element output = row.getOutputElement();
        Element main = row.getMainElement();
        List<Element> input = row.getInputElement();
        Integer id = main.getId();
        Type type = main.getType();
        switch( type ) {
            case START:
                return null;
            case END:
                return null;
            case VALUE:
                String vAttribute = output.getString( 0 );
                Object value = main.getObject( 0 );
                return new SetAttributeActivity( id, vAttribute, value );
            case CALL:
                String cBean = main.getString( 0 );
                String cMethod = main.getString( 1 );
                InputMapping<?>[] cArgumentsMapping = ElementUtil.createArrayMapping( main.getObjectArray( 2 ) );
                OutputMapping cOutputMapping = ElementUtil.createOutputMapping( output );
                return new BeanCallActivity( id, cBean, cMethod, cArgumentsMapping, cOutputMapping );
            case CALL_ASYNC:
                String caBean = main.getString( 0 );
                String caMethod = main.getString( 1 );
                InputMapping<?>[] caArgumentsMapping = ElementUtil.createArrayMapping( main.getObjectArray( 2 ) );
                OutputMapping caOutputMapping = ElementUtil.createOutputMapping( output );
                return new BeanAsyncCallActivity( id, caBean, main.getAutoRetryOnRecovery(), caMethod, caArgumentsMapping, caOutputMapping );
            case HUMAN_TASK:
                String role = main.getString( 0 );
                String user = main.getString( 1 );
                MapMapping htArgumentsMapping = ElementUtil.createMapMapping( input );
                OutputMapping htOutputMapping = ElementUtil.createOutputMapping( output );
                return new HumanTaskActivity( id, role, user, htArgumentsMapping, htOutputMapping );
            case WAIT_SIGNAL:
                String signal = main.getString( 0 );
                OutputMapping wsOutputMapping = ElementUtil.createOutputMapping( output );
                return new CatchSignal( id, signal, wsOutputMapping );
            case WAIT_TIMER:
                String due = main.getString( 0 );
                if( ElUtil.hasBrackets( due ) ){
                    return new CatchTimer( new ExpressionLanguageMapping<Long>( due ), id );
                }
                else{
                    long delayMillis = Long.valueOf( due );
                    return new CatchTimer( id, delayMillis );
                }
            case WAIT_UNTIL_DATE:
                String dueDate = main.getString( 0 );
                return new CatchTimer( id, new ExpressionLanguageMapping<Date>( dueDate ) );
            case CREATE_INSTANCE:
                String graphName = main.getString( 0 );
                InputMapping<String> nameMapping = ElUtil.hasBrackets( graphName ) ?
                        new ExpressionLanguageMapping<String>( graphName ) : ConstantMapping.of( graphName );

                Integer graphVersion = main.getInteger( 1 );
                InputMapping<Integer> versionMapping = ConstantMapping.of( graphVersion );

                String label1 = main.getString( 2 );
                InputMapping<String> label1Mapping = ElUtil.hasBrackets( label1 ) ?
                        new ExpressionLanguageMapping<String>( label1 ) : ConstantMapping.of( label1 );

                String label2 = main.getString( 3 );
                InputMapping<String> label2Mapping = ElUtil.hasBrackets( label2 ) ?
                        new ExpressionLanguageMapping<String>( label2 ) : ConstantMapping.of( label2 );

                MapMapping ciArgumentsMapping = ElementUtil.createMapMapping( input );

                return new CreateNewInstanceActivity( id, nameMapping, versionMapping, label1Mapping, label2Mapping, ciArgumentsMapping );
            case WHILE_DO_BEGIN:
                return new XorFork( id );
            case WHILE_DO_END:
                id = -findPartnerNodeId( current, type );
                return new XorJoin( id );
            case DO_WHILE_BEGIN:
                id = -findPartnerNodeId( current, type );
                return new XorJoin( id );
            case DO_WHILE_END:
                return new XorFork( id );
            case IF:
                return new XorFork( id );
            case ELSE_IF:
                return null;
            case ELSE:
                return null;
            case END_IF:
                id = -findPartnerNodeId( current, type );
                return new XorJoin( id );
            case SPLIT:
                boolean hasConditionalBranches = hasConditionalBranches( current );
                return hasConditionalBranches ? new OrFork( id ) : new AndFork( id );
            case BRANCH:
                return null;
            case JOIN_FIRST:
                id = -findPartnerNodeId( current, type );
                return new CancellingDiscriminator( id );
            case JOIN_ALL:
                id = -findPartnerNodeId( current, type );
                return new AndJoin( id );
            case ESCALATE:
                return new ThrowEscalation( id );
            case VALIDATE_INPUT_VARIABLE:
                String vivAttribute = main.getString( 0 );
                Class<?> vivType = (Class<?>)main.getObject( 1 );
                if( main.countArguments() == 2 ){
                    return new ValidateAttributeActivity( id, vivAttribute, vivType );
                }
                boolean vivIsRequired = (boolean)main.getObject( 2 );
                if( main.countArguments() == 3 ){
                    return new ValidateAttributeActivity( id, vivAttribute, vivType, vivIsRequired );
                }
                Object vivDefaultValue = main.getObject( 3 );
                return new ValidateAttributeActivity( id, vivAttribute, vivType, vivIsRequired, vivDefaultValue );
            default :
                throw new IllegalStateException( "Cannot create node for unknown main element type in row: " + row );
        }
    }

    /**
     * There are two types of branches, those with and those without a condition ({@link WorkflowFactory#branch()} vs {@link WorkflowFactory#branch(String)}).
     * This method returns whether the given split has at least one branch that defines a condition.
     */
    private boolean hasConditionalBranches( Tree<Row> split ){
        for( Tree<Row> branch : split.getChildren() ){
            Element main = branch.getContent().getMainElement();
            if( main.countArguments() > 0 ){
                return true;
            }
        }
        return false;
    }

    /**
     * Some node producing elements do not define an id themselves but deduce their id based
     * on their block-opening or block-closing partner. E.g. a WHILE_DO_END deduces its id 
     * based on its WHILE_DO_BEGIN partner.
     */
    private int findPartnerNodeId( Tree<Row> current, Type type ){
        switch( type ) {
            case JOIN_FIRST:
            case JOIN_ALL:
            case WHILE_DO_END:
                // The partner is the previous sibling which is the SPLIT or WHILE_DO_BEGIN
                return current.getPrevious().getContent().getId();
            case DO_WHILE_BEGIN:
                // The partner is the next sibling which is the DO_WHILE_END
                return current.getNext().getContent().getMainElement().getId();
            case END_IF:
                // The partner is the first previous IF (there may be ELSE_IF and ELSE siblings between IF and this END_IF)
                Tree<Row> if_ = current;
                while( !Type.IF.equals( if_.getContent().getType() ) ){
                    if_ = if_.getPrevious();
                }
                return if_.getContent().getId();
            default :
                throw new IllegalStateException();
        }
    }

}
