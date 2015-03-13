package ee.telekom.workflow.example.definition;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ee.telekom.workflow.test.AbstractWorkflowApiTest;

/**
 * Example for how to write automated tests for workflow definitions.
 *
 * @author Raido Türk
 */ 
@RunWith(MockitoJUnitRunner.class)
public class ExampleStep00Test extends AbstractWorkflowApiTest{

    Example_00_StepSelection step0 = new Example_00_StepSelection();

    @Mock
    ExampleStepSelector service;

    @Test
    public void should_go_to_step_1(){
        doReturn( service ).when( beanResolver ).getBean( "exampleStepSelector" );
        when( service.findNextStep( anyString(), anyString() ) ).thenReturn( Example_01_SendWarning.class.getCanonicalName() );
        addGraphAndStartInstance( step0, createCustomerInput() );

        assertActiveWorkItemsCount( 1 );
        terminateTimer();
        assertInstanceCompleted();
        assertEnvironmentContainsValues( Collections.<String, Object>singletonMap( "nextStep", Example_01_SendWarning.class.getCanonicalName() ) );
        verifyNewInstanceCreation( Example_01_SendWarning.class.getCanonicalName() );
    }

    private HashMap<String, Object> createCustomerInput(){
        HashMap<String, Object> map = new HashMap<>();
        map.put( "customerId", "100001" );
        return map;
    }

}
