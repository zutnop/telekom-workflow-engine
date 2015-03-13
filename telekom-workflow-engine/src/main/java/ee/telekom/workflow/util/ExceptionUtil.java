package ee.telekom.workflow.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

import ee.telekom.workflow.graph.WorkflowException;

public class ExceptionUtil{

    public static String getErrorText( Exception exception ){
        Throwable cause = getWorkflowCause( exception );
        return cause == null ? exception.getMessage() : cause.getMessage();
    }

    public static String getErrorDetails( Exception exception ){
        return ExceptionUtils.getStackTrace( exception );
    }

    /**
     * Finds the first cause in the exception hierarchy that is not an instance
     * of {@link WorkflowException}.
     */
    private static Throwable getWorkflowCause( Exception exception ){
        Throwable e = exception;
        while( e instanceof WorkflowException ){
            e = e.getCause();
        }
        return e;
    }

}
