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
        scheduledExecutorService = Executors.newScheduledThreadPool( 1, new NamedPoolThreadFactory( "lifecycle-manager" ) );
        scheduledExecutorService.scheduleWithFixedDelay(
                new LifecycleManagerRunnable(),
                0,
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

    private class LifecycleManagerRunnable implements Runnable{
        @Override
        public void run(){
            try{
                try{
                    lifecycleService.checkNodeStatus();
                }
                catch( Exception e ){
                    log.error( e.getMessage(), e );
                    exceptionNotificationService.handleException( e );
                }
            }
            catch( Exception e ){
                log.error( "LifecycleManagerRunnable failed to check the node status, but we will try again after the configured time interval.", e );
            }
            catch( Throwable t ){
                log.error( "LifecycleManagerRunnable failed miserably to check the node status, the scheduledExecutorService will break now!", t );
                throw t;
            }
        }
    }

}
