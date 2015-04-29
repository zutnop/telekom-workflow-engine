package ee.telekom.workflow.facade.workitem;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.facade.model.WorkItemState;
import ee.telekom.workflow.facade.util.SqlUtil;
import ee.telekom.workflow.graph.WorkItemStatus;
import ee.telekom.workflow.util.AbstractWorkflowEngineDao;
import ee.telekom.workflow.util.AdvancedParameterSource;

/**
 * Work item DAO class, providing methods exclusively used by the facade.
 *
 * @author Christian Klock
 */
@Repository
public class WorkItemStateDao extends AbstractWorkflowEngineDao{

    @Autowired
    private WorkflowEngineConfiguration config;

    public WorkItemState find( long woitRefNum, boolean isInstanceActive ){
        String sql = ""
                + "SELECT woit.* "
                + "  FROM " + getTableName( isInstanceActive ) + " woit "
                + "  JOIN " + getInstanceTableName( isInstanceActive ) + " woin on woit.woin_ref_num = woin.ref_num"
                + " WHERE woit.ref_num = ? "
                + "   AND woin.cluster_name = ?";
        Object[] args = {woitRefNum, config.getClusterName()};
        List<WorkItemState> result = getJdbcTemplate().query( sql, args, WorkItemStateRowMapper.INSTANCE );
        return result.isEmpty() ? null : result.get( 0 );
    }

    public List<WorkItemState> findByWoinRefNum( long woinRefNum, boolean isInstanceActive ){
        String sql = ""
                + "SELECT woit.* "
                + "  FROM " + getTableName( isInstanceActive ) + " woit "
                + "  JOIN " + getInstanceTableName( isInstanceActive ) + " woin on woit.woin_ref_num = woin.ref_num"
                + " WHERE woit.woin_ref_num = ? "
                + "   AND woin.cluster_name = ? "
                + " ORDER BY woit.ref_num DESC ";
        Object[] args = {woinRefNum, config.getClusterName()};
        return getJdbcTemplate().query( sql, args, WorkItemStateRowMapper.INSTANCE );
    }

    public WorkItemState findActive( long woinRefNum, int tokenId ){
        String sql = ""
                + "SELECT woit.* "
                + "  FROM " + getTableName( true ) + " woit "
                + "  JOIN " + getInstanceTableName( true ) + " woin on woit.woin_ref_num = woin.ref_num"
                + " WHERE woin.ref_num = ? "
                + "   AND woin.cluster_name = ? "
                + "   AND woit.token_id = ? "
                + "   AND NOT woit.status IN (?,?)";
        Object[] args = {woinRefNum, config.getClusterName(), tokenId, WorkItemStatus.COMPLETED.name(), WorkItemStatus.CANCELLED.name()};
        List<WorkItemState> result = getJdbcTemplate().query( sql, args, WorkItemStateRowMapper.INSTANCE );
        return result.isEmpty() ? null : result.get( 0 );
    }

