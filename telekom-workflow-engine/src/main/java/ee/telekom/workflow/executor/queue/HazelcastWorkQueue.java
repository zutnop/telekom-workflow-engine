package ee.telekom.workflow.executor.queue;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.workunit.WorkUnit;
import ee.telekom.workflow.listener.WorkflowEngineHazelcastStartupListener;

@Component
public class HazelcastWorkQueue implements WorkQueue{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    private static final String WORK_QUEUE_NAME = "work";

    @Autowired
    private WorkflowEngineConfiguration config;
    private HazelcastInstance hcInstance;
    private AtomicBoolean isLocalHcInstance = new AtomicBoolean( false );
    private AtomicBoolean isStarted = new AtomicBoolean( false );
    private List<WorkflowEngineHazelcastStartupListener> listeners = new ArrayList<>();

    @Override
    public void registerHazelcastStartupListener( WorkflowEngineHazelcastStartupListener listener ){
        listeners.add( listener );
    }

    @Override
    public void start(){
        String hcInstanceName = config.getClusterHazelcastName();
        hcInstance = Hazelcast.getHazelcastInstanceByName( hcInstanceName );
        if (hcInstance == null) {
            log.info( "Didn't find an existing Hazelcast instance by name " + hcInstanceName + ". Starting our own instance!" );
            Config hcConfig = new Config();
            // Unfortunately, using the following line does not yet apply during the Hazelcast
            // initialization such that Hazelcast initalization log output ends up at stout.
            // This propblem is resolved by setting a system property as shown below.
            // hcConfig.setProperty( "hazelcast.logging.type", "slf4j" );
            System.setProperty( "hazelcast.logging.class", "com.hazelcast.logging.Slf4jFactory" );
            hcConfig.setProperty( "hazelcast.jmx", "true" );
            hcConfig.setProperty( "hazelcast.shutdownhook.enabled", "false" );
            hcConfig.setInstanceName( hcInstanceName );
            hcConfig.getGroupConfig().setName( config.getClusterName() );
            hcConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled( true );
            hcConfig.getNetworkConfig().getJoin().getMulticastConfig().setMulticastGroup( config.getClusterMulticastGroup() );
            hcConfig.getNetworkConfig().getJoin().getMulticastConfig().setMulticastPort( config.getClusterMulticastPort() );
            hcConfig.getNetworkConfig().getJoin().getMulticastConfig().setMulticastTimeToLive( config.getClusterMulticastTtl() );

            hcInstance = Hazelcast.newHazelcastInstance( hcConfig );
            isLocalHcInstance.set( true );
        } else {
            log.info( "Found an existing Hazelcast instance by name " + hcInstanceName + ". Using that." );
        }
        notifyListeners();
        isStarted.set( true );
        log.info( "Started queue" );
    }

    private void notifyListeners(){
        for( WorkflowEngineHazelcastStartupListener listener : listeners ){
            listener.onStarted();
        }
    }

    @Override
    public void stop(){
        log.debug( "Stopping queue" );
        if (isLocalHcInstance.get()) {
            hcInstance.getLifecycleService().shutdown();
        }
        isStarted.set( false );
        log.info( "Stopped queue" );
    }

    @Override
    public boolean isStarted(){
        return isStarted.get();
    }

    private IQueue<WorkUnit> getWorkQueue(){
        return hcInstance.getQueue( WORK_QUEUE_NAME );
    }

    @Override
    public void put( WorkUnit workUnit ) throws InterruptedException{
        getWorkQueue().put( workUnit );
    }

    @Override
    public WorkUnit poll( long timeout, TimeUnit unit ) throws InterruptedException{
        return getWorkQueue().poll( timeout, unit );
    }

    @Override
    public void awaitEmpty(){
        IsEmptyListener listener = new IsEmptyListener( getWorkQueue() );
        boolean includeItemValuesInNotificationEvents = false;
        String registrationId = getWorkQueue().addItemListener( listener, includeItemValuesInNotificationEvents );

        while( !getWorkQueue().isEmpty() ){
            try{
                listener.awaitEmpty( getWorkQueue() );
            }
            catch( InterruptedException ignore ){
            }
        }
        getWorkQueue().removeItemListener( registrationId );
    }

    private static class IsEmptyListener implements ItemListener<WorkUnit>{

        private final Object monitor = new Object();
        private final IQueue<WorkUnit> queue;

        public IsEmptyListener( IQueue<WorkUnit> queue ){
            this.queue = queue;
        }

        @Override
        public void itemAdded( ItemEvent<WorkUnit> event ){
        }

        @Override
        public void itemRemoved( ItemEvent<WorkUnit> e ){
            if( queue.isEmpty() ){
                synchronized( monitor ){
                    monitor.notifyAll();
                }
            }
        }

        public void awaitEmpty( IQueue<?> queue ) throws InterruptedException{
            synchronized( monitor ){
                if( !queue.isEmpty() ){
                    monitor.wait();
                }
            }
        }
    }

}
