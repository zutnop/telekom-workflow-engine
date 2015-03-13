package ee.telekom.workflow.core.workunit;

import java.sql.ResultSet;
import java.sql.SQLException;

import ee.telekom.workflow.util.AbstractRowMapper;

public class WorkUnitRowMapper extends AbstractRowMapper<WorkUnit>{

    public static final WorkUnitRowMapper INSTANCE = new WorkUnitRowMapper();

    @Override
    public WorkUnit mapRow( ResultSet rs, int rowNum ) throws SQLException{
        WorkUnit object = new WorkUnit();
        object.setWoinRefNum( getLong( rs, "woin_ref_num" ) );
        object.setType( getWorkType( rs, "type" ) );
        object.setWoitRefNum( getLong( rs, "woit_ref_num" ) );
        return object;
    }

    private WorkType getWorkType( ResultSet rs, String columnLabel ) throws SQLException{
        String value = rs.getString( columnLabel );
        return value == null ? null : WorkType.valueOf( value );
    }
}
