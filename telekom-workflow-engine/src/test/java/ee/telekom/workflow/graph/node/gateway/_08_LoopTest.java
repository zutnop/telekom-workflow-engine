package ee.telekom.workflow.graph.node.gateway;

import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.GraphFactory;

public class _08_LoopTest extends AbstractGraphTest{

    @Test
    public void oneNode(){
        assertLoopExecution( GraphFactory.INSTANCE.loop_one( 1 ), 1, "1" );
        assertLoopExecution( GraphFactory.INSTANCE.loop_one( 2 ), 2, "1,1" );
        assertLoopExecution( GraphFactory.INSTANCE.loop_one( 3 ), 3, "1,1,1" );
    }

    @Test
    public void twoNode(){
        assertLoopExecution( GraphFactory.INSTANCE.loop_two( 1 ), 1, "1,2" );
        assertLoopExecution( GraphFactory.INSTANCE.loop_two( 2 ), 2, "1,2,1,2" );
        assertLoopExecution( GraphFactory.INSTANCE.loop_two( 3 ), 3, "1,2,1,2,1,2" );
    }

    @Test
    public void andForkJoin_twoThreads_before_after(){
        assertLoopExecution(
                GraphFactory.INSTANCE
                        .loop_andForkJoin_twoThreads_before_after( 1 ),
                1, "1,3,4,6" );
        assertLoopExecution(
                GraphFactory.INSTANCE
                        .loop_andForkJoin_twoThreads_before_after( 2 ),
                2, "1,3,4,6,1,3,4,6" );
        assertLoopExecution(
                GraphFactory.INSTANCE
                        .loop_andForkJoin_twoThreads_before_after( 3 ),
                3, "1,3,4,6,1,3,4,6,1,3,4,6" );
    }

    @Test
    public void discriminatorJoin_twice(){
        assertLoopExecution(
                GraphFactory.INSTANCE.loop_cancelling_discriminator_twice( 1 ),
                1, "1,3,6,8,11" );
        assertLoopExecution(
                GraphFactory.INSTANCE.loop_cancelling_discriminator_twice( 2 ),
                2, "1,3,6,8,11,1,3,6,8,11" );
    }

    @Test
    public void loop_one_special(){
        assertLoopExecution( GraphFactory.INSTANCE.loop_one_special( 2 ), 2, "1,1,3" );
    }

    @Test
    public void while_loop(){
        assertLoopExecution( GraphFactory.INSTANCE.loop_while( 3 ), 3, "1,3,3,3,4" );
    }
}
