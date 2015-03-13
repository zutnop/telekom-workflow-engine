package ee.telekom.workflow.core.node;

import java.sql.ResultSet;
import java.sql.SQLException;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.util.AbstractRowMapper;

/**
 * Row mapper for {@link WorkflowInstance}s that maps all database fields.
 * 
 * @author Christian Klock
 */
public class NodeRowMapper extends AbstractRowMapper<Node>{

    public static final NodeRowMapper INSTANCE = new NodeRowMapper();

    @Override
    public Node mapRow( ResultSet rs, int rowNum ) throws SQLException{
        Node object = new Node();
        object.setRefNum( getLong( rs, "ref_num" ) );
        object.setNodeName( getString( rs, "node_name" ) );
        object.setClusterName( getString( rs, "cluster_name" ) );
        object.setStatus( getNodeStatus( rs, "status" ) );
        object.setHeartbeat( getDate( rs, "heartbeat" ) );
        return object;
    }

}
