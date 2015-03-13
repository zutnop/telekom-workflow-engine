package ee.telekom.workflow.executor.plugin;

import java.util.concurrent.atomic.AtomicInteger;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphRepository;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.WorkflowException;
import ee.telekom.workflow.graph.core.GraphRepositoryImpl;

// Manually wired up in test application context
public class MockPlugin implements WorkflowEnginePlugin{

    public static final String EXCEPTION_MESSAGE = "script exception";
    private static boolean throwException = false;
    private boolean isStarted = false;

    private GraphRepository repository = new GraphRepositoryImpl();
    private AtomicInteger htCreated = new AtomicInteger();
    private AtomicInteger htCompleted = new AtomicInteger();
    private AtomicInteger htCancelled = new AtomicInteger();
    private AtomicInteger htCancelledWithoutGraph = new AtomicInteger();

    @Override
    public void start(){
        isStarted = true;
    }

    @Override
    public void stop(){
        isStarted = false;
    }

    @Override
    public boolean isStarted(){
        return isStarted;
    }

    @Override
    public Object getBean( String name ){
        if( throwException ){
            throw new WorkflowException( EXCEPTION_MESSAGE );
        }
        return new GraphFactory.TestBean();
    }

    public static void startThrowingException(){
        throwException = true;
    }

    public static void stopThrowingException(){
        throwException = false;
    }

    @Override
    public GraphRepository getGraphRepository(){
        return repository;
    }

    public void clearRepository(){
        repository = new GraphRepositoryImpl();
    }

    @Override
    public void reloadWorkflowDefinitions(){
        // do nothing
    }

    @Override
    public void onWorkflowInstanceCreated( GraphInstance instance ){
        // do nothing
    }

    @Override
    public void onWorkflowInstanceCompleted( GraphInstance instance ){
        // do nothing
    }

    @Override
    public void onWorkflowInstanceAborted( GraphInstance instance ){
        // do nothing
    }

    @Override
    public void onHumanTaskCreated( GraphWorkItem workItem ){
        htCreated.incrementAndGet();
    }

    @Override
    public void onHumanTaskCompleted( GraphWorkItem workItem ){
        htCompleted.incrementAndGet();
    }

    @Override
    public void onHumanTaskCancelled( GraphWorkItem workItem ){
        htCancelled.incrementAndGet();
    }

    @Override
    public void onHumanTaskCancelled( WorkflowInstance workflowInstance, WorkItem workItem ){
        htCancelledWithoutGraph.incrementAndGet();
    }

    public int getHtCreated(){
        return htCreated.get();
    }

    public int getHtCompleted(){
        return htCompleted.get();
    }

    public int getHtCancelled(){
        return htCancelled.get();
    }

    public int getHtCancelledWithoutGraph(){
        return htCancelledWithoutGraph.get();
    }

    public void resetCounters(){
        htCreated.set( 0 );
        htCompleted.set( 0 );
        htCancelled.set( 0 );
        htCancelledWithoutGraph.set( 0 );
    }
}
