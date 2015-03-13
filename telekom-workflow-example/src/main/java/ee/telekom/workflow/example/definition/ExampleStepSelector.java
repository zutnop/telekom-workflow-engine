package ee.telekom.workflow.example.definition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.telekom.workflow.example.service.CustomerService;

@Component
public class ExampleStepSelector{

    @Autowired
    private CustomerService customerService;

    public String findNextStep( String customerId, String currentStepId ){
        if( "00".equals( currentStepId ) ){
            String customerStatus = customerService.getCustomerStatus( customerId );
            if( "ACTIVE".equalsIgnoreCase( customerStatus ) ){
                return Example_01_SendWarning.class.getCanonicalName();
            }
            else{
                return Example_03_CreateSchedule.class.getCanonicalName();
            }
        }
        else if( "01".equals( currentStepId ) ){
            return Example_02_Suspend.class.getCanonicalName();
        }
        else if( "02".equals( currentStepId ) ){
            return Example_03_CreateSchedule.class.getCanonicalName();
        }
        throw new IllegalArgumentException( "No matching next step!" );
    }

}
