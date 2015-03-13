package ee.telekom.workflow.example.definition;

import org.springframework.stereotype.Component;

import ee.telekom.workflow.api.WorkflowDefinition;
import ee.telekom.workflow.api.WorkflowFactory;

/**
 * An example workflow definition via DSL.
 * 
 * This workflow is divided into sub-workflows called "steps". This is the first action step that warns the customer 
 * that we will take actions to collect the debt and then finds the next appropriate step based on the customer status.
 * 
 * @author Erko Hansar
 */
@Component
public class Example_01_SendWarning implements WorkflowDefinition{

    @Override
    public String getName(){
        return this.getClass().getCanonicalName();
    }

    @Override
    public int getVersion(){
        return 2;
    }

    @Override
    public void configureWorkflowDefinition( WorkflowFactory factory ){
        /* @formatter:off */
        factory
            .start()
            
            // validate input
            .validateInputVariable( 0, "customerId", String.class)
            
            // load data into environment
            .variable( "failedToContact" ).value( 1, false )
            .variable( "customerName" ).call( 2, "customerService", "getCustomerName", "${customerId}" )
            .variable( "customerBalance" ).call( 3, "customerService", "getCustomerBalance", "${customerId}" )
            
            // only continue with the proceedings if the customer balance is negative
            .if_( 4, "customerBalance < 0" )
                // loop until the message sending succeeds
                .doWhile()
                    .if_( 6, "customerBalance >= -3")
                        // if small debt, then auto send the message 
                        .variable( "failedToContact" ).callAsync( 13, "customerService", "sendDebtWarning", "${customerId}", "${customerBalance}", "SMALL_DEBT" )
                    .elseIf( "customerBalance >= -15" )
                        // if medium debt, then auto send the message
                        .variable( "failedToContact" ).callAsync( 7, "customerService", "sendDebtWarning", "${customerId}", "${customerBalance}", "MEDIUM_DEBT" )
                    .else_()
                        // for large debt, create a manual task to contact the customer personally
                        .variable( "accountManager" ).call( 8, "customerService", "getAccountManager", "${customerId}" )
                        .variable( "failedToContact" ).humanTask( 9, "ROLE_ACCOUNT_MANAGER", "${accountManager}" ).withAttribute( "customerId", "${customerId}" ).withAttribute( "taskType", "LARGE_DEBT" ).done()
                    .endIf()                    
                .doWhile( 5, "failedToContact" )
                
                // find out the next step 
                .variable( "nextStep" ).call( 10, "exampleStepSelector", "findNextStep", "${customerId}", "01" )
                //  and start it, passing along the customerId attribute
                .createInstance( 11, "${nextStep}", null, "${customerId}", null ).withAttribute( "customerId", "${customerId}" ).done()
            .endIf()

            .end();
        /* @formatter:on */
    }

}
