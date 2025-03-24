package ee.telekom.workflow.core.workunit;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.graph.WorkItemStatus;
import ee.telekom.workflow.util.AbstractWorkflowEngineDao;

/**
 * Provides the engine's central polling query.
 *
 * @author Christian Klock
 */
@Repository
public class WorkUnitDao extends AbstractWorkflowEngineDao{

    // The poll query does not change so we can store it as a constant
    private String sqlCache;
    private String cachedSqlClusterName;

    /**
     * Retrieves the list of work units that can be performed at the given date.
     */
    public List<WorkUnit> findNewWorkUnits( Date now, String clusterName ){
        if( sqlCache == null || !Objects.equals( cachedSqlClusterName, clusterName ) ){
            sqlCache = getSql( clusterName );
            cachedSqlClusterName = clusterName;
        }
        return getJdbcTemplate().query( sqlCache, WorkUnitRowMapper.INSTANCE, now);
    }

    /*
    * Note: The (a) sub-query is written deliberately before the sub-queries (b) and (c).
    *       (b) and (c) return null values for woit_ref_num and waiting_since. PostgreSQL is only able to
    *       determine the type of this fields if there is a preceding sub-query (that is (a) in our case)
    *       that returns a non-null value for that field.
    *       If (b) and (c) were preceding (a), than PostgreSQL would use VARCHAR as a fallback type for the
    *       null value fields. This would make the UNION of the sub-queries fail due to a type mismatch.
    */
    private String getSql( String clusterName ){
        String clusterCondition = getClusterCondition( clusterName );
        String sql = ""

                // (a) completing signals, tasks, human tasks
                + "SELECT woin.ref_num AS woin_ref_num,     "
                + "       '" + WorkType.COMPLETE_WORK_ITEM.name() + "' AS type, "
                + "       woit.ref_num AS woit_ref_num,            "
                + "       woit.date_updated AS waiting_since "
                + "  FROM " + getSchema() + "work_items woit                  "
                + "  JOIN " + getSchema() + "workflow_instances woin "
                + "    ON woin.ref_num = woit.woin_ref_num "
                + " WHERE woin.status  = '" + WorkflowInstanceStatus.EXECUTING.name() + "' "
                + "   AND woin.locked  = 'N' "
                + clusterCondition
                + "   AND woit.status  = '" + WorkItemStatus.EXECUTED.name() + "' "
                + "   AND woit.due_date IS NULL "

                + "UNION ALL "

                // (b) starting workflows
                + "SELECT woin.ref_num AS woin_ref_num, "
                + "       '" + WorkType.START_WORKFLOW.name() + "' AS type, "
                + "       null AS woit_ref_num, "
                + "       null AS waiting_since "
                + "  FROM " + getSchema() + "workflow_instances woin "
                + " WHERE woin.status = '" + WorkflowInstanceStatus.NEW.name() + "' "
                + "   AND woin.locked = 'N' "
                + clusterCondition

                + " UNION ALL "

                // (c) aborting workflows
                + "SELECT woin.ref_num AS woin_ref_num, "
                + "       '" + WorkType.ABORT_WORKFLOW.name() + "' AS type, "
                + "       null AS woit_ref_num, "
                + "       null AS waiting_since "
                + "  FROM " + getSchema() + "workflow_instances woin "
                + " WHERE woin.status = '" + WorkflowInstanceStatus.ABORT.name() + "' "
                + "   AND woin.locked = 'N' "
                + clusterCondition

                + " UNION ALL "

                // (d) completing timers
                + "SELECT woin.ref_num AS woin_ref_num,     "
                + "       '" + WorkType.COMPLETE_WORK_ITEM.name() + "' AS type, "
                + "       woit.ref_num AS woit_ref_num,            "
                + "       woit.due_date AS waiting_since "
                + "  FROM " + getSchema() + "work_items woit                  "
                + "  JOIN " + getSchema() + "workflow_instances woin "
                + "    ON woin.ref_num = woit.woin_ref_num "
                + " WHERE woin.status  = '" + WorkflowInstanceStatus.EXECUTING.name() + "' "
                + "   AND woin.locked  = 'N' "
                + clusterCondition
                + "   AND woit.status  = '" + WorkItemStatus.NEW.name() + "' "
                + "   AND woit.due_date < ? "

                + "UNION ALL "

                // (e) executing tasks
                + "SELECT woin.ref_num AS woin_ref_num,     "
                + "       '" + WorkType.EXECUTE_TASK.name() + "' AS type, "
                + "       woit.ref_num AS woit_ref_num,            "
                + "       COALESCE(woit.date_updated, woit.date_created) AS waiting_since "
                + "  FROM " + getSchema() + "work_items woit                  "
                + "  JOIN " + getSchema() + "workflow_instances woin "
                + "    ON woin.ref_num = woit.woin_ref_num "
                + " WHERE woin.status  = '" + WorkflowInstanceStatus.EXECUTING.name() + "' "
                + "   AND woin.locked  = 'N' "
                + clusterCondition
                + "   AND woit.status  = '" + WorkItemStatus.NEW.name() + "' "
                + "   AND NOT woit.bean IS NULL "

                + " ORDER BY woin_ref_num ASC, waiting_since ASC NULLS FIRST, woit_ref_num ASC";
        return sql.replaceAll( "\\s+", " " );
    }

    private static String getClusterCondition( String clusterName ){
        if( clusterName == null || clusterName.trim().isEmpty() ){
            return "   AND woin.cluster_name IS NULL ";
        }
        else{
            String escapedClusterName = StringUtils.replace(clusterName, "'", "''");
            return "   AND woin.cluster_name = '" + escapedClusterName + "'";
        }
    }
}
