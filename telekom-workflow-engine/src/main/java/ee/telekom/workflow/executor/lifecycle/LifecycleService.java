package ee.telekom.workflow.executor.lifecycle;

/**
 * The engines central life cycle manager that implements a start-up and shut-down order
 * for the different engine's components (considering the interdependencies).
 */
public interface LifecycleService{

    /**
     * Called at deployment. Starts the engine's components if required and possible.
     */
    void startUp();

    /**
     * Called at undeployment. Stops the engine's components if required.
     */
    void shutDown();

    /**
     * Called regularly to  
     * - leave a heart beat and mark dead cluster siblings as FAILED.
     * - heal workflow instance's if there is a FAILED node. 
     * - test whether the engine's is still master or not anymore and to start/stop components accordingly if required.
     */
    void checkNodeStatus();

    /**
     * Returns whether the engine is started.
     */
    boolean isStarted();

}
