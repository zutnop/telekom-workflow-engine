package ee.telekom.workflow.example.definition;

import org.springframework.stereotype.Component;

import ee.telekom.workflow.api.WorkflowDefinition;
import ee.telekom.workflow.api.WorkflowFactory;

/**
 * An example workflow definition via DSL.
 * 
 * This workflow is divided into sub-workflows called "steps". This is the third and the final step that creates a 
 * payment schedule for the customer. It then monitors the progress of the schedule.
 * 
 * @author Erko Hansar
 */
@Component
public class Example_03_CreateSchedule implements WorkflowDefinition{

    @Override
    public String getName(){
        return this.getClass().getCanonicalName();
    }

    @Override
    public int getVersion(){
        return 3;
    }

    @Override
    public void configureWorkflowDefinition( WorkflowFactory factory ){
        /* @formatter:off */
        factory
            .start()
            
            // validate input
            .validateInputVariable( 0, "customerId", String.class)
            
            // load data into environment
            .variables( "customerName=name", "customerBalance=balance", "customerType=type" ).call( 1, "customerService", "getCustomerVariableMap", "${customerId}" )

            // only continue with the proceedings if the customer balance is still negative
            .if_( 2, "customerBalance < 0" )
                // create the payment schedule
                .callAsync( 3, "customerService", "createPaymentSchedule", "${customerId}", "${customerBalance}" )
                
                .split( 4 )
                    .branch()
                        // listen for the balance changes until the debt has been paid off
                        .doWhile()
                            .waitSignal( 5, "PAYMENT" )                
                            .variable( "customerBalance" ).call( 6, "customerService", "getCustomerBalance", "${customerId}" )
                        .doWhile( 7, "customerBalance < 0" )
                        .escalate( 8 )
                        .callAsync( 9, "customerService", "debtCollected", "${customerId}" )
                    .branch( "customerType == 'B2B'" )
                        // for business customers, listen for the bankruptcy and write off the debt
                        .waitSignal( 10, "BANKRUPTCY" )
                        .escalate( 11 )
                        .callAsync( 12, "customerService", "writeOffDebt", "${customerId}" )
                .joinAll()
            .endIf()
            
            .end();
        /* @formatter:on */
    }

}
