package ee.telekom.workflow.api;

import ee.telekom.workflow.api.Element.Type;
import ee.telekom.workflow.graph.Graph;

/**
 * {@link WorkflowFactoryImpl} implementation that translates the DSL method invocations into a 
 * {@link Tree} of {@link Row}s where a row's depths within the tree corresponds a DSL method
 * invocation's indentation level when formating the DSL method invocations in a typical fashion.
 */
@SuppressWarnings("rawtypes")
public class WorkflowFactoryImpl implements
        WorkflowFactory,
        DslBlock,
        DslAttribute,
        DslVariable,
        DslExpression,
        DslBranchBlock,
        DslDoWhileBlock,
        DslIfBlock,
        DslElseBlock,
        DslMainBlock,
        DslSplit,
        DslValidationBlock,
        DslWhileDoBlock{

    private String name;
    private int version;
    private boolean keepHistory;
    private int archivePeriodLength;
    private Tree<Row> root = Tree.root( Row.class );
    private Tree<Row> current = root;

    public WorkflowFactoryImpl( String name, int version ){
        this.name = name;
        this.version = version;
        this.keepHistory = true;
        this.archivePeriodLength = -1;
    }
    
    public WorkflowFactoryImpl( String name, int version, boolean keepHistory, int archivePeriodLength ){
        this.name = name;
        this.version = version;
        this.keepHistory = keepHistory;
        this.archivePeriodLength = archivePeriodLength;
    }

    @Override
    public WorkflowFactoryImpl start(){
        append( Type.START, null );
        return this;
    }

    @Override
    public void end(){
        append( Type.END, null );
    }

    @Override
    public WorkflowFactoryImpl variable( String name ){
        append( Type.VARIABLE, null, name );
        return this;
    }

    @Override
    public WorkflowFactoryImpl variables( String... nameMappings ){
        append( Type.VARIABLES, null, (Object)nameMappings );
        return this;
    }

    @Override
    public WorkflowFactoryImpl value( int id, Object value ){
        append( Type.VALUE, id, value );
        return this;
    }

    @Override
    public WorkflowFactoryImpl call( int id, String beanName, String methodName, Object... arguments ){
        append( Type.CALL, id, beanName, methodName, arguments );
        return this;
    }

    @Override
    public WorkflowFactoryImpl callAsync( int id, String beanName, String methodName, Object... arguments ){
        append( Type.CALL_ASYNC, id, AutoRecovery.DISABLED, beanName, methodName, arguments );
        return this;
    }

    @Override
    public WorkflowFactoryImpl callAsync( int id, String beanName, String methodName, AutoRecovery autoRecovery, Object... arguments ){
        append( Type.CALL_ASYNC, id, autoRecovery, beanName, methodName, arguments );
        return this;
    }

    @Override
    public WorkflowFactoryImpl humanTask( int id, String roleName, String assignee ){
        append( Type.HUMAN_TASK, id, roleName, assignee );
        return this;
    }

    @Override
    public WorkflowFactoryImpl waitSignal( int id, String type ){
        append( Type.WAIT_SIGNAL, id, type );
        return this;
    }

    @Override
    public WorkflowFactoryImpl waitTimer( int id, String due ){
        append( Type.WAIT_TIMER, id, due );
        return this;
    }

    @Override
    public WorkflowFactoryImpl waitUntilDate( int id, String due ){
        append( Type.WAIT_UNTIL_DATE, id, due );
        return this;
    }

    @Override
    public WorkflowFactoryImpl createInstance( int id, String workflowName, Integer workflowVersion, String label1, String label2 ){
        append( Type.CREATE_INSTANCE, id, workflowName, workflowVersion, label1, label2 );
        return this;
    }

    @Override
    public WorkflowFactoryImpl withAttribute( String name, Object value ){
        append( Type.ATTRIBUTE, null, value, name );
        return this;
    }

    @Override
    public WorkflowFactoryImpl done(){
        // do nothing
        return this;
    }

    @Override
    public WorkflowFactoryImpl whileDo( int id, String condition ){
        append( Type.WHILE_DO_BEGIN, id, condition );
        return this;
    }

    @Override
    public WorkflowFactoryImpl whileDo(){
        append( Type.WHILE_DO_END, null );
        return this;
    }

    @Override
    public WorkflowFactoryImpl doWhile(){
        append( Type.DO_WHILE_BEGIN, null );
        return this;
    }

    @Override
    public WorkflowFactoryImpl doWhile( int id, String condition ){
        append( Type.DO_WHILE_END, id, condition );
        return this;
    }

    @Override
    public WorkflowFactoryImpl if_( int id, String condition ){
        append( Type.IF, id, condition );
        return this;
    }

    @Override
    public WorkflowFactoryImpl elseIf( String condition ){
        append( Type.ELSE_IF, null, condition );
        return this;
    }

    @Override
    public WorkflowFactoryImpl else_(){
        append( Type.ELSE, null );
        return this;
    }

    @Override
    public WorkflowFactoryImpl endIf(){
        append( Type.END_IF, null );
        return this;
    }

    @Override
    public WorkflowFactoryImpl split( int id ){
        append( Type.SPLIT, id );
        return this;
    }

    @Override
    public WorkflowFactoryImpl branch(){
        append( Type.BRANCH, null );
        return this;
    }

    @Override
    public WorkflowFactoryImpl branch( String condition ){
        append( Type.BRANCH, null, condition );
        return this;
    }

    @Override
    public WorkflowFactoryImpl joinFirst(){
        append( Type.JOIN_FIRST, null );
        return this;
    }

    @Override
    public WorkflowFactoryImpl joinAll(){
        append( Type.JOIN_ALL, null );
        return this;
    }

    @Override
    public WorkflowFactoryImpl escalate( int id ){
        append( Type.ESCALATE, id );
        return this;
    }

    @Override
    public WorkflowFactoryImpl validateInputVariable( int id, String attribute, Class type ){
        append( Type.VALIDATE_INPUT_VARIABLE, id, attribute, type );
        return this;
    }

    @Override
    public WorkflowFactoryImpl validateInputVariable( int id, String attribute, Class type, boolean isRequired ){
        append( Type.VALIDATE_INPUT_VARIABLE, id, attribute, type, isRequired );
        return this;
    }

    @Override
    public WorkflowFactoryImpl validateInputVariable( int id, String attribute, Class type, boolean isRequired, Object defaultValue ){
        append( Type.VALIDATE_INPUT_VARIABLE, id, attribute, type, isRequired, defaultValue );
        return this;
    }

    private Tree<Row> append( Type type, Integer id, Object... arguments ){
        return append( type, id, AutoRecovery.getDefault(), arguments );
    }

    private Tree<Row> append( Type type, Integer id, AutoRecovery autoRecovery, Object... arguments ){
        Element token = new Element( type, id, autoRecovery, arguments );
        if( Type.START.equals( type ) ){
            return downRight( token );
        }

        if( current.getContent().getOutputElement() != null && current.getContent().getMainElement() == null ){
            return here( token );
        }

        Type lastType = current.getContent().getMainElement().getType();

        // Block closing statements do rarely depend on the preceding statement
        // and generally cause a down-left. The few exceptions are:
        // (a) A preceding empty WHILE_DO/DO_WHILE/IF/ELSE_IF/ELSE block causes
        //     -> down
        // (b) BRANCH statements open a block and close one at the same time and cause
        //     -> down-right for the first branch
        //     -> down for a preceding empty branch
        //     -> down-left for a preceding non-empty branch
        // (c) JOIN_ALL/JOIN_FIRST cause a 
        //     -> down-left for a preceding empty branch
        //     -> down-left-left for a non-empty preceding branch 
        if( Type.WHILE_DO_END.equals( type ) ){
            if( Type.WHILE_DO_BEGIN.equals( lastType ) ){
                return down( token );
            }
            else{
                return downLeft( token );
            }
        }
        if( Type.DO_WHILE_END.equals( type ) ){
            if( Type.DO_WHILE_BEGIN.equals( lastType ) ){
                return down( token );
            }
            else{
                return downLeft( token );
            }
        }
        if( Type.ELSE_IF.equals( type ) ){
            if( Type.IF.equals( lastType ) || Type.ELSE_IF.equals( lastType ) ){
                return down( token );
            }
            else{
                return downLeft( token );
            }
        }
        if( Type.ELSE.equals( type ) ){
            if( Type.IF.equals( lastType ) || Type.ELSE_IF.equals( lastType ) ){
                return down( token );
            }
            else{
                return downLeft( token );
            }
        }
        if( Type.END_IF.equals( type ) ){
            if( Type.IF.equals( lastType ) || Type.ELSE_IF.equals( lastType ) || Type.ELSE.equals( lastType ) ){
                return down( token );
            }
            else{
                return downLeft( token );
            }
        }
        if( Type.BRANCH.equals( type ) ){
            if( Type.SPLIT.equals( lastType ) ){
                return downRight( token );
            }
            else if( Type.BRANCH.equals( lastType ) ){
                return down( token );
            }
            else{
                return downLeft( token );
            }
        }
        if( Type.JOIN_ALL.equals( type ) || Type.JOIN_FIRST.equals( type ) ){
            if( Type.BRANCH.equals( lastType ) ){
                return downLeft( token );
            }
            else{
                return downLeftLeft( token );
            }
        }

        // ATTRIBUTE tokens are always added to the same row (here): 
        if( Type.ATTRIBUTE.equals( type ) ){
            return here( token );
        }

        // In any not yet handled cause, indentation solely depends on the
        // previous type.
        switch( lastType ) {
            case START:
                return down( token );
            case VARIABLE:
            case VARIABLES:
                return here( token );
            case VALUE:
            case CALL:
            case CALL_ASYNC:
            case WAIT_SIGNAL:
            case WAIT_TIMER:
            case WAIT_UNTIL_DATE:
            case ATTRIBUTE:
            case ESCALATE:
            case HUMAN_TASK:
            case CREATE_INSTANCE:
            case VALIDATE_INPUT_VARIABLE:
                return down( token );
            case WHILE_DO_BEGIN:
            case DO_WHILE_BEGIN:
            case IF:
            case ELSE_IF:
            case ELSE:
            case SPLIT:
            case BRANCH:
                return downRight( token );
            case WHILE_DO_END:
            case DO_WHILE_END:
            case END_IF:
            case JOIN_FIRST:
            case JOIN_ALL:
                return down( token );
            case END:
                throw new IllegalStateException( "END must be the last element" );
        }
        throw new IllegalStateException( "Should not happen" );
    }

    /**
     * Starts a new row one indentation level to the right.
     */
    private Tree<Row> downRight( Element token ){
        return current = current.addChild( Tree.of( new Row( token ) ) );
    }

    /**
     * Starts a new row one indentation level to the left.
     */

    private Tree<Row> downLeft( Element token ){
        return current = current.getParent().addSibling( Tree.of( new Row( token ) ) );
    }

    /**
     * Starts a new row one two indentation levels to the left.
     */
    private Tree<Row> downLeftLeft( Element token ){
        return current = current.getParent().getParent().addSibling( Tree.of( new Row( token ) ) );
    }

    /**
     * Starts a new row at the same level of indentation.
     */
    private Tree<Row> down( Element token ){
        return current = current.addSibling( Tree.of( new Row( token ) ) );
    }

    /**
     * Adds element to the same row.
     */
    private Tree<Row> here( Element token ){
        current.getContent().addToken( token );
        return current;
    }

    public Graph buildGraph(){
        return new GraphBuilder( name, version, keepHistory, archivePeriodLength, root ).build();
    }

    @Override
    public String toString(){
        return root.toString();
    }

}