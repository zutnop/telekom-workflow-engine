package ee.telekom.workflow.util;

import java.lang.invoke.MethodHandles;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes a DEBUG log entry after every ee.telekom.workflow package @Service class public method call.
 * The row contains the elapsed milliseconds and an exception message if thrown.
 * 
 * @author Erko Hansar
 */
@Aspect
public class StatisticsLoggingAspect{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Pointcut("execution(public * ee.telekom.workflow..*(..))")
    public void publicWorkflowMethod(){
    }

    @Around("publicWorkflowMethod() && @within(org.springframework.stereotype.Service)")
    public Object aroundServiceMethod( ProceedingJoinPoint pjp ) throws Throwable{
        long start = System.currentTimeMillis();
        Throwable error = null;
        try{
            return pjp.proceed();
        }
        catch( Throwable t ){
            error = t;
            throw t;
        }
        finally{
            if( log.isDebugEnabled() ){
                long elapsed = System.currentTimeMillis() - start;
                log.debug( pjp.toShortString() + " took " + elapsed + " ms" + (error != null ? ", exception: " + error : "") );
            }
        }

    }

}
