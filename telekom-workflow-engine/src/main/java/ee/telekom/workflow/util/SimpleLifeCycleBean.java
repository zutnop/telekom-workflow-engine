package ee.telekom.workflow.util;

import org.springframework.context.SmartLifecycle;

public abstract class SimpleLifeCycleBean implements SmartLifecycle{

    private static final int DEFAULT_PHASE = 1;

    private final int phase = DEFAULT_PHASE;
    private boolean running = false;

    public abstract void doStart();

    public abstract void doStop();

    @Override
    public final void start(){
        doStart();
        running = true;
    }

    @Override
    public final void stop(){
        doStop();
        running = false;
    }

    @Override
    public boolean isRunning(){
        return running;
    }

    @Override
    public int getPhase(){
        return phase;
    }

    @Override
    public boolean isAutoStartup(){
        return true;
    }

    @Override
    public void stop( Runnable callback ){
        stop();
        callback.run();
    }

}
