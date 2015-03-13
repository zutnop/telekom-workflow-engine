package ee.telekom.workflow.core.error;

import java.sql.ResultSet;
import java.sql.SQLException;

import ee.telekom.workflow.util.AbstractRowMapper;

/**
 * Row mapper for {@link ExecutionError}s that maps all database fields except the 
 * ones that are not used by the internal parts of the engine (date_created, created_by).
 * 
 * @author Christian Klock
 */
public class ExecutionErrorRowMapper extends AbstractRowMapper<ExecutionError>{

    public static final ExecutionErrorRowMapper INSTANCE = new ExecutionErrorRowMapper();

    @Override
    public ExecutionError mapRow( ResultSet rs, int rowNum ) throws SQLException{
        ExecutionError object = new ExecutionError();
        object.setRefNum( getLong( rs, "ref_num" ) );
        object.setWoinRefNum( getLong( rs, "woin_ref_num" ) );
        object.setWoitRefNum( getLong( rs, "woit_ref_num" ) );
        object.setErrorText( getString( rs, "error_text" ) );
        object.setErrorDetails( getString( rs, "error_details" ) );
        return object;
    }

}
