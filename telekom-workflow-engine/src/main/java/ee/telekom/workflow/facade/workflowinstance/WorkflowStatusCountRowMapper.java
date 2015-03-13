package ee.telekom.workflow.facade.workflowinstance;

import java.sql.ResultSet;
import java.sql.SQLException;

import ee.telekom.workflow.util.AbstractRowMapper;

public class WorkflowStatusCountRowMapper extends AbstractRowMapper<WorkflowStatusCount>{

    public static final WorkflowStatusCountRowMapper INSTANCE = new WorkflowStatusCountRowMapper();

    @Override
    public WorkflowStatusCount mapRow( ResultSet rs, int rowNum ) throws SQLException{
        WorkflowStatusCount object = new WorkflowStatusCount();
        object.setWorkflowName( getString( rs, "workflow_name" ) );
        object.setStatus( getWorkflowInstanceStatus( rs, "status" ) );
        object.setCount( getInteger( rs, "count" ) );
        return object;
    }

}
