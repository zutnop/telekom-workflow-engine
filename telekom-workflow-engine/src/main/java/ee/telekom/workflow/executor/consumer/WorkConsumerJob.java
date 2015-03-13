package ee.telekom.workflow.executor.consumer;

import ee.telekom.workflow.executor.producer.WorkProducerJob;

/**
 * Provides the life cycle methods of the engines work unit consumer part.
 * <p> 
 * See also {@link WorkProducerJob}.
 * 
 * @author Christian Klock
 */
public interface WorkConsumerJob{

    /**
     * Starts the consumers. 
     * <p>
     * Make sure you call {@link #stop()} for a clean shutdown if you called this method.
     * Once stopped, this method can be used to restart the consumers.
     */
    void start();

    /**
     * Stops the consumers. Is a no-op, if the consumers are already stopped.
     */
    void stop();

}
