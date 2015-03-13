package ee.telekom.workflow.core.node;

import java.util.List;

/**
 * Interface providing services for handling cluster nodes.
 */
public interface NodeService{

    Node findOrCreateByName( String name );

    List<Node> findAllClusterNodes();

    List<String> findFailedNodes();

    void updateHeartbeat( String name );

    void markEnable( long refNum );

    void markEnable( List<String> nodes );

    void markEnabled( long refNum );

    void markDisable( long refNum );

    void markDisabled( long refNum );

    void markFailed( long refNum );

    void markDeadNodesFailed();

    void doHeartBeat();

    boolean isAlive( String nodeName );

}
