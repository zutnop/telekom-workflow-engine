package ee.telekom.workflow.executor.producer;

import ee.telekom.workflow.executor.consumer.WorkConsumerJob;

/**
 * Provides the life cycle methods of the engines work unit producer part.
 * <p> 
 * See also {@link WorkConsumerJob}.
 * 
 * @author Christian Klock
 */
public interface WorkProducerJob{

    /**
     * Starts the producer. 
     * <p>
     * Make sure you call {@link #stop()} for a clean shutdown if you called this method.
     * Once stopped, this method can be used to restart the producer.
     */
    void start();

    /**
     * Stops the producer. Is a no-op, if the producer is already stopped.
     */
    void stop();

    /**
     * Returns whether the producer is currently started.
     */
    boolean isStarted();

    /**
     * Suspends the producer without shutting it down. Resume the producer in order to
     * let it continue to produce work units.
     */
    void suspend();

    /**
     * Resumes the producer. Is a no-op for a not suspended producer.
     */
    void resume();

    /**
     * Returns whether the producer is currently suspended.
     */
    boolean isSuspended();

}
