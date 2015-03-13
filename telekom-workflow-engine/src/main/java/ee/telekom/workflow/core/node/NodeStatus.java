package ee.telekom.workflow.core.node;

/**
 * Nodes regularly check their status and start/stop working based on the status
 * stored in a database. This enables one node to start/stop other nodes.
 */
public enum NodeStatus{

    /**
     * The node should start working when it next time checks its status.
     */
    ENABLE,

    /**
     * The node has started working.
     * A node in this status is considered dead, if it does not regularly update its heartbeat.
     */
    ENABLED,

    /**
     * The node should stop working when it next time checks its status.
     */
    DISABLE,

    /**
     * The node has stopped working.
     */
    DISABLED,

    /**
     * A node is marked considered failed/dead if it ENABLED but does not regularly update its heartbeat.
     * The cluster's master regularly check's whether other nodes to this end.
     */
    FAILED

}
