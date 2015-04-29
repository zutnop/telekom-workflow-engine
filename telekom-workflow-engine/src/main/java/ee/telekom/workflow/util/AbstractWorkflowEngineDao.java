package ee.telekom.workflow.util;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;

public class AbstractWorkflowEngineDao extends AbstractDao{

    @Autowired
    private WorkflowEngineConfiguration config;

    @Override
    @Autowired
    public void setDataSource( @Qualifier("workflowengineDataSource") DataSource dataSource ){
        super.setDataSource( dataSource );
    }

    public String getCreatedOrLastUpdatedBy(){
        return config.getNodeName();
    }

    protected String getClusterName(){
        return config.getClusterName();
    }

    protected long getNextSequenceValue( String sequence ){
        return getJdbcTemplate().queryForLong( "SELECT nextval('" + sequence + "')" );
    }

    protected String getSchema() {
        return config.getSchema();
    }

}
