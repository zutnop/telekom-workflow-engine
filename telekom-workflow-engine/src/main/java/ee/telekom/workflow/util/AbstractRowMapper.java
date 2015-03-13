package ee.telekom.workflow.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import ee.telekom.workflow.core.node.NodeStatus;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.graph.WorkItemStatus;

public abstract class AbstractRowMapper<T> implements RowMapper<T>{

    protected String getString( ResultSet rs, String columnLabel ) throws SQLException{
        return rs.getString( columnLabel );
    }

    protected Integer getInteger( ResultSet rs, String columnLabel ) throws SQLException{
        Integer value = rs.getInt( columnLabel );
        return rs.wasNull() ? null : value;
    }

    protected Long getLong( ResultSet rs, String columnLabel ) throws SQLException{
        Long value = rs.getLong( columnLabel );
        return rs.wasNull() ? null : value;
    }

    protected Date getDate( ResultSet rs, String columnLabel ) throws SQLException{
        return rs.getTimestamp( columnLabel );
    }

    protected Boolean getBoolean( ResultSet rs, String columnLabel ) throws SQLException{
        String value = rs.getString( columnLabel );
        return YesNoUtil.asBoolean( value );
    }

    protected WorkflowInstanceStatus getWorkflowInstanceStatus( ResultSet rs, String columnLabel ) throws SQLException{
        String value = rs.getString( columnLabel );
        return value == null ? null : WorkflowInstanceStatus.valueOf( value );
    }

    protected WorkItemStatus getWorkItemStatus( ResultSet rs, String columnLabel ) throws SQLException{
        String value = rs.getString( columnLabel );
        return value == null ? null : WorkItemStatus.valueOf( value );
    }

    protected NodeStatus getNodeStatus( ResultSet rs, String columnLabel ) throws SQLException{
        String value = rs.getString( columnLabel );
        return value == null ? null : NodeStatus.valueOf( value );
    }
}
