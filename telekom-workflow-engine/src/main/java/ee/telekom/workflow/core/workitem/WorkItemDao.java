package ee.telekom.workflow.core.workitem;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import ee.telekom.workflow.graph.WorkItemStatus;
import ee.telekom.workflow.util.AbstractWorkflowEngineDao;
import ee.telekom.workflow.util.AdvancedParameterSource;

@Repository
public class WorkItemDao extends AbstractWorkflowEngineDao{

    public void create( List<WorkItem> woits ){
        for( WorkItem woit : woits ){
            woit.setRefNum( getNextSequenceValue( "woit_ref_num_s" ) );
        }
        String sql = ""
                + "INSERT INTO work_items "
                + "  (ref_num, woin_ref_num, token_id, signal, due_date, bean, method, role, user_name, arguments, result, status, date_created, created_by) "
                + " VALUES "
                + "  (:refNum, :woinRefNum, :tokenId, :signal, :dueDate, :bean, :method, :role, :userName, :arguments, :result, :status, :dateCreated, :createdBy)";
        AdvancedParameterSource[] sources = new AdvancedParameterSource[woits.size()];
        for( int i = 0; i < woits.size(); i++ ){
            sources[i] = new AdvancedParameterSource()
                    .addBean( woits.get( i ) )
                    .addValue( "dateCreated", new Date() )
                    .addValue( "createdBy", getCreatedOrLastUpdatedBy() );
        }
        getNamedParameterJdbcTemplate().batchUpdate( sql, sources );
    }

    public WorkItem findByRefNum( long refNum ){
        String sql = "SELECT * FROM work_items WHERE ref_num = ?";
        Object[] args = {refNum};
        List<WorkItem> results = getJdbcTemplate().query( sql, args, WorkItemRowMapper.INSTANCE );
        return results.isEmpty() ? null : results.get( 0 );
    }

    public List<WorkItem> findByWoinRefNum( long woinRefNum ){
        String sql = "SELECT * FROM work_items WHERE woin_ref_num = ?";
        Object[] args = {woinRefNum};
        return getJdbcTemplate().query( sql, args, WorkItemRowMapper.INSTANCE );
    }

    public List<WorkItem> findActiveByWoinRefNum( long woinRefNum ){
        String sql = "SELECT * FROM work_items WHERE woin_ref_num = ? AND NOT status IN (?,?) ORDER BY ref_num ASC";
        Object[] args = {woinRefNum, WorkItemStatus.COMPLETED.name(), WorkItemStatus.CANCELLED.name()};
        return getJdbcTemplate().query( sql, args, WorkItemRowMapper.INSTANCE );
    }

    public WorkItem findActiveByWoinRefNumAndTokenId( long woinRefNum, int tokenId ){
        String sql = "SELECT * FROM work_items WHERE woin_ref_num = ? AND token_id = ? AND NOT status IN (?,?) ORDER BY ref_num ASC";
        Object[] args = {woinRefNum, tokenId, WorkItemStatus.COMPLETED.name(), WorkItemStatus.CANCELLED.name()};
        List<WorkItem> results = getJdbcTemplate().query( sql, args, WorkItemRowMapper.INSTANCE );
        return results.isEmpty() ? null : results.get( 0 );
    }

    public Collection<WorkItem> findByNodeNameAndStatus( String nodeName, WorkItemStatus status ){
        String sql = ""
                + "SELECT woit.* "
                + "  FROM work_items woit, "
                + "       workflow_instances woin "
                + " WHERE woin.ref_num = woit.woin_ref_num"
                + "   AND woin.node_name = :nodeName "
                + "   AND woit.status = :status";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "nodeName", nodeName )
                .addValue( "status", status );
        return getNamedParameterJdbcTemplate().query( sql, source, WorkItemRowMapper.INSTANCE );
    }

    public void updateResult( long refNum, String result ){
        String sql = ""
                + "UPDATE work_items "
                + "   SET result = :result, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE ref_num = :refNum";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "result", result )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "refNum", refNum );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    public boolean updateStatus( long refNum, WorkItemStatus newStatus, Collection<WorkItemStatus> expectedStatuses ){
        String sql = ""
                + "UPDATE work_items "
                + "   SET status = :newStatus, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE ref_num = :refNum "
                + "   AND status IN (:expectedStatuses)";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "refNum", refNum )
                .addValue( "newStatus", newStatus )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "expectedStatuses", expectedStatuses );
        int count = getNamedParameterJdbcTemplate().update( sql, source );
        return (count == 1);
    }

    public boolean updateStatus( List<Long> refNums, WorkItemStatus newStatus, Collection<WorkItemStatus> expectedStatuses ){
        String sql = ""
                + "UPDATE work_items "
                + "   SET status = :newStatus, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE ref_num IN (:refNums)"
                + "   AND status IN (:expectedStatuses)";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "refNums", refNums )
                .addValue( "newStatus", newStatus )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "expectedStatuses", expectedStatuses );
        int count = getNamedParameterJdbcTemplate().update( sql, source );
        return (count == refNums.size());
    }

    public boolean updateStatusAndResult( long refNum, WorkItemStatus newStatus, WorkItemStatus expectedStatus, String result ){
        String sql = ""
                + "UPDATE work_items "
                + "   SET status = :newStatus, "
                + "       result = :result, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE ref_num = :refNum "
                + "   AND status = :expectedStatus";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "refNum", refNum )
                .addValue( "newStatus", newStatus )
                .addValue( "result", result )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "expectedStatus", expectedStatus );
        int count = getNamedParameterJdbcTemplate().update( sql, source );
        return (count == 1);
    }

}
