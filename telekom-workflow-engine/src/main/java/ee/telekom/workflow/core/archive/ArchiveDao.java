package ee.telekom.workflow.core.archive;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceRowMapper;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.core.workitem.WorkItemRowMapper;
import ee.telekom.workflow.util.AbstractWorkflowEngineDao;
import ee.telekom.workflow.util.AdvancedParameterSource;

@Repository
public class ArchiveDao extends AbstractWorkflowEngineDao{

    public void archive( long woinRefNum, int archivePeriodLength ){
        createArchiveWorkflowInstance( woinRefNum, archivePeriodLength );
        createArchiveWorkItems( woinRefNum );
        deleteOriginalWorkItems( woinRefNum );
        deleteOriginalWorkflowInstance( woinRefNum );
    }

    public void cleanup(){
        List<Long> refNums = getJdbcTemplate().queryForList( "SELECT ref_num FROM " + getSchema() + "workflow_instances_archive WHERE cleanup_after IS NOT NULL AND cleanup_after <= CURRENT_TIMESTAMP", Long.class );
        if (!refNums.isEmpty()) {
            AdvancedParameterSource[] sources = new AdvancedParameterSource[refNums.size()];
            for( int i = 0; i < refNums.size(); i++ ){
                sources[i] = new AdvancedParameterSource().addValue( "refNum", refNums.get(i) );
            }
            getNamedParameterJdbcTemplate().batchUpdate("DELETE FROM " + getSchema() + "work_items_archive WHERE woin_ref_num IN (:refNum)", sources);
            getNamedParameterJdbcTemplate().batchUpdate("DELETE FROM " + getSchema() + "workflow_instances_archive WHERE ref_num IN (:refNum)", sources);
        }
    }

    private void createArchiveWorkflowInstance( long woinRefNum, int archivePeriodLength ){
        Map<String, Object> old = getJdbcTemplate().queryForMap( "SELECT * FROM " + getSchema() + "workflow_instances WHERE ref_num = ?", woinRefNum );
        OffsetDateTime cleanup_after = archivePeriodLength < 0 ? null : OffsetDateTime.now().plusDays(archivePeriodLength);
        String sql = ""
                + "INSERT INTO " + getSchema() + "workflow_instances_archive "
                + "  (ref_num, workflow_name, workflow_version, attributes, state, history, label1, label2, cluster_name, locked, status, cleanup_after, date_created, created_by, date_updated, last_updated_by) "
                + " VALUES "
                + "  (:ref_num, :workflow_name, :workflow_version, :attributes, :state, :history, :label1, :label2, :cluster_name, :locked, :status, :cleanup_after, :date_created, :created_by, :date_updated, :last_updated_by)";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addMapWithLowercaseKeys( old )
                .addValue( "cleanup_after", cleanup_after );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    private void createArchiveWorkItems( long woinRefNum ){
        List<Map<String, Object>> old = getJdbcTemplate().queryForList( "SELECT * FROM " + getSchema() + "work_items WHERE woin_ref_num = ?", woinRefNum );
        String sql = ""
                + "INSERT INTO " + getSchema() + "work_items_archive "
                + "  (ref_num, woin_ref_num, token_id, signal, due_date, bean, method, role, user_name, arguments, result, status, date_created, created_by, date_updated, last_updated_by) "
                + " VALUES "
                + "  (:ref_num, :woin_ref_num, :token_id, :signal, :due_date, :bean, :method, :role, :user_name, :arguments, :result, :status, :date_created, :created_by, :date_updated, :last_updated_by)";
        AdvancedParameterSource[] sources = new AdvancedParameterSource[old.size()];
        for( int i = 0; i < old.size(); i++ ){
            sources[i] = new AdvancedParameterSource().addMapWithLowercaseKeys( old.get( i ) );
        }
        getNamedParameterJdbcTemplate().batchUpdate( sql, sources );
        getJdbcTemplate().update( "DELETE FROM " + getSchema() + "work_items WHERE woin_ref_num = ?", woinRefNum );
    }

    private void deleteOriginalWorkflowInstance( long woinRefNum ){
        getJdbcTemplate().update( "DELETE FROM " + getSchema() + "workflow_instances WHERE ref_num = ?", woinRefNum );
    }

    private void deleteOriginalWorkItems( long woinRefNum ){
        getJdbcTemplate().update( "DELETE FROM " + getSchema() + "work_items WHERE woin_ref_num = ?", woinRefNum );
    }

    public WorkflowInstance findWoinByRefNum( long refNum ){
        String sql = "SELECT * FROM " + getSchema() + "workflow_instances_archive WHERE ref_num = ?";
        Object[] args = {refNum};
        List<WorkflowInstance> results = getJdbcTemplate().query( sql, args, WorkflowInstanceRowMapper.INSTANCE );
        return results.isEmpty() ? null : results.get( 0 );
    }

    public WorkItem findWoitByRefNum( long refNum ){
        String sql = "SELECT * FROM " + getSchema() + "work_items_archive WHERE ref_num = ?";
        Object[] args = {refNum};
        List<WorkItem> results = getJdbcTemplate().query( sql, args, WorkItemRowMapper.INSTANCE );
        return results.isEmpty() ? null : results.get( 0 );
    }

    public List<WorkItem> findWoitsByWoinRefNum( long woinRefNum ){
        String sql = "SELECT * FROM " + getSchema() + "work_items_archive WHERE woin_ref_num = ?";
        Object[] args = {woinRefNum};
        return getJdbcTemplate().query( sql, args, WorkItemRowMapper.INSTANCE );
    }

}
