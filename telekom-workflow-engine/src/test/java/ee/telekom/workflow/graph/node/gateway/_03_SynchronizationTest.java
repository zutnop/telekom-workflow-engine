package ee.telekom.workflow.graph.node.gateway;

import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.GraphFactory;

public class _03_SynchronizationTest extends AbstractGraphTest{

    @Test
    public void one(){
        assertExecution( GraphFactory.INSTANCE.synchronzation_one(), "2" );
    }

    @Test
    public void two(){
        assertExecution( GraphFactory.INSTANCE.synchronzation_two(), "2,3" );
    }

    @Test
    public void three(){
        assertExecution( GraphFactory.INSTANCE.synchronzation_three(), "2,3,4" );
    }

    @Test
    public void two_firstBranchEmpty(){
        assertExecution( GraphFactory.INSTANCE.synchronzation_two_firstBranchEmpty(), "2" );
    }

    @Test
    public void two_secondBranchEmpty(){
        assertExecution( GraphFactory.INSTANCE.synchronzation_two_secondBranchEmpty(), "2" );
    }

    @Test
    public void two_bothBranchesEmpty(){
        assertExecution( GraphFactory.INSTANCE.synchronzation_two_bothBranchesEmpty(), null );
    }

    @Test
    public void two_pre_post(){
        assertExecution( GraphFactory.INSTANCE.synchronzation_two_pre_post(), "1,3,4,6" );
    }

    @Test
    public void firstBranchEmpty_pre_post(){
        assertExecution( GraphFactory.INSTANCE.synchronization_firstBranchEmpty_pre_post(), "1,3,6" );
    }

    @Test
    public void secondBranchEmpty_pre_post(){
        assertExecution( GraphFactory.INSTANCE.synchronization_secondBranchEmpty_pre_post(), "1,3,6" );
    }

}
