package ee.telekom.workflow.core.lock;

import java.lang.invoke.MethodHandles;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;

/**
 * Manages an exclusive distributed lock per cluster to avoid multiple master.
 */
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class LockServiceImpl implements LockService{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private LockDao dao;
    @Autowired
    private WorkflowEngineConfiguration config;

    @Override
    public boolean eagerAcquire(){
        releaseExpiredLock();
        return refreshOwnLock() || acquireLock();
    }

    @Override
    public boolean acquireLock(){
        String clusterName = config.getClusterName();
        String nodeName = config.getNodeName();
        Date expireTime = DateUtils.addSeconds( new Date(), config.getHeartbeatMaximumPauseSeconds() );
        boolean isAcquired = dao.create( clusterName, nodeName, expireTime );
        return isAcquired;
    }

    @Override
    public boolean isOwnLock(){
        String nodeName = config.getNodeName();
        return nodeName.equals( getLockOwner() );
    }

    @Override
    public boolean refreshOwnLock(){
        String clusterName = config.getClusterName();
        String nodeName = config.getNodeName();
        Date expireTime = DateUtils.addSeconds( new Date(), config.getHeartbeatMaximumPauseSeconds() );
        boolean isRefreshed = dao.updateExpireTime( clusterName, nodeName, expireTime );
        return isRefreshed;
    }

    @Override
    public boolean releaseOwnLock(){
        String clusterName = config.getClusterName();
        String nodeName = config.getNodeName();
        boolean isReleased = dao.deleteByOwner( clusterName, nodeName );
        if( isReleased ){
            log.info( "Released own lock" );
        }
        else{
            log.error( "Could not release own lock since lock was not owned" );
        }
        return isReleased;
    }

    @Override
    public boolean releaseExpiredLock(){
        String clusterName = config.getClusterName();
        Date now = new Date();
        boolean isReleased = dao.deleteByExpireTime( clusterName, now );
        if( isReleased ){
            log.warn( "Released exprired lock" );
        }
        return isReleased;
    }

    @Override
    public String getLockOwner(){
        return dao.findOwner( config.getClusterName() );
    }

    @Override
    public Date getLockExpireDate(){
        return dao.findExpireTime( config.getClusterName() );
    }

}
