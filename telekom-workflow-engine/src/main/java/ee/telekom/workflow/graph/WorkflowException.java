package ee.telekom.workflow.graph;

/**
 * Generic exception class for workflow execution errors
 */
public class WorkflowException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WorkflowException(String message) {
		super(message);
	}

	public WorkflowException(String message, Throwable cause) {
		super(message, cause);
	}

}
