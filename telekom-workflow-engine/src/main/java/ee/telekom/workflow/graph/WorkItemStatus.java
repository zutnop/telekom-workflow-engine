package ee.telekom.workflow.graph;

public enum WorkItemStatus {

	NEW,

	EXECUTING,

	EXECUTED,

	EXECUTING_ERROR,

	COMPLETING,

	COMPLETED,

	COMPLETING_ERROR,

	CANCELLED;

}
