package ee.telekom.workflow.graph.core;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphValidator;

public class GraphValidatorTest{

    @Test
    public void test_no_errors(){
        assertValidation( GraphFactory.INSTANCE.sequence_one(), 0 );
        assertValidation( GraphFactory.INSTANCE.sequence_two(), 0 );
        assertValidation( GraphFactory.INSTANCE.exclusivechoice_two(), 0 );
        assertValidation( GraphFactory.INSTANCE.cancelling_discriminator_and_nested(), 0 );
        assertValidation( GraphFactory.INSTANCE.loop_cancelling_discriminator_twice( 1 ), 0 );
    }

    @Test
    public void test_with_errors(){
        assertValidation( GraphFactory.INSTANCE.error_no_start_node(), 1 );
        assertValidation( GraphFactory.INSTANCE.error_exclusivechoice_two(), 1 );
        assertValidation( GraphFactory.INSTANCE.error_cancelling_discriminator_two_pre_post(), 1 );
        assertValidation( GraphFactory.INSTANCE.error_reserved_variable_name(), 2 );
    }

    private void assertValidation( Graph graph, int errorCount ){
        GraphValidator validator = new GraphValidatorImpl();
        Assert.assertEquals( errorCount, validator.validate( graph ).size() );
    }

}
