package ee.telekom.workflow.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceUtil{

    public static void shutDownSynchronously( ExecutorService executorService ){
        if( executorService != null ){
            executorService.shutdown();
            while( !executorService.isTerminated() ){
                try{
                    executorService.awaitTermination( 1, TimeUnit.MINUTES );
                }
                catch( InterruptedException e ){
                    // Do not return on interrupts, but wait until all submitted tasks are completed.
                }
            }
        }
    }
}
