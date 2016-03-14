package ee.telekom.workflow.executor.consumer;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.notification.ExceptionNotificationService;
import ee.telekom.workflow.util.ExecutorServiceUtil;
import ee.telekom.workflow.util.NamedPoolThreadFactory;

@Component
public class WorkConsumerJobImpl implements WorkConsumerJob{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private WorkConsumerService workConsumerService;
    @Autowired
    private WorkflowEngineConfiguration config;
    @Autowired
    private ExceptionNotificationService exceptionNotificationService;

    private ExecutorService executorService;
    private final AtomicBoolean isStopping = new AtomicBoolean();

    @Override
    public synchronized void start(){
        isStopping.set( false );

        // number of parallel consumer threads
        int numberOfConsumerThreads = config.getNumberOfConsumerThreads();

        // spring security context for executor threads
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("workflow-engine", "[not-used]", AuthorityUtils.createAuthorityList("ROLE_WORKFLOW_ENGINE")));

        // actual executor thread pool
        ExecutorService delegateExecutorService = Executors.newFixedThreadPool( numberOfConsumerThreads, new NamedPoolThreadFactory( "consumer" ) );
        // wrapper executor service that sets the security context for each thread
        executorService = new DelegatingSecurityContextExecutorService(delegateExecutorService, securityContext);

        // start the consuming jobs
        for( int i = 0; i < numberOfConsumerThreads; i++ ){
            executorService.execute( new ConsumerRunnable() );
        }
        log.info( "Scheduled {} consumers", numberOfConsumerThreads );
    }

    @Override
    public synchronized void stop(){
        log.debug( "Stopping consumers" );
        isStopping.set( true );
        ExecutorServiceUtil.shutDownSynchronously( executorService );
        log.info( "Stopped all consumers" );
    }

    private class ConsumerRunnable implements Runnable{
        @Override
        public void run(){
            try{
                log.info( "Started consumer on thread {}", Thread.currentThread().getName() );
                while( !isStopping.get() ){
                    try{
                        workConsumerService.consumeWorkUnit();
                    }
                    catch( Exception e ){
                        log.error( "ConsumerRunnable failed to consume work, but we will try again after 10 seconds.", e );
                        exceptionNotificationService.handleException( e );
                        try{
                            Thread.sleep( 1000L * 10 );
                        }
                        catch( InterruptedException ie ){
                            // do nothing
                        }
                    }
                }
                log.info( "Stopped consumer on thread {}", Thread.currentThread().getName() );
            }
            catch( Throwable t ){
                log.error( "ConsumerRunnable failed miserably to consume work, the fixed executor thread will die now!", t );
                throw t;
            }
        }
    }

}
