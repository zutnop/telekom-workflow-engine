package ee.telekom.workflow.example.listener;

import java.lang.invoke.MethodHandles;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.telekom.workflow.facade.WorkflowEngineFacade;
import ee.telekom.workflow.facade.model.WorkItemState;
import ee.telekom.workflow.listener.HumanTaskEvent;
import ee.telekom.workflow.listener.HumanTaskEventListener;

/**
 * If enabled, this listener auto-completes all example workflow human tasks. 
 * Useful for performance testing, when we don't want to wait for human input.
 * 
 * @author Erko Hansar
 */
@Component
public class HumanTaskAutoCompleter implements HumanTaskEventListener{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    // toggle this flag to enable the feature for performance testing
    private static final boolean AUTO_COMPLETE_ENABLED = false;

    @Autowired
    private WorkflowEngineFacade facade;

    private final Queue<Pair<Long, Integer>> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isStopped = new AtomicBoolean( true );
    private Thread thread = null;

    @Override
    public void onCreated( HumanTaskEvent event ){
        if( AUTO_COMPLETE_ENABLED ){
            log.info( "Adding human task to auto complete queue: " + event.getWoinRefNum() + ", " + event.getTokenId() );
            queue.add( Pair.of( event.getWoinRefNum(), event.getTokenId() ) );
        }
    }

    @Override
    public void onCompleted( HumanTaskEvent event, Object humanTaskResult ){
        // do nothing
    }

    @Override
    public void onCancelled( HumanTaskEvent event ){
        // do nothing
    }

    ///// PRIVATE METHODS /////

    @PostConstruct
    private void initCompleter() throws Exception{
        if( AUTO_COMPLETE_ENABLED ){
            log.debug( "Starting up HumanTaskAutoCompleter" );
            isStopped.set( false );
            thread = new Thread( new HTAutoCompleter() );
            thread.start();
        }
    }

    @PreDestroy
    private void destroyCompleter() throws Exception{
        if( AUTO_COMPLETE_ENABLED ){
            log.debug( "Shutting down HumanTaskAutoCompleter" );
            isStopped.set( true );
            thread.join();
        }
    }

    private class HTAutoCompleter implements Runnable{

        @Override
        public void run(){
            log.info( "Started HTAutoCompleter on thread {}", Thread.currentThread().getName() );
            mainLoop : while( true ){
                // loop until queue has an item
                Pair<Long, Integer> item = null;
                while( item == null ){
                    if( isStopped.get() ){
                        break mainLoop;
                    }
                    item = queue.poll();
                    sleepIfNull( item );
                }

                // loop until matching WorkItem is found
                WorkItemState woit = null;
                while( woit == null ){
                    if( isStopped.get() ){
                        break mainLoop;
                    }
                    woit = facade.findActiveWorkItemByTokenId( item.getLeft(), item.getRight() );
                    sleepIfNull( woit );
                }

                log.info( "Auto-completing human task: " + woit.getRefNum() );
                facade.submitHumanTask( woit.getRefNum(), false );
            }
            log.info( "Stopped HTAutoCompleter on thread {}", Thread.currentThread().getName() );
        }

        private void sleepIfNull( Object object ){
            if( object == null ){
                try{
                    Thread.sleep( 1000 );
                }
                catch( InterruptedException e ){
                    log.warn( "HTAutoCompleter error", e );
                }
            }
        }

    }

}
