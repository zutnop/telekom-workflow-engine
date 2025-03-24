package ee.telekom.workflow.core.workflowinstance;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import ee.telekom.workflow.util.AbstractWorkflowEngineDao;
import ee.telekom.workflow.util.AdvancedParameterSource;

@Repository
public class WorkflowInstanceDao extends AbstractWorkflowEngineDao{

    public void create( WorkflowInstance woin ){
        long refNum = getNextSequenceValue( getSchema() + "woin_ref_num_s" );
        woin.setRefNum( refNum );
        String sql = ""
                + "INSERT INTO " + getSchema() + "workflow_instances "
                + "  (ref_num, workflow_name, workflow_version, attributes, state, label1, label2, cluster_name, locked, status, date_created, created_by) "
                + " VALUES "
                + "  (:refNum, :workflowName, :workflowVersion, :attributes, :state, :label1, :label2, :clusterName, :locked, :status, :dateCreated, :createdBy)";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addBean( woin )
                .addValue( "dateCreated", new Date() )
                .addValue( "createdBy", getCreatedOrLastUpdatedBy() );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    public WorkflowInstance findByRefNum( long refNum ){
        String sql = "SELECT * FROM " + getSchema() + "workflow_instances WHERE ref_num = ?";
        List<WorkflowInstance> results = getJdbcTemplate().query( sql, WorkflowInstanceRowMapper.INSTANCE, refNum );
        return results.isEmpty() ? null : results.getFirst();
    }

    public WorkflowInstanceStatus findStatusByRefNum( long refNum ){
        String sql = "SELECT status FROM " + getSchema() + "workflow_instances WHERE ref_num = ?";
        List<String> results = getJdbcTemplate().queryForList( sql, String.class, refNum );
        return results.isEmpty() ? null : WorkflowInstanceStatus.valueOf(results.getFirst() );
    }

    public boolean updateAndUnlock( long refNum,
                                 int workflowVersion,
                                 String attributes,
                                 String history,
                                 String state,
                                 WorkflowInstanceStatus newStatus,
                                 Collection<WorkflowInstanceStatus> expectedStatuses ){
        String sql = ""
                + "UPDATE " + getSchema() + "workflow_instances "
                + "   SET workflow_version = :workflowVersion, "
                + "       attributes = :attributes, "
                + "       history = :history, "
                + "       state = :state, "
                + "       locked = :locked, "
                + "       node_name = :nodeName, "
                + "       status = :status, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE ref_num = :refNum "
                + "   AND status IN (:expectedStatuses)";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "refNum", refNum )
                .addValue( "workflowVersion", workflowVersion )
                .addValue( "attributes", attributes )
                .addValue( "history", history )
                .addValue( "state", state )
                .addValue( "locked", false )
                .addValue( "nodeName", null )
                .addValue( "status", newStatus )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "expectedStatuses", expectedStatuses );
        int count = getNamedParameterJdbcTemplate().update( sql, source );
        return (count == 1);
    }

    public boolean updateStatus( long refNum, WorkflowInstanceStatus newStatus, Collection<WorkflowInstanceStatus> expectedStatuses ){
        String sql = ""
                + "UPDATE " + getSchema() + "workflow_instances "
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

    public int recover( String nodeName, WorkflowInstanceStatus status, WorkflowInstanceStatus recoveredStatus ){
        String sql = ""
                + "UPDATE " + getSchema() + "workflow_instances "
                + "   SET status = :recoveredStatus, "
                + "       locked = :recoveredLocked,"
                + "       node_name = NULL, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE node_name = :nodeName "
                + "   AND status = :status ";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "recoveredStatus", recoveredStatus )
                .addValue( "recoveredLocked", false )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "nodeName", nodeName )
                .addValue( "status", status );
        int count = getNamedParameterJdbcTemplate().update( sql, source );
        return count;
    }

    public int recoverNotAssigned( String clusterName ){
        String sql = ""
                + "UPDATE " + getSchema() + "workflow_instances "
                + "   SET locked = :recoveredLocked,"
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE cluster_name = :clusterName "
                + "   AND node_name IS NULL "
                + "   AND locked = :locked "
                + "   AND status IN (:expectedStatuses)";
        Collection<WorkflowInstanceStatus> expectedStatuses = Arrays.asList(
                WorkflowInstanceStatus.NEW,
                WorkflowInstanceStatus.EXECUTING,
                WorkflowInstanceStatus.ABORT );
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "recoveredLocked", false )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "clusterName", clusterName )
                .addValue( "locked", true )
                .addValue( "expectedStatuses", expectedStatuses );
        int count = getNamedParameterJdbcTemplate().update( sql, source );
        return count;
    }

    public void updateLock( List<Long> refNums, boolean locked ){
        String sql = ""
                + "UPDATE " + getSchema() + "workflow_instances "
                + "   SET locked = :locked, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE ref_num in (:refNums) ";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "refNums", refNums )
                .addValue( "locked", locked )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    public boolean updateNodeNameFromNull( long refNum, String nodeName ){
        String sql = ""
                + "UPDATE " + getSchema() + "workflow_instances "
                + "   SET node_name = :nodeName, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE ref_num = :refNum "
                + "   AND node_name IS NULL ";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "refNum", refNum )
                .addValue( "nodeName", nodeName );
        int count = getNamedParameterJdbcTemplate().update( sql, source );
        return (count == 1);
    }

    public void updateLockAndNodeName( long refNum, boolean locked, String nodeName ){
        String sql = ""
                + "UPDATE " + getSchema() + "workflow_instances "
                + "   SET locked = :locked, "
                + "       node_name = :nodeName, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE ref_num = :refNum ";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "refNum", refNum )
                .addValue( "locked", locked )
                .addValue( "nodeName", nodeName )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    public boolean updateState( long refNum, String state, WorkflowInstanceStatus expectedStatus ){
        String sql = ""
                + "UPDATE " + getSchema() + "workflow_instances "
                + "   SET state = :state, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE ref_num = :refNum "
                + "   AND status = :expectedStatus";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "refNum", refNum )
                .addValue( "state", state )
                .addValue( "expectedStatus", expectedStatus )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() );
        int count = getNamedParameterJdbcTemplate().update( sql, source );
        return (count == 1);
    }

    public boolean updateHistory( Long refNum, String history, WorkflowInstanceStatus expectedStatus ){
        String sql = ""
                + "UPDATE " + getSchema() + "workflow_instances "
                + "   SET history = :history, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE ref_num = :refNum "
                + "   AND status = :expectedStatus";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "refNum", refNum )
                .addValue( "history", history )
                .addValue( "expectedStatus", expectedStatus )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() );
        int count = getNamedParameterJdbcTemplate().update( sql, source );
        return (count == 1);
    }

    public List<WorkflowInstance> findStuck( String clusterName, int workItemExecutionTimeWarnSeconds ){
        Calendar thresholdTimestamp = Calendar.getInstance();
        thresholdTimestamp.add( Calendar.SECOND, workItemExecutionTimeWarnSeconds * -1 );

        String sql = ""
                + "SELECT * "
                + "FROM " + getSchema() + "workflow_instances "
                + "WHERE cluster_name = :clusterName "
                + "  AND locked = 'Y' "
                + "  AND status NOT IN (:ignoredStatuses) "
                + "  AND date_updated < :dateUpdated";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "clusterName", clusterName )
                .addValue( "ignoredStatuses", Arrays.asList( WorkflowInstanceStatus.EXECUTING_ERROR
                        , WorkflowInstanceStatus.ABORTING_ERROR
                        , WorkflowInstanceStatus.STARTING_ERROR ))
                .addValue( "dateUpdated", thresholdTimestamp.getTime() );
        return getNamedParameterJdbcTemplate().query( sql, source, WorkflowInstanceRowMapper.INSTANCE );
    }

}
