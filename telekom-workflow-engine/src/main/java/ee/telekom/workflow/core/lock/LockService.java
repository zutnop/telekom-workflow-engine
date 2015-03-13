package ee.telekom.workflow.core.lock;

import java.util.Date;

/**
 * Provides a lock service to manage a cluster wide shared lock.
 * <p>
 * All operations are implemented atomically such that they are safe for concurrent usage
 * from different JVM's.
 * <p>
 * The service is intended as a mean to determine the cluster's master node.
 */
public interface LockService{

    /**
     * Releases expired locks and afterwards attempts to refresh 
     * its own lock or acquire the lock.
     * 
     * @return true, if lock is owned. Otherwise false.
     */
    boolean eagerAcquire();

    /**
     * Attempts to acquire the cluster's lock
     * @return true, if lock was acquired. Otherwise false.
     */
    boolean acquireLock();

    /**
     * @return true, if lock is owned by this node. Otherwise false.
     */
    boolean isOwnLock();

    /**
     * Attempts to refresh the owned lock. Refreshing the lock means
     * to postpone its expiration date.
     * @return true, if lock was refreshed. Otherwise false.
     */
    boolean refreshOwnLock();

    /**
     * Releases the owned lock.
     * @return true, if lock was released. Otherwise false.
     */
    boolean releaseOwnLock();

    /**
     * Attempt to release the cluster's lock if it has expired.
     * @return true, if the lock was expired and is now released.
     */
    boolean releaseExpiredLock();

    /**
     * Returns the cluster's lock owner.
     * @return the cluster's lock owner.
     */
    String getLockOwner();

    /**
     * Returns the locks expire date or <code>null</code> if no cluster is not locked.
     * @return the locks expire date or <code>null</code> if no cluster is not locked.
     */
    Date getLockExpireDate();

}