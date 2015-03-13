package ee.telekom.workflow.core.workflowinstance;

public enum WorkflowInstanceStatus {

	NEW,

	STARTING,

	EXECUTING,

	EXECUTED,

	SUSPENDED,

	ABORT,

	ABORTING,

	ABORTED,

	STARTING_ERROR,

	EXECUTING_ERROR,

	ABORTING_ERROR

}
