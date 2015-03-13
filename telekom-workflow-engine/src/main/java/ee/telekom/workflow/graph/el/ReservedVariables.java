package ee.telekom.workflow.graph.el;

/**
 * This interface lists the reserved variable names that have a special handling (as oppose to resolving from Environment) when evaluating an EL expression.
 * 
 * @author Erko Hansar
 */
public interface ReservedVariables{

    // evaluates to current Date
    String NOW = "NOW";

    // evaluates to current workflow instance id (ref_num) value
    String WORKFLOW_INSTANCE_ID = "WORKFLOW_INSTANCE_ID";

}
