package ee.telekom.workflow.graph.core;

import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphEngineFacade;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphInstance;

public class AbortInstanceTest extends AbstractGraphTest{

    @Test
    public void abort_with_signals(){
        Graph graph = GraphFactory.INSTANCE.escalation_three();
        assertAbort( graph, 4, 3 );
    }

    @Test
    public void abort_with_timer(){
        Graph graph = GraphFactory.INSTANCE.timer_one_pre_post();
        assertAbort( graph, 1, 1 );
    }

    @Test
    public void abort_with_timer_parallel(){
        Graph graph = GraphFactory.INSTANCE.timer_parallel_pre_post();
        assertAbort( graph, 3, 2 );
    }

    @Test
    public void abort_with_task(){
        Graph graph = GraphFactory.INSTANCE.beanasynccall_one_pre_post();
        assertAbort( graph, 1, 1 );
    }

    @Test
    public void abort_with_human_task(){
        Graph graph = GraphFactory.INSTANCE.human_task_one_pre_post();
        assertAbort( graph, 1, 1 );
    }

    private void assertAbort( Graph graph, int activeTokens, int activeWorkItems ){
        GraphEngineFacade engine = new GraphEngineImpl();
        GraphInstance instance = engine.start( graph, null );
        assertActiveTokens( instance, activeTokens );
        assertActiveWorkItems( instance, activeWorkItems );

        engine.abort( instance );
        assertActiveWorkItems( instance, 0 );
        assertActiveTokens( instance, 0 );

    }

}
