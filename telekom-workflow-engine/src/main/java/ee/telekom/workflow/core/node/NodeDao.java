package ee.telekom.workflow.core.node;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import ee.telekom.workflow.util.AbstractWorkflowEngineDao;
import ee.telekom.workflow.util.AdvancedParameterSource;

@Repository
public class NodeDao extends AbstractWorkflowEngineDao{

    public void create( Node node ){
        long refNum = getNextSequenceValue( getSchema() + "node_ref_num_s" );
        node.setRefNum( refNum );
        String sql = ""
                + "INSERT INTO " + getSchema() + "nodes "
                + "  (ref_num, node_name, cluster_name, status, heartbeat) "
                + " VALUES "
                + "  (:refNum, :nodeName, :clusterName, :status, :heartbeat)";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addBean( node );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    public Node findByName( String clusterName, String nodeName ){
        String sql = "SELECT * FROM " + getSchema() + "nodes WHERE cluster_name = ? AND node_name = ?";
        List<Node> results = getJdbcTemplate().query( sql, NodeRowMapper.INSTANCE, clusterName, nodeName );
        return results.isEmpty() ? null : results.getFirst();
    }

    public List<Node> findAll( String clusterName ){
        String sql = "SELECT * FROM " + getSchema() + "nodes WHERE cluster_name = ?";
        return getJdbcTemplate().query( sql, NodeRowMapper.INSTANCE, clusterName );
    }

    public List<Node> findByStatus( String clusterName, NodeStatus status ){
        String sql = "SELECT * FROM " + getSchema() + "nodes WHERE cluster_name = :clusterName AND status = :status";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "clusterName", clusterName )
                .addValue( "status", status );
        return getNamedParameterJdbcTemplate().query( sql, source, NodeRowMapper.INSTANCE );
    }

    public void updateStatusWhereDead( String clusterName, Date criticalDate, NodeStatus newStatus, NodeStatus expectedStatus ){
        String sql = ""
                + "UPDATE " + getSchema() + "nodes "
                + "   SET status = :newStatus "
                + " WHERE cluster_name = :clusterName"
                + "   AND heartBeat < :criticalDate "
                + "   AND status = :expectedStatus ";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "clusterName", clusterName )
                .addValue( "criticalDate", criticalDate )
                .addValue( "newStatus", newStatus )
                .addValue( "expectedStatus", expectedStatus );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    public void updateHeartBeat( String nodeName, Date heartBeat ){
        String sql = ""
                + "UPDATE " + getSchema() + "nodes "
                + "   SET heartBeat = :heartBeat "
                + " WHERE node_name = :nodeName";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "heartBeat", heartBeat )
                .addValue( "nodeName", nodeName );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    public void updateStatus( long refNum, NodeStatus status ){
        String sql = ""
                + "UPDATE " + getSchema() + "nodes "
                + "   SET status = :status "
                + " WHERE ref_num = :refNum";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "status", status )
                .addValue( "refNum", refNum );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

}
