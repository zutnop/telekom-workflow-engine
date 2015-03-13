package ee.telekom.workflow.graph;

import ee.telekom.workflow.graph.node.activity.ScriptActivity.Script;

/**
 * This {@link Script} appends the given id to the "path" attribute of the {@link Environment}
 * UNLESS it is told to throw a {@link RuntimeException} instead.
 */
public class RecordPathScript implements Script{

    public static final String EXCEPTION_MESSAGE = "script exception";
    public static final String ATTRIBUTE = "path";
    private static boolean throwException = false;
    private int id;

    public RecordPathScript( int id ){
        this.id = id;
    }

    @Override
    public void execute( Environment environment ){
        if( throwException ){
            throw new RuntimeException( EXCEPTION_MESSAGE );
        }
        String path = (String)environment.getAttribute( ATTRIBUTE );
        if( path == null ){
            path = "" + id;
        }
        else{
            path = path + "," + id;
        }
        environment.setAttribute( ATTRIBUTE, path );
    }

    public static void startThrowingException(){
        throwException = true;
    }

    public static void stopThrowingException(){
        throwException = false;
    }

}