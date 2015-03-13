package ee.telekom.workflow.example.definition;

import org.springframework.stereotype.Component;

import ee.telekom.workflow.api.WorkflowDefinition;
import ee.telekom.workflow.api.WorkflowFactory;

/**
 * An example workflow definition via DSL.
 * 
 * This workflow is divided into sub-workflows called "steps". This is the initial step that waits a given time before starting 
 * the execution and then find the next appropriate step based on the customer status.
 * 
 * @author Erko Hansar
 */
@Component
public class Example_00_StepSelection implements WorkflowDefinition{

    @Override
    public String getName(){
        return this.getClass().getCanonicalName();
    }

    @Override
    public int getVersion(){
        return 1;
    }

    @Override
    public void configureWorkflowDefinition( WorkflowFactory factory ){
        /* @formatter:off */
        factory
            .start()

            // define customerId as a required input variable
            .validateInputVariable( 0, "customerId", String.class )
            // define waitTime as an optional input variable with a default value
            .validateInputVariable( 1, "waitTime", Long.class, false, 1000L * 30 )
            // wait for the waitTime duration
            .waitTimer( 2, "${waitTime}" )
            
            // find out the next step 
            .variable( "nextStep" ).call( 3, "exampleStepSelector", "findNextStep", "${customerId}", "00" )
            //  and start it, passing along the customerId attribute
            .createInstance( 4, "${nextStep}", null, "${customerId}", null ).withAttribute( "customerId", "${customerId}" ).done()
            
            .end();
        /* @formatter:on */
    }

}
