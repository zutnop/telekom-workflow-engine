package ee.telekom.workflow.core.recovery;

import java.util.List;

/**
 * Service method for recovering from engine failures. The engine may fail
 * or leave data in an inconsistent state if it is not shutdown in a clean way
 * or if there are other hardware/network failures.
 *
 * Two kinds of problems may arise from such a failure scenario.
 * <ol>
 * <li>A workflow instance is locked and assigned to a node but the node has failed/is dead.
 * <li>A workflow instance is locked AND it is not in the queue AND it is not assigned to a node.
 * <ol>
 */
public interface RecoveryService{

    /**
     * Recovers all workflow instance's with the first above mentioned problem.
     * @param deadNodes the nodes that are dead/failed.
     */
    void recoverExecutionsAssignedToNodes( List<String> deadNodes );

    /**
     * Recovers all workflow instance's with the second above mentioned problem.
     * @param clusterName this cluster's name
     */
    void recoverExecutionsNotAssignedToNodes( String clusterName );

}
