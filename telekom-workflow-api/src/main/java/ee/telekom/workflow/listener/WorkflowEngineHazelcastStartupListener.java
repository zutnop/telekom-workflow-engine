package ee.telekom.workflow.listener;

/**
 * Provides a listener that is notified when workflow engine Hazelcast instance has been started.
 *
 * @author Raido Türk
 */
public interface WorkflowEngineHazelcastStartupListener{

    /**
     * Called after workflow engine Hazelcast instance has been started.
     */
    public void onStarted();

}
