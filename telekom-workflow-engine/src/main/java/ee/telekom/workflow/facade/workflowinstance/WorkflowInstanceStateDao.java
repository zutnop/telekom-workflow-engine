package ee.telekom.workflow.facade.workflowinstance;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.facade.model.SearchWorkflowInstances;
import ee.telekom.workflow.facade.model.WorkflowInstanceFacadeStatus;
import ee.telekom.workflow.facade.model.WorkflowInstanceState;
import ee.telekom.workflow.facade.util.StatusUtil;
import ee.telekom.workflow.util.AbstractWorkflowEngineDao;
import ee.telekom.workflow.util.AdvancedParameterSource;

/**
 * Workflow instance DAO, providing methods exclusively used by the facade.
 *
 * @author Christian Klock
 */
@Repository
public class WorkflowInstanceStateDao extends AbstractWorkflowEngineDao{

    @Autowired
    private WorkflowEngineConfiguration config;

    public WorkflowInstanceState find( long refNum, boolean isActive ){
        String sql = "SELECT * FROM " + getTableName( isActive ) + " WHERE ref_num = ? AND cluster_name = ?";
        Object[] args = {refNum, config.getClusterName()};
        List<WorkflowInstanceState> result = getJdbcTemplate().query( sql, args, WorkflowInstanceStateRowMapper.INSTANCE );
        return result.isEmpty() ? null : result.get( 0 );
    }

