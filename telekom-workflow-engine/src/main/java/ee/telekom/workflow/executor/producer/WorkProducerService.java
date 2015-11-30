package ee.telekom.workflow.executor.producer;

import java.util.List;

import ee.telekom.workflow.core.workunit.WorkUnit;

/**
 * Provides a service for the work unit producer.
 *
 * @author Christian Klock
 */
public interface WorkProducerService{

    /**
     * Locks and queues new work units for processing. Removes the queued work units (max number limited by the maxBatchSize value) from the input list.
     *
     * @param unprocessedWorkUnits a list of work units which require locking and queuing
     * @param maxBatchSize max number of work units to produce in one run
     * @throws InterruptedException, if interrupted while adding work units to the work unit queue.
     */
    void produceWork( List<WorkUnit> unprocessedWorkUnits, int maxBatchSize ) throws InterruptedException;

}
