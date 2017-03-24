package ee.telekom.workflow.executor.queue;

import java.util.concurrent.TimeUnit;

import ee.telekom.workflow.core.workunit.WorkUnit;

/**
 * Provides a fifo queue for work units. It is part of the producer-consumer pattern for work units.
 *  
 * @author Christian Klock
 */
public interface WorkQueue{

    void start();

    void stop();

    boolean isStarted();

    void put( WorkUnit workUnit ) throws InterruptedException;

    WorkUnit poll( long timeout, TimeUnit unit ) throws InterruptedException;

    /**
     * Blocks the calling thread until the queue is empty.
     */
    void awaitEmpty();

}
