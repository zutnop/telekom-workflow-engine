package ee.telekom.workflow.executor.consumer;

/**
 * Provides services for the workflow consumer.
 *
 * @author Christian Klock
 */
public interface WorkConsumerService{

    /**
     * Consumes a single work unit.
     * <p>
     * Blocks for 15 s or until a work unit becomes consumable, if there is currently none. 
     * 
     */
    void consumeWorkUnit();

    /**
     * Returns the total number of consumed work units by the engine instance at hand since
     * the engine's last deployment.
     */
    long getConsumedWorkUnits();

}
