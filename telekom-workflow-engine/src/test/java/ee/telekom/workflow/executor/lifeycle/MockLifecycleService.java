package ee.telekom.workflow.executor.lifeycle;

import java.util.concurrent.atomic.AtomicBoolean;

import ee.telekom.workflow.executor.lifecycle.LifecycleService;

//Manually wired up in test application context
public class MockLifecycleService implements LifecycleService{

    private AtomicBoolean isStarted = new AtomicBoolean( false );

    @Override
    public void startUp(){
        isStarted.set( true );
    }

    @Override
    public void shutDown(){
        isStarted.set( false );
    }

    @Override
    public void checkNodeStatus(){
        // Do nothing
    }

    @Override
    public boolean isStarted(){
        return isStarted.get();
    }

}
