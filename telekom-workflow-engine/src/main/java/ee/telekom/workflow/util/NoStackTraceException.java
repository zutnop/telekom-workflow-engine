package ee.telekom.workflow.util;

public class NoStackTraceException extends Exception{

    private static final long serialVersionUID = 1L;

    public NoStackTraceException( String message ){
        super( message, null, true, false );
    }

}
