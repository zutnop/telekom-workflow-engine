package ee.telekom.workflow.executor.producer;

/**
 * Provides a service for the work unit producer.
 * 
 * @author Christian Klock
 */
public interface WorkProducerService{

    /**
     * Produces and locks new work units.
     * 
     * @throws InterruptedException, if interrupted while adding work units to the work unit queue.
     */
    void produceWork() throws InterruptedException;

}
