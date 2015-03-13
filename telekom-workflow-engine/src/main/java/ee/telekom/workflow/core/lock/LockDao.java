package ee.telekom.workflow.core.lock;

import java.util.Date;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import ee.telekom.workflow.util.AbstractWorkflowEngineDao;
import ee.telekom.workflow.util.AdvancedParameterSource;

@Repository
public class LockDao extends AbstractWorkflowEngineDao{

    public boolean create( String clusterName, String owner, Date expireTime ){
        String sql = "INSERT INTO locks (cluster_name, owner, expire_time) VALUES (?, ?, ?)";
        Object[] args = {clusterName, owner, expireTime};
        try{
            int count = getJdbcTemplate().update( sql, args );
            return count == 1;
        }
        catch( DuplicateKeyException e ){
            // Cluster_name is the primary key. There may not be two locks for the same cluster.
            return false;
        }
    }

    public boolean deleteByOwner( String clusterName, String owner ){
        String sql = "DELETE FROM locks WHERE cluster_name = ? AND owner = ?";
        Object[] args = {clusterName, owner};
        int count = getJdbcTemplate().update( sql, args );
        boolean isDeleted = (count == 1);
        return isDeleted;
    }

    public boolean deleteByExpireTime( String clusterName, Date expireTime ){
        String sql = "DELETE FROM locks WHERE cluster_name = ? AND expire_time < ?";
        Object[] args = {clusterName, expireTime};
        int count = getJdbcTemplate().update( sql, args );
        boolean isDeleted = (count == 1);
        return isDeleted;
    }

    public boolean updateExpireTime( String clusterName, String nodeName, Date expireTime ){
        String sql = ""
                + "UPDATE locks "
                + "   SET expire_time = :expireTime "
                + " WHERE cluster_name = :clusterName "
                + "   AND owner = :nodeName";
        AdvancedParameterSource source = new AdvancedParameterSource()
                .addValue( "expireTime", expireTime )
                .addValue( "clusterName", clusterName )
                .addValue( "nodeName", nodeName );
        int count = getNamedParameterJdbcTemplate().update( sql, source );
        boolean isUpdated = (count == 1);
        return isUpdated;
    }

    public String findOwner( String clusterName ){
        String sql = "SELECT owner FROM locks WHERE cluster_name = ?";
        Object[] args = {clusterName};
        List<String> result = getJdbcTemplate().queryForList( sql, String.class, args );
        return result.isEmpty() ? null : result.get( 0 );
    }

    public Date findExpireTime( String clusterName ){
        String sql = "SELECT expire_time FROM locks WHERE cluster_name = ?";
        Object[] args = {clusterName};
        List<Date> result = getJdbcTemplate().queryForList( sql, Date.class, args );
        return result.isEmpty() ? null : result.get( 0 );
    }
}
