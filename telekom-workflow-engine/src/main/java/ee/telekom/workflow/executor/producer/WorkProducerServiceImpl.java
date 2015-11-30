package ee.telekom.workflow.executor.producer;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.core.workunit.WorkUnit;
import ee.telekom.workflow.core.workunit.WorkUnitService;
import ee.telekom.workflow.executor.queue.WorkQueue;

@Service
@Transactional
public class WorkProducerServiceImpl implements WorkProducerService{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private WorkUnitService workUnitService;
    @Autowired
    private WorkQueue queue;

    @Override
    public void produceWork( List<WorkUnit> unprocessedWorkUnits, int maxBatchSize ) throws InterruptedException{
        List<WorkUnit> batchOfWorkUnits = unprocessedWorkUnits.subList(0, Math.min(maxBatchSize, unprocessedWorkUnits.size()));
        if( batchOfWorkUnits.size() > 0 ){
            workUnitService.lock( batchOfWorkUnits );
            for( WorkUnit wu : batchOfWorkUnits ){
                log.info( "Adding '{}' to queue", wu );
                queue.put( SerializationUtils.clone( wu ) );
            }
            batchOfWorkUnits.clear();
        }
    }

}