    public List<WorkItemState> findActiveByRole( String role ){
        String sql = ""
                + "SELECT woit.* "
                + "  FROM " + getSchema() + "work_items woit "
                + "  JOIN " + getSchema() + "workflow_instances woin on woit.woin_ref_num = woin.ref_num "
                + " WHERE woit.status = :status "
                + "   AND woin.cluster_name = :clusterName "
                + "   AND woit.role = :role ";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "status", WorkItemStatus.NEW )
                .addValue( "clusterName", config.getClusterName() )
                .addValue( "role", role );
        return getNamedParameterJdbcTemplate().query( sql.toString(), source, WorkItemStateRowMapper.INSTANCE );
    }

    public List<WorkItemState> findActiveByUser( String user ){
        String sql = ""
                + "SELECT woit.* "
                + "  FROM " + getSchema() + "work_items woit "
                + "  JOIN " + getSchema() + "workflow_instances woin on woit.woin_ref_num = woin.ref_num "
                + " WHERE woit.status = :status "
                + "   AND woin.cluster_name = :clusterName "
                + "   AND woit.user_name = :user ";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "status", WorkItemStatus.NEW )
                .addValue( "clusterName", config.getClusterName() )
                .addValue( "user", user );
        return getNamedParameterJdbcTemplate().query( sql.toString(), source, WorkItemStateRowMapper.INSTANCE );
    }

    public List<WorkItemState> findActiveByRoleAndUser( String role, String user ){
        StringBuilder sql = new StringBuilder( ""
                + "SELECT woit.* "
                + "  FROM " + getSchema() + "work_items woit "
                + "  JOIN " + getSchema() + "workflow_instances woin on woit.woin_ref_num = woin.ref_num "
                + " WHERE woit.status = :status "
                + "   AND woin.cluster_name = :clusterName " );
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "status", WorkItemStatus.NEW )
                .addValue( "clusterName", config.getClusterName() );
        if( role == null ){
            sql.append( " AND role IS NULL " );
        }
        else{
            sql.append( " AND role = :role " );
            source.addValue( "role", role );
        }
        if( user == null ){
            sql.append( " AND user_name IS NULL " );
        }
        else{
            sql.append( " AND user_name = :userName " );
            source.addValue( "userName", user );
        }
        return getNamedParameterJdbcTemplate().query( sql.toString(), source, WorkItemStateRowMapper.INSTANCE );
    }

    public void updateStatusAndResultByInstanceAndSignal( long woinRefNum,
                                                          String signal,
                                                          WorkItemStatus newStatus,
                                                          WorkItemStatus expectedStatus,
                                                          String result ){
        String sql = ""
                + "UPDATE " + getSchema() + "work_items "
                + "   SET status = :newStatus, "
                + "       result = :result, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                // protection against updates of workflow instances in a different cluster
                + " WHERE woin_ref_num IN (SELECT ref_num FROM " + getSchema() + "workflow_instances WHERE ref_num = :woinRefNum AND cluster_name = :clusterName) "
                + "   AND signal = :signal "
                + "   AND status = :expectedStatus";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "newStatus", newStatus )
                .addValue( "result", result )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "woinRefNum", woinRefNum )
                .addValue( "clusterName", config.getClusterName() )
                .addValue( "signal", signal )
                .addValue( "expectedStatus", expectedStatus );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    public void updateStatusAndResultByWorkItemAndSignal( long woitRefNum,
                                                          String signal,
                                                          WorkItemStatus newStatus,
                                                          WorkItemStatus expectedStatus,
                                                          String result ){
        String sql = ""
                + "UPDATE " + getSchema() + "work_items AS woit"
                + "   SET status = :newStatus, "
                + "       result = :result, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                // protection against updates of workflow instances in a different cluster
                + " WHERE woin_ref_num IN (SELECT ref_num FROM " + getSchema() + "workflow_instances WHERE ref_num = woit.woin_ref_num AND cluster_name = :clusterName) "
                + "   AND ref_num = :woitRefNum "
                + "   AND signal = :signal "
                + "   AND status = :expectedStatus";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "newStatus", newStatus )
                .addValue( "result", result )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "woitRefNum", woitRefNum )
                .addValue( "clusterName", config.getClusterName() )
                .addValue( "signal", signal )
                .addValue( "expectedStatus", expectedStatus );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    public void updateStatusAndResultByLabel1AndSignal( String label1,
                                                        String signal,
                                                        WorkItemStatus newStatus,
                                                        WorkItemStatus expectedStatus,
                                                        String result ){
        String sql = ""
                + "UPDATE " + getSchema() + "work_items "
                + "   SET status = :newStatus, "
                + "       result = :result, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE woin_ref_num IN ("
                + "     SELECT ref_num "
                + "       FROM " + getSchema() + "workflow_instances "
                + "      WHERE label1 " + (StringUtils.isBlank( label1 ) ? "IS NULL" : "= :label1")
                + "        AND cluster_name = :clusterName) "
                + "   AND signal = :signal "
                + "   AND status = :expectedStatus";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "newStatus", newStatus )
                .addValue( "result", result )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "label1", label1 )
                .addValue( "clusterName", config.getClusterName() )
                .addValue( "signal", signal )
                .addValue( "expectedStatus", expectedStatus );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    public void updateStatusAndResultByLabelsAndSignal( String label1,
                                                        String label2,
                                                        String signal,
                                                        WorkItemStatus newStatus,
                                                        WorkItemStatus expectedStatus,
                                                        String result ){
        String sql = ""
                + "UPDATE " + getSchema() + "work_items "
                + "   SET status = :newStatus, "
                + "       result = :result, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                + " WHERE woin_ref_num IN ("
                + "     SELECT ref_num "
                + "       FROM " + getSchema() + "workflow_instances "
                + "      WHERE label1 " + (StringUtils.isBlank( label1 ) ? "IS NULL" : "= :label1")
                + "        AND label2 " + (StringUtils.isBlank( label2 ) ? "IS NULL" : "= :label2")
                + "        AND cluster_name = :clusterName) "
                + "   AND signal = :signal "
                + "   AND status = :expectedStatus";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "newStatus", newStatus )
                .addValue( "result", result )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "label1", label1 )
                .addValue( "label2", label2 )
                .addValue( "clusterName", config.getClusterName() )
                .addValue( "signal", signal )
                .addValue( "expectedStatus", expectedStatus );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    public boolean updateDueDate( long woitRefNum, Date dueDate, WorkItemStatus expectedStatus ){
        String sql = ""
                + "UPDATE " + getSchema() + "work_items AS woit "
                + "   SET due_date = :dueDate, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                // protection against updates of workflow instances in a different cluster
                + " WHERE woin_ref_num IN (SELECT ref_num FROM " + getSchema() + "workflow_instances WHERE ref_num = woit.woin_ref_num AND cluster_name = :clusterName) "
                + "   AND ref_num = :refNum "
                + "   AND status = :expectedStatus";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "dueDate", dueDate )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "clusterName", config.getClusterName() )
                .addValue( "refNum", woitRefNum )
                .addValue( "expectedStatus", expectedStatus );
        int count = getNamedParameterJdbcTemplate().update( sql, source );
        return (count == 1);
    }

    public boolean updateUserName( long refNum, WorkItemStatus expectedStatus, String userName ){
        String sql = ""
                + "UPDATE " + getSchema() + "work_items AS woit"
                + "   SET user_name = :userName, "
                + "       date_updated = :dateUpdated, "
                + "       last_updated_by = :lastUpdatedBy "
                // protection against updates of workflow instances in a different cluster
                + " WHERE woin_ref_num IN (SELECT ref_num FROM " + getSchema() + "workflow_instances WHERE ref_num = woit.woin_ref_num AND cluster_name = :clusterName) "
                + "   AND ref_num = :refNum "
                + "   AND status = :expectedStatus";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "userName", userName )
                .addValue( "dateUpdated", new Date() )
                .addValue( "lastUpdatedBy", getCreatedOrLastUpdatedBy() )
                .addValue( "clusterName", config.getClusterName() )

                .addValue( "refNum", refNum )
                .addValue( "expectedStatus", expectedStatus );
        int count = getNamedParameterJdbcTemplate().update( sql, source );
        return (count == 1);
    }

    public Map<Long, Date> findNextActiveTimerDueDates( List<Long> woinRefNums ){
        Map<Long, Date> nextActiveTimerDueDates = new HashMap<>();
        String sql = ""
                + "SELECT woin_ref_num, min(due_date) AS due_date "
                + "  FROM " + getSchema() + "work_items "
                + " WHERE woin_ref_num IN (:refNums) "
                + "   AND status = :status "
                + "   AND NOT due_date IS NULL "
                + " GROUP BY woin_ref_num "
                + " ORDER BY woin_ref_num";
        for( List<Long> partition : SqlUtil.partition( woinRefNums, 1000 ) ){
            AdvancedParameterSource source = new AdvancedParameterSource()
                    .addValue( "refNums", partition )
                    .addValue( "status", WorkItemStatus.NEW );
            List<Map<String, Object>> results = getNamedParameterJdbcTemplate().queryForList( sql, source );
            for( Map<String, Object> element : results ){
                Number woinRefNum = (Number)element.get( "woin_ref_num" );
                Date nextDueDate = (Date)element.get( "due_date" );
                nextActiveTimerDueDates.put( woinRefNum.longValue(), nextDueDate );
            }
        }
        return nextActiveTimerDueDates;
    }

    public Set<Long> findHasActiveHumanTask( List<Long> woinRefNums ){
        Set<Long> hasActiveHumanTask = new TreeSet<>();
        String sql = ""
                + "SELECT woin_ref_num "
                + "  FROM " + getSchema() + "work_items "
                + " WHERE woin_ref_num IN (:refNums) "
                + "   AND status = :status "
                + "   AND (NOT role IS NULL OR NOT user_name IS NULL) "
                + "  GROUP BY woin_ref_num "
                + "  ORDER BY woin_ref_num";
        for( List<Long> partition : SqlUtil.partition( woinRefNums, 1000 ) ){
            AdvancedParameterSource source = new AdvancedParameterSource()
                    .addValue( "refNums", partition )
                    .addValue( "status", WorkItemStatus.NEW );
            hasActiveHumanTask.addAll( getNamedParameterJdbcTemplate().queryForList( sql, source, Long.class ) );
        }
        return hasActiveHumanTask;
    }

    private String getTableName( boolean isInstanceActive ){
        return getSchema() + (isInstanceActive ? "work_items" : "work_items_archive");
    }

    private String getInstanceTableName( boolean isInstanceActive ){
        return getSchema() + (isInstanceActive ? "workflow_instances" : "workflow_instances_archive");
    }

}
