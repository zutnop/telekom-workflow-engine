package ee.telekom.workflow.executor.consumer;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.core.workunit.WorkUnit;
import ee.telekom.workflow.executor.WorkflowExecutor;
import ee.telekom.workflow.executor.queue.WorkQueue;

@Component
public class WorkConsumerServiceImpl implements WorkConsumerService{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private WorkQueue queue;
    @Autowired
    private WorkflowExecutor executor;
    @Autowired
    private WorkflowEngineConfiguration config;

    private AtomicLong consumedWorkUnits = new AtomicLong();

    @Override
    public void consumeWorkUnit(){
        WorkUnit workUnit = null;
        try{
            workUnit = queue.poll( 15, TimeUnit.SECONDS );
        }
        catch( InterruptedException e ){
        }
        if( workUnit != null ){
            try{
                log.info( "Retrieved '{}' from queue.", workUnit );
                MDC.put( "workunit", workUnit.toString() );
                workflowInstanceService.updateNodeName( workUnit.getWoinRefNum(), config.getNodeName() );
                execute( workUnit );
                consumedWorkUnits.incrementAndGet();
            }
            finally{
                MDC.clear();
            }
        }
    }

    @Override
    public long getConsumedWorkUnits(){
        return consumedWorkUnits.get();
    }

    private void execute( WorkUnit wu ){
        switch( wu.getType() ) {
            case START_WORKFLOW:
                executor.startWorkflow( wu.getWoinRefNum() );
                break;
            case ABORT_WORKFLOW:
                executor.abortWorkflow( wu.getWoinRefNum() );
                break;
            case EXECUTE_TASK:
                executor.executeTask( wu.getWoinRefNum(), wu.getWoitRefNum() );
                break;
            case COMPLETE_WORK_ITEM:
                executor.completeWorkItem( wu.getWoinRefNum(), wu.getWoitRefNum() );
                break;
        }
    }

}
