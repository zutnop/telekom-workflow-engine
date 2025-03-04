package ee.telekom.workflow.core.error;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import ee.telekom.workflow.util.AbstractWorkflowEngineDao;
import ee.telekom.workflow.util.AdvancedParameterSource;

@Repository
public class ExecutionErrorDao extends AbstractWorkflowEngineDao{

    public void create( ExecutionError error ){
        long refNum = getNextSequenceValue( getSchema() + "exer_ref_num_s" );
        error.setRefNum( refNum );
        String sql = ""
                + "INSERT INTO " + getSchema() + "execution_errors "
                + "  (ref_num, woin_ref_num, woit_ref_num, error_text, error_details, date_created, created_by) "
                + " VALUES "
                + "  (:refNum, :woinRefNum, :woitRefNum, :errorText, :errorDetails, :dateCreated, :createdBy)";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addBean( error )
                .addValue( "dateCreated", new Date() )
                .addValue( "createdBy", getCreatedOrLastUpdatedBy() );
        getNamedParameterJdbcTemplate().update( sql, source );
    }

    public ExecutionError findByWoinRefNum( long woinRefNum ){
        String sql = "SELECT * FROM " + getSchema() + "execution_errors WHERE woin_ref_num = ?";
        List<ExecutionError> result = getJdbcTemplate().query( sql, ExecutionErrorRowMapper.INSTANCE, woinRefNum );
        return result.isEmpty() ? null : result.getFirst();
    }

    public void delete( long refNum ){
        String sql = "DELETE FROM " + getSchema() + "execution_errors WHERE ref_num = ?";
        Object[] args = {refNum};
        getJdbcTemplate().update( sql, args );
    }

}
