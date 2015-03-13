package ee.telekom.workflow.core.workflowinstance;

import java.sql.ResultSet;
import java.sql.SQLException;

import ee.telekom.workflow.util.AbstractRowMapper;

/**
 * Row mapper for {@link WorkflowInstance}s that maps all database fields except the 
 * ones that are not used by the internal parts of the engine (date_created, created_by,
 * date_last_updated, last_updated_by).
 * 
 * @author Christian Klock
 */
public class WorkflowInstanceRowMapper extends AbstractRowMapper<WorkflowInstance>{

    public static final WorkflowInstanceRowMapper INSTANCE = new WorkflowInstanceRowMapper();

    @Override
    public WorkflowInstance mapRow( ResultSet rs, int rowNum ) throws SQLException{
        WorkflowInstance object = new WorkflowInstance();
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
        object.setStatus( getWorkflowInstanceStatus( rs, "status" ) );
        object.setLocked( getBoolean( rs, "locked" ) );
        return object;
    }

}
