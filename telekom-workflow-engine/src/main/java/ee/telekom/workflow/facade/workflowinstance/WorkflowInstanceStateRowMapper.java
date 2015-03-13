package ee.telekom.workflow.facade.workflowinstance;

import java.sql.ResultSet;
import java.sql.SQLException;

import ee.telekom.workflow.facade.model.WorkflowInstanceState;
import ee.telekom.workflow.util.AbstractRowMapper;

public class WorkflowInstanceStateRowMapper extends AbstractRowMapper<WorkflowInstanceState>{

    public static final WorkflowInstanceStateRowMapper INSTANCE = new WorkflowInstanceStateRowMapper();

    @Override
    public WorkflowInstanceState mapRow( ResultSet rs, int rowNum ) throws SQLException{
        WorkflowInstanceState object = new WorkflowInstanceState();
        object.setRefNum( getLong( rs, "ref_num" ) );
        object.setWorkflowName( getString( rs, "workflow_name" ) );
        object.setWorkflowVersion( getInteger( rs, "workflow_version" ) );
        object.setAttributes( getString( rs, "attributes" ) );
        object.setState( getString( rs, "state" ) );
        object.setHistory( getString( rs, "history" ) );
        object.setLabel1( getString( rs, "label1" ) );
        object.setLabel2( getString( rs, "label2" ) );
        object.setClusterName( getString( rs, "cluster_name" ) );
        object.setNodeName( getString( rs, "node_name" ) );
        object.setStatus( getString( rs, "status" ) );
        object.setLocked( getBoolean( rs, "locked" ) );
        object.setDateCreated( getDate( rs, "date_created" ) );
        object.setDateUpdated( getDate( rs, "date_updated" ) );
        return object;
    }

}
