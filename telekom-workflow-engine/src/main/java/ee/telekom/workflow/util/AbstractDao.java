package ee.telekom.workflow.util;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class AbstractDao{

    private NamedParameterJdbcDaoSupport daoSupport;

    public void setDataSource( DataSource dataSource ){
        daoSupport = new NamedParameterJdbcDaoSupport();
        daoSupport.setDataSource( dataSource );
    }

    public DataSource getDataSource(){
        return daoSupport.getDataSource();
    }

    protected JdbcTemplate getJdbcTemplate(){
        return daoSupport.getJdbcTemplate();
    }

    protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(){
        return daoSupport.getNamedParameterJdbcTemplate();
    }

}
