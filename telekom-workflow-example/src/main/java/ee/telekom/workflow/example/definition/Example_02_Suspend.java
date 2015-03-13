package ee.telekom.workflow.example.definition;

import org.springframework.stereotype.Component;

import ee.telekom.workflow.api.WorkflowDefinition;
import ee.telekom.workflow.api.WorkflowFactory;

/**
 * An example workflow definition via DSL.
 * 
 * This workflow is divided into sub-workflows called "steps". This is the second step that suspends the customer subscription  
 * and then finds the next appropriate step based on the customer status.
 * 
 * @author Erko Hansar
 */
@Component
public class Example_02_Suspend implements WorkflowDefinition{

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
            
            // validate input
            .validateInputVariable( 0, "customerId", String.class)
            
            // load data into environment
            .variable( "customerBalance" ).call( 1, "customerService", "getCustomerBalance", "${customerId}" )
            // calculate the suspend time after the warning message has been sent
            .variable( "suspendTime" ).call( 2, "customerService", "getSuspendTimeAfterWarning" )

            .whileDo( 3, "customerBalance < 0 && suspendTime.time > System.currentTimeMillis()" )
                .split( 4 )
                    .branch()
                        // wait until suspendTime
                        .waitUntilDate( 6, "${suspendTime}" )
                    .branch()
                        // and at the same time monitor balance changes
                        .waitSignal( 7, "PAYMENT" )
                .joinFirst()
                .variable( "customerBalance" ).call( 8, "customerService", "getCustomerBalance", "${customerId}" )
            .whileDo()

            // only continue with the proceedings if the customer balance is still negative
            .if_( 9, "customerBalance < 0" )
                // create a suspend order
                .variable("suspendOrderId").callAsync( 10, "customerService", "suspendCustomer", "${customerId}" )
                
                // wait until the order is beeing processed
                .doWhile()
                    .waitTimer( 11, "1000" )
                    .variable( "suspendOrderStatus" ).call( 12, "customerService", "getOrderStatus", "${suspendOrderId}" )
                .doWhile( 13, "suspendOrderStatus == 'PROCESSING'" )
                
                // if the order fails, create a manual task
                .if_( 14, "suspendOrderStatus != 'COMPLETED'" )
                    .humanTask( 15, "ROLE_CUSTOMER_SUPPORT", null ).withAttribute( "customerId", "${customerId}" ).withAttribute( "taskType", "MANUAL_SUSPEND" ).done()
                .endIf()
            
                // find out the next step 
                .variable( "nextStep" ).call( 16, "exampleStepSelector", "findNextStep", "${customerId}", "02" )
                //  and start it, passing along the customerId attribute
                .createInstance( 17, "${nextStep}", null, "${customerId}", null ).withAttribute( "customerId", "${customerId}" ).done()
            .endIf()

            .end();
        /* @formatter:on */
    }

}
