package ee.telekom.workflow.core.workitem;

import java.sql.ResultSet;
import java.sql.SQLException;

import ee.telekom.workflow.api.AutoRecovery;
import ee.telekom.workflow.util.AbstractRowMapper;

/**
 * Row mapper for {@link WorkItem}s that maps all database fields except the 
 * ones that are not used by the internal parts of the engine (date_created, created_by,
 * date_last_updated, last_updated_by).
 * 
 * @author Christian Klock
 */
public class WorkItemRowMapper extends AbstractRowMapper<WorkItem>{

    public static final WorkItemRowMapper INSTANCE = new WorkItemRowMapper();

    @Override
    public WorkItem mapRow( ResultSet rs, int rowNum ) throws SQLException{
        WorkItem object = new WorkItem();
        object.setRefNum( getLong( rs, "ref_num" ) );
        object.setWoinRefNum( getLong( rs, "woin_ref_num" ) );
        object.setTokenId( getInteger( rs, "token_id" ) );
        object.setStatus( getWorkItemStatus( rs, "status" ) );
        object.setSignal( getString( rs, "signal" ) );
        object.setDueDate( getDate( rs, "due_date" ) );
        object.setBean( getString( rs, "bean" ) );
        object.setMethod( getString( rs, "method" ) );
        object.setRole( getString( rs, "role" ) );
        object.setUserName( getString( rs, "user_name" ) );
        object.setArguments( getString( rs, "arguments" ) );
        object.setResult( getString( rs, "result" ) );
        object.setAutoRecovery( AutoRecovery.of(rs.getBoolean("auto_recovery")) );
        return object;
    }

}
