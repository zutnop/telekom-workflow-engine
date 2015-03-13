package ee.telekom.workflow.example.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * Dummy implementation. Normally this service would connect to your CRM and/or billing database.
 * 
 * @author Erko Hansar
 */
@Service("customerService")
public class CustomerServiceImpl implements CustomerService{

    private static final Map<String, Customer> customers;
    static{
        customers = new HashMap<>();
        customers.put( "100001", new Customer( "100001", "John Doe", "ACTIVE", BigDecimal.valueOf( 10 ) ) );
        customers.put( "100002", new Customer( "100002", "Jane Roe", "ACTIVE", BigDecimal.valueOf( -7 ) ) );
        customers.put( "100003", new Customer( "100003", "Mary Major", "ACTIVE", BigDecimal.valueOf( -155 ) ) );
        customers.put( "100004", new Customer( "100004", "Richard Miles", "SUSPENDED", BigDecimal.valueOf( -10 ) ) );
    }

    @Override
    public String getCustomerName( String customerId ){
        fakeSlowServicePerformanceImpact( 200 );
        return customers.get( customerId ).getName();
    }

    @Override
    public String getCustomerStatus( String customerId ){
        fakeSlowServicePerformanceImpact( 200 );
        return customers.get( customerId ).getStatus();
    }

    @Override
    public String getAccountManager( String customerId ){
        fakeSlowServicePerformanceImpact( 200 );
        return "account-manager-id";
    }

    @Override
    public Map<String, Object> getCustomerVariableMap( String customerId ){
        fakeSlowServicePerformanceImpact( 200 );
        Map<String, Object> result = new HashMap<String, Object>();
        Customer customer = customers.get( customerId );
        result.put( "id", customer.getId() );
        result.put( "name", customer.getName() );
        result.put( "status", customer.getStatus() );
        result.put( "balance", customer.getBalance() );
        result.put( "type", "B2B" );
        return result;
    }

    @Override
    public BigDecimal getCustomerBalance( String customerId ){
        fakeSlowServicePerformanceImpact( 200 );
        return customers.get( customerId ).getBalance();
    }

    @Override
    public void createPaymentSchedule( String customerId, BigDecimal balance ){
        fakeSlowServicePerformanceImpact( 200 );
    }

    @Override
    public void debtCollected( String customerId ){
        fakeSlowServicePerformanceImpact( 200 );
    }

    @Override
    public void writeOffDebt( String customerId ){
        fakeSlowServicePerformanceImpact( 200 );
    }

    @Override
    public boolean sendDebtWarning( String customerId, BigDecimal customerBalance, String warningType ){
        fakeSlowServicePerformanceImpact( 2000 );
        return false;
    }

    @Override
    public Date getSuspendTimeAfterWarning(){
        Calendar calendar = Calendar.getInstance();
        calendar.add( Calendar.MINUTE, 3 );
        return calendar.getTime();
    }

    @Override
    public String suspendCustomer( String customerId ){
        fakeSlowServicePerformanceImpact( 200 );
        return "345678";
    }

    @Override
    public String getOrderStatus( String orderId ){
        fakeSlowServicePerformanceImpact( 200 );
        return "COMPLETED";
    }

    private void fakeSlowServicePerformanceImpact( long millis ){
        try{
            Thread.sleep( millis );
        }
        catch( InterruptedException e ){
            // do nothing
        }
    }

    public static class Customer implements Serializable{

        private static final long serialVersionUID = 1L;

        private String id;
        private String name;
        private String status;
        private BigDecimal balance;

        public Customer( String id, String name, String status, BigDecimal balance ){
            this.id = id;
            this.name = name;
            this.status = status;
            this.balance = balance;
        }

        public String getId(){
            return id;
        }

        public void setId( String id ){
            this.id = id;
        }

        public String getName(){
            return name;
        }

        public void setName( String name ){
            this.name = name;
        }

        public String getStatus(){
            return status;
        }

        public void setStatus( String status ){
            this.status = status;
        }

        public BigDecimal getBalance(){
            return balance;
        }

        public void setBalance( BigDecimal balance ){
            this.balance = balance;
        }

    }

}
