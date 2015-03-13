package ee.telekom.workflow.facade.workitem;

import java.sql.ResultSet;
import java.sql.SQLException;

import ee.telekom.workflow.facade.model.WorkItemState;
import ee.telekom.workflow.util.AbstractRowMapper;

public class WorkItemStateRowMapper extends AbstractRowMapper<WorkItemState>{

    public static final WorkItemStateRowMapper INSTANCE = new WorkItemStateRowMapper();

    @Override
    public WorkItemState mapRow( ResultSet rs, int rowNum ) throws SQLException{
        WorkItemState object = new WorkItemState();
        object.setRefNum( getLong( rs, "ref_num" ) );
        object.setWoinRefNum( getLong( rs, "woin_ref_num" ) );
        object.setTokenId( getInteger( rs, "token_id" ) );
        object.setStatus( getString( rs, "status" ) );
        object.setSignal( getString( rs, "signal" ) );
        object.setDueDate( getDate( rs, "due_date" ) );
        object.setBean( getString( rs, "bean" ) );
        object.setMethod( getString( rs, "method" ) );
        object.setRole( getString( rs, "role" ) );
        object.setUserName( getString( rs, "user_name" ) );
        object.setArguments( getString( rs, "arguments" ) );
        object.setResult( getString( rs, "result" ) );
        object.setDateCreated( getDate( rs, "date_created" ) );
        object.setDateUpdated( getDate( rs, "date_updated" ) );
        return object;
    }

}
