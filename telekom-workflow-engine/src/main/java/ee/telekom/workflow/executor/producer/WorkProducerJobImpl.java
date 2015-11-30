package ee.telekom.workflow.executor.producer;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.lock.LockService;
import ee.telekom.workflow.core.notification.ExceptionNotificationService;
import ee.telekom.workflow.core.workunit.WorkUnit;
import ee.telekom.workflow.core.workunit.WorkUnitService;
import ee.telekom.workflow.util.ExecutorServiceUtil;
import ee.telekom.workflow.util.NamedPoolThreadFactory;

@Component
public class WorkProducerJobImpl implements WorkProducerJob{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    private static final int WORK_MAX_BATCH_SIZE = 1000;

    @Autowired
    private WorkProducerService workProducerService;
    @Autowired
    private WorkUnitService workUnitService;
    @Autowired
    private LockService lockService;
    @Autowired
    private WorkflowEngineConfiguration config;
    @Autowired
    private ExceptionNotificationService exceptionNotificationService;

    private volatile AtomicBoolean isStarted = new AtomicBoolean( false );
    private volatile AtomicBoolean isSuspended = new AtomicBoolean( false );
    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public synchronized void start(){
        scheduledExecutorService = Executors.newScheduledThreadPool( 1, new NamedPoolThreadFactory( "producer" ) );
        scheduledExecutorService.scheduleWithFixedDelay(
                new ProducerRunnable(),
                0,
                config.getProducerIntervalSeconds(),
                TimeUnit.SECONDS );
        isSuspended.set( false );
        isStarted.set( true );
        log.info( "Started producer" );
    }

    @Override
    public synchronized void stop(){
        log.debug( "Stopping producer" );
        ExecutorServiceUtil.shutDownSynchronously( scheduledExecutorService );
        isSuspended.set( false );
        isStarted.set( false );
        log.info( "Stopped producer" );
    }

    @Override
    public synchronized boolean isStarted(){
        return isStarted.get();
    }

    @Override
    public void suspend(){
        isSuspended.set( true );
    }

    @Override
    public void resume(){
        isSuspended.set( false );
    }

    @Override
    public boolean isSuspended(){
        return isSuspended.get();
    }

    private class ProducerRunnable implements Runnable{
        @Override
        public void run(){
            try{
                if( isSuspended.get() || !lockService.refreshOwnLock() ){
                    return;
                }
                try{
                    List<WorkUnit> unprocessedWorkUnits = workUnitService.findNewWorkUnits( new Date() );
                    do {
                        workProducerService.produceWork( unprocessedWorkUnits, WORK_MAX_BATCH_SIZE );
                    } while ( !unprocessedWorkUnits.isEmpty() );
                }
                catch( Exception e ){
                    log.error( e.getMessage(), e );
                    exceptionNotificationService.handleException( e );
                }
            }
            catch( Exception e ){
                log.error( "ProducerRunnable failed to produce work, but we will try again after the configured time interval.", e );
            }
            catch( Throwable t ){
                log.error( "ProducerRunnable failed miserably to produce work, the scheduledExecutorService will break now!", t );
                throw t;
            }
        }
    }

}
