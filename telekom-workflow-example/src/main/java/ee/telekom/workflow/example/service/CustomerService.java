package ee.telekom.workflow.example.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * An example business logic service that will allow your workflows to integrate with your backend systems. Normally 
 * the contained methods would be located in multiple different services (like billing service, configuration service, 
 * messaging service, order service etc.) but they are all located in this single service to keep things simple for 
 * the demo app.
 * 
 * @author Erko Hansar
 */
public interface CustomerService{

    ///// CUSTOMER SERVICES /////

    String getCustomerName( String customerId );

    String getCustomerStatus( String customerId );

    String getAccountManager( String customerId );

    Map<String, Object> getCustomerVariableMap( String customerId );

    ///// BILLING SERVICES /////

    BigDecimal getCustomerBalance( String customerId );

    void createPaymentSchedule( String customerId, BigDecimal balance );

    void debtCollected( String customerId );

    void writeOffDebt( String customerId );

    ///// MESSAGING SERVICES /////

    boolean sendDebtWarning( String customerId, BigDecimal customerBalance, String warningType );

    ///// CONFIGURATION SERVICES /////

    Date getSuspendTimeAfterWarning();

    ///// ORDER SERVICES /////

    String suspendCustomer( String customerId );

    String getOrderStatus( String orderId );

}
