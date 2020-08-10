package ee.telekom.workflow.executor.producer;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    private volatile AtomicInteger errorSkipCountdown = new AtomicInteger( 0 );
    private volatile AtomicInteger errorSkipMultiplier = new AtomicInteger( 1 );
    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public synchronized void start(){
        scheduledExecutorService = Executors.newScheduledThreadPool( 1, new NamedPoolThreadFactory( "producer" ) );
        scheduledExecutorService.scheduleWithFixedDelay(
                new ProducerRunnable(),
                config.getProducerIntervalSeconds(),
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
        log.info( "Suspended producer" );
    }

    @Override
    public void resume(){
        isSuspended.set( false );
        log.info( "Resumed producer" );
    }

    @Override
    public boolean isSuspended(){
        return isSuspended.get();
    }

    private class ProducerRunnable implements Runnable{
        @Override
        public void run(){
            try{
                if( errorSkipCountdown.getAndUpdate( value -> (value > 0) ? (value - 1) : 0 ) > 0 ){
                    // skip runs until countdown has reached 0
                    return;
                }
                if( isSuspended.get() || !lockService.refreshOwnLock() ){
                    return;
                }
                try{
                    List<WorkUnit> unprocessedWorkUnits = workUnitService.findNewWorkUnits( new Date() );
                    do {
                        workProducerService.produceWork( unprocessedWorkUnits, WORK_MAX_BATCH_SIZE );
                    } while ( !unprocessedWorkUnits.isEmpty() );
                    // work was successful, reset the skip multiplier
                    errorSkipMultiplier.set( 1 );
                }
                catch( Exception e ){
                    exceptionNotificationService.handleException( e );
                    throw e;
                }
            }
            catch( Exception e ){
                // progressively increase the skip countdown so that after every failed retry, the waiting time would be longer before the next retry
                int currentSkipMultiplier = errorSkipMultiplier.getAndUpdate( value -> (value < 600) ? (value * 3) : value );
                errorSkipCountdown.set( currentSkipMultiplier );
                log.error( "ProducerRunnable failed to produce work, but we will try again after " + currentSkipMultiplier + " x configured time interval.", e );
            }
            catch( Throwable t ){
                log.error( "ProducerRunnable failed miserably to produce work, the scheduledExecutorService will break now!", t );
                throw t;
            }
        }
    }

}