    public List<WorkflowInstanceState> findByLabel1( String label1, boolean activeOnly ){
        String where = ""
                + " WHERE cluster_name = :clusterName "
                + "   AND label1 " + (StringUtils.isBlank( label1 ) ? "IS NULL" : "= :label1 ");
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "clusterName", config.getClusterName() )
                .addValue( "label1", label1 );
        String sql;
        if( activeOnly ){
            sql = "SELECT * FROM " + getTableName( true ) + where;
        }
        else{
            sql = ""
                    + "SELECT * FROM " + getTableName( true ) + where
                    + " UNION ALL "
                    + "SELECT * FROM " + getTableName( false ) + where;
        }
        return getNamedParameterJdbcTemplate().query( sql, source, WorkflowInstanceStateRowMapper.INSTANCE );
    }

    public List<WorkflowInstanceState> findByLabels( String label1, String label2, boolean activeOnly ){
        String where = ""
                + " WHERE cluster_name = :clusterName "
                + "   AND label1 " + (StringUtils.isBlank( label1 ) ? "IS NULL" : "= :label1 ")
                + "   AND label2 " + (StringUtils.isBlank( label2 ) ? "IS NULL" : "= :label2 ");
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "clusterName", config.getClusterName() )
                .addValue( "label1", label1 )
                .addValue( "label2", label2 );
        String sql;
        if( activeOnly ){
            sql = "SELECT * FROM " + getTableName( true ) + where;
        }
        else{
            sql = ""
                    + "SELECT * FROM " + getTableName( true ) + where
                    + " UNION ALL "
                    + "SELECT * FROM " + getTableName( false ) + where;
        }
        return getNamedParameterJdbcTemplate().query( sql, source, WorkflowInstanceStateRowMapper.INSTANCE );
    }

    public List<WorkflowInstanceState> find( SearchWorkflowInstances request ){
        String select = "SELECT * ";
        StringBuilder where = new StringBuilder( " WHERE cluster_name = :clusterName " );
        AdvancedParameterSource source = new AdvancedParameterSource().addValue( "clusterName", config.getClusterName() );
        if( request.getRefNum() != null && request.getRefNum().size() > 0 ){
            where.append( " AND ref_num IN (:refNum) " );
            source.addValue( "refNum", request.getRefNum() );
        }
        if( request.getWorkflowName() != null && request.getWorkflowName().size() > 0 ){
            where.append( " AND workflow_name IN (:workflowName) " );
            source.addValue( "workflowName", request.getWorkflowName() );
        }
        if( request.getLabel1() != null && request.getLabel1().size() > 0 ){
            where.append( " AND label1 IN (:label1) " );
            source.addValue( "label1", request.getLabel1() );
        }
        if( request.getLabel2() != null && request.getLabel2().size() > 0 ){
            where.append( " AND label2 IN (:label2) " );
            source.addValue( "label2", request.getLabel2() );
        }
        if( request.getStatus() != null && request.getStatus().size() > 0 ){
            where.append( " AND status IN (:statuses) " );
            source.addValue( "statuses", StatusUtil.toInternal( request.getStatus() ) );
        }
        String sql;
        boolean needToQueryMainTable = needToQueryMainTable( request.getStatus() );
        boolean needToQueryArchiveTable = needToQueryArchiveTable( request.getStatus() );
        if( needToQueryMainTable && needToQueryArchiveTable ){
            sql = ""
                    + select + "FROM " + getTableName( true ) + where
                    + "UNION ALL "
                    + select + "FROM " + getTableName( false ) + where;
        }
        else if( needToQueryArchiveTable ){
            sql = select + "FROM " + getTableName( false ) + where;
        }
        else{
            sql = select + "FROM " + getTableName( true ) + where;
        }
        if ( request.getColumn() != null ) {
            WorkflowInstancesDataTableColumnMapper column = WorkflowInstancesDataTableColumnMapper.from(request.getColumn());
            String direction = "ASC".equalsIgnoreCase(request.getDirection()) ? "ASC" : "DESC";
            sql += " ORDER BY  " + column.getFieldName() + " " + direction + " ";
        }
        int limit = request.getLength();
        int offset = request.getStart();
        if (limit != 0) {
            sql += " LIMIT :limit OFFSET :offset ";
            source.addValue("limit", limit)
                    .addValue("offset", offset);
        }
        return getNamedParameterJdbcTemplate().query( sql, source, WorkflowInstanceStateRowMapper.INSTANCE );
    }

    public List<WorkflowStatusCount> findWorklowStatusCount(){
        String select = "SELECT workflow_name, status, count(1) as count ";
        String where = " WHERE cluster_name = :clusterName ";
        String group = " GROUP BY workflow_name, status ";
        String sql = ""
                + select + "FROM " + getTableName( true ) + where + group
                + "UNION ALL "
                + select + "FROM " + getTableName( false ) + where + group;
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "clusterName", config.getClusterName() );
        return getNamedParameterJdbcTemplate().query( sql, source, WorkflowStatusCountRowMapper.INSTANCE );
    }

    public List<String> findWorkflowNamesWithInstances(){
        String select = "SELECT DISTINCT workflow_name ";
        String where = " WHERE cluster_name = :clusterName ";
        String sql = ""
                + select + "FROM " + getTableName( true ) + where
                + "UNION "
                + select + "FROM " + getTableName( false ) + where;
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "clusterName", config.getClusterName() );
        return getNamedParameterJdbcTemplate().queryForList( sql, source, String.class );
    }

    private String getTableName( boolean isActive ){
        return getSchema() + (isActive ? "workflow_instances" : "workflow_instances_archive");
    }

    private static boolean needToQueryMainTable( List<WorkflowInstanceFacadeStatus> statuses ){
        if( statuses == null || statuses.isEmpty() ){
            return true;
        }
        for( WorkflowInstanceFacadeStatus status : statuses ){
            if( status != WorkflowInstanceFacadeStatus.EXECUTED && status != WorkflowInstanceFacadeStatus.ABORTED ){
                return true;
            }
        }
        return false;
    }

    private static boolean needToQueryArchiveTable( List<WorkflowInstanceFacadeStatus> statuses ){
        if( statuses == null || statuses.isEmpty() ){
            return true;
        }
        for( WorkflowInstanceFacadeStatus status : statuses ){
            if( status == WorkflowInstanceFacadeStatus.EXECUTED || status == WorkflowInstanceFacadeStatus.ABORTED ){
                return true;
            }
        }
        return false;
    }

}
