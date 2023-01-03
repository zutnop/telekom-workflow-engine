package ee.telekom.workflow.api;

/**
 * An {@link Element} corresponds to a method in the API. Every method in the API creates an {@link Element} in a {@link Row}.
 * An element has a type, an optional id and optionally any number of arguments.
 * <p>
 * For instance, {@link WorkflowFactory#call(int, String, String, Object...)} creates according
 * <pre>new Element(Type.CALL, id, bean, method, arguments)</pre>.
 */
public class Element{

    public static enum Type{
        START,
        END,
        VARIABLE,
        VARIABLES,
        VALUE,
        CALL,
        CALL_ASYNC,
        HUMAN_TASK,
        WAIT_SIGNAL,
        WAIT_TIMER,
        WAIT_UNTIL_DATE,
        CREATE_INSTANCE,
        ATTRIBUTE,
        WHILE_DO_BEGIN,
        WHILE_DO_END,
        DO_WHILE_BEGIN,
        DO_WHILE_END,
        IF,
        ELSE_IF,
        ELSE,
        END_IF,
        SPLIT,
        BRANCH,
        JOIN_FIRST,
        JOIN_ALL,
        ESCALATE,
        VALIDATE_INPUT_VARIABLE
    }

    private Type type;
    private Integer id;
    private AutoRecovery autoRecovery;
    private Object[] arguments;

    public Element( Type type, Integer id, AutoRecovery autoRecovery, Object[] arguments ){
        this.type = type;
        this.id = id;
        this.autoRecovery = autoRecovery;
        this.arguments = arguments;
    }

    public Type getType(){
        return type;
    }

    public Integer getId(){
        return id;
    }

    public AutoRecovery getAutoRecovery(){
        return autoRecovery;
    }

    public Object getObject( int index ){
        return arguments[index];
    }

    public Object[] getObjectArray( int index ){
        return (Object[])getObject( index );
    }

    public String getString( int index ){
        return (String)getObject( index );
    }

    public String[] getStringArray( int index ){
        return (String[])getObject( index );
    }

    public Integer getInteger( int index ){
        return (Integer)getObject( index );
    }

    public int countArguments(){
        return arguments == null ? 0 : arguments.length;
    }

    @Override
    public String toString(){
        return type.name() + (id == null ? "" : ":" + id) + getArgumnetsAsText();

    }

    private String getArgumnetsAsText(){
        if( arguments == null || arguments.length == 0 ){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append( " (" );
        for( int i = 0; i < arguments.length; i++ ){
            Object argument = arguments[i];
            appendArgument( sb, argument, true );
            if( i < arguments.length - 1 ){
                sb.append( ", " );
            }
        }
        sb.append( ")" );
        return sb.toString();
    }

    private void appendArgument( StringBuilder sb, Object o, boolean unpackArray ){
        if( o == null || o instanceof Boolean || o instanceof Number ){
            sb.append( o );
        }
        else if( o instanceof String ){
            sb.append( "\"" ).append( o ).append( "\"" );
        }
        else if( unpackArray && o instanceof Object[] ){ // handling task arguments
            Object[] array = (Object[])o;
            for( int j = 0; j < array.length; j++ ){
                Object element = array[j];
                appendArgument( sb, element, false );
                if( j < array.length - 1 ){
                    sb.append( ", " );
                }
            }
        }
        else{
            sb.append( "$" + o.getClass().getCanonicalName() );
        }
    }
}
