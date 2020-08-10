package ee.telekom.workflow.executor.lifecycle;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.notification.ExceptionNotificationService;
import ee.telekom.workflow.util.ExecutorServiceUtil;
import ee.telekom.workflow.util.NamedPoolThreadFactory;
import ee.telekom.workflow.util.SimpleLifeCycleBean;

@Component
public class LifecycleJobImpl extends SimpleLifeCycleBean{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private LifecycleService lifecycleService;
    @Autowired
    private WorkflowEngineConfiguration config;
    @Autowired
    private ExceptionNotificationService exceptionNotificationService;

    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public void doStart(){
        lifecycleService.startUp();

        scheduledExecutorService = Executors.newScheduledThreadPool( 2, new NamedPoolThreadFactory( "lifecycle-manager" ) );

        scheduledExecutorService.scheduleWithFixedDelay(
                new LifecycleHeartBeatRunnable(),
                0, // do the first heart beat immediately, to run before health checks
                config.getHeartbeatInterval(),
                TimeUnit.SECONDS );

        scheduledExecutorService.scheduleWithFixedDelay(
                new LifecycleManagerRunnable(),
                config.getHeartbeatInterval(), // start health checks later
                config.getHeartbeatInterval(),
                TimeUnit.SECONDS );

        log.info( "Started lifecycle-manager" );
    }

    @Override
    public void doStop(){
        log.debug( "Stopping lifecycle-manager" );
        ExecutorServiceUtil.shutDownSynchronously( scheduledExecutorService );
        lifecycleService.shutDown();
        log.debug( "Stopped lifecycle-manager" );
    }

    private abstract class LifecycleBaseRunnable implements Runnable{
        @Override
        public void run(){
            try{
                try{
                    executeWork();
                }
                catch( Exception e ){
                    log.error( e.getMessage(), e );
                    exceptionNotificationService.handleException( e );
                }
            }
            catch( Exception e ){
                log.error( "{} failed to execute work, but we will try again after the configured time interval.", getClass().getSimpleName(), e );
            }
            catch( Throwable t ){
                log.error( "{} failed miserably to execute work, the scheduledExecutorService will break now!", getClass().getSimpleName(), t );
                throw t;
            }
        }

        protected abstract void executeWork();
    }

    private class LifecycleHeartBeatRunnable extends LifecycleBaseRunnable{
        @Override
        protected void executeWork(){
            lifecycleService.doHeartBeat();
        }
    }

    private class LifecycleManagerRunnable extends LifecycleBaseRunnable{
        @Override
        protected void executeWork(){
            lifecycleService.checkNodeStatus();
        }
    }

}
