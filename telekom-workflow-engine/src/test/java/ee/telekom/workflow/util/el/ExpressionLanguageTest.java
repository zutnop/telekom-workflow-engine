package ee.telekom.workflow.util.el;

import java.util.Date;

import javax.el.ELProcessor;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.graph.core.EnvironmentImpl;
import ee.telekom.workflow.graph.el.ElUtil;

/**
 * http://docs.oracle.com/javaee/7/api/javax/el/package-summary.html
 * 
 * @author Erko Hansar
 */
public class ExpressionLanguageTest{

    @Test
    public void testBean(){
        ELProcessor processor = ElUtil.initNewELProcessor( new EnvironmentImpl(), null );

        processor.defineBean( "client", new Client( 1, "Heli Kopter" ) );
        String name = (String)processor.eval( "client.name" );

        Assert.assertEquals( name, "Heli Kopter" );
    }

    @Test
    public void testConditions(){
        ELProcessor processor = ElUtil.initNewELProcessor( new EnvironmentImpl(), null );

        processor.defineBean( "client", new Client( 1, "Heli Kopter" ) );
        boolean result = (boolean)processor.eval( "not empty client.name and client.id > 0" );

        Assert.assertTrue( result );
    }

    @Test
    public void testValue(){
        ELProcessor processor = ElUtil.initNewELProcessor( new EnvironmentImpl(), null );

        processor.setValue( "throwException", false );
        boolean success = (boolean)processor.eval( "not throwException" );

        Assert.assertTrue( success );
    }

    @Test
    public void testEnvironmentBean(){
        EnvironmentImpl environment = new EnvironmentImpl();
        environment.setAttribute( "testAtr1", true );
        environment.setAttribute( "testAtr2", "33" );
        environment.setAttribute( "testAtr3", null );

        ELProcessor processor = ElUtil.initNewELProcessor( environment, null );
        // this way all the parameters must be prefixed with "env.", but we will at least be able to check unmapped attributes without an exception
        processor.defineBean( "env", environment.getAttributes() );

        boolean result = (boolean)processor.eval( "empty env.testAtr3 && env.testAtr1 && empty env.unknownAtr" );

        Assert.assertTrue( result );
    }

    @Test
    public void testEnvironmentResolver(){
        EnvironmentImpl environment = new EnvironmentImpl();
        environment.setAttribute( "testAtr1", true );
        environment.setAttribute( "testAtr2", "33" );
        environment.setAttribute( "testAtr3", null );

        ELProcessor processor = ElUtil.initNewELProcessor( environment, null );

        boolean result = (boolean)processor.eval( "empty testAtr3 && testAtr1" );

        // this again fails: "ELResolver cannot handle a null base Object with identifier 'unknownAtr'"
        //boolean result = (boolean)processor.eval( "empty testAtr3 && testAtr1 && empty unknownAtr" );

        Assert.assertTrue( result );
    }

    @Test
    public void testEnvironmentResolverNull(){
        EnvironmentImpl environment = new EnvironmentImpl();
        environment.setAttribute( "testDate1", null );

        ELProcessor processor = ElUtil.initNewELProcessor( environment, null );

        // in EL, null.fieldName does not throw a nullpointer, and null in conditions always returns false
        boolean result = (boolean)processor.eval( "testDate1.time < System.currentTimeMillis()" );
        Assert.assertFalse( result );
        result = (boolean)processor.eval( "testDate1.whatever > System.currentTimeMillis()" );
        Assert.assertFalse( result );
    }

    @Test
    public void testEnvironmentResolverDate(){
        Date start = new Date();

        EnvironmentImpl environment = new EnvironmentImpl();
        environment.setAttribute( "currentMillis", System.currentTimeMillis() );

        try{
            Thread.sleep( 30 );
        }
        catch( InterruptedException e ){
        }

        ELProcessor processor = ElUtil.initNewELProcessor( environment, null );

        boolean result = (boolean)processor.eval( "NOW.time > currentMillis" );
        Assert.assertTrue( result );

        Date now = (Date)processor.eval( "NOW" );
        Assert.assertTrue( now.after( start ) );
    }

    @Test
    public void testEnvironmentResolverInstanceIdNull(){
        EnvironmentImpl environment = new EnvironmentImpl();

        ELProcessor processor = ElUtil.initNewELProcessor( environment, null );

        Long instanceId = (Long)processor.eval( "WORKFLOW_INSTANCE_ID" );
        Assert.assertEquals( null, instanceId );
    }

    @Test
    public void testEnvironmentResolverInstanceId(){
        EnvironmentImpl environment = new EnvironmentImpl();

        ELProcessor processor = ElUtil.initNewELProcessor( environment, 55L );

        long instanceId = (long)processor.eval( "WORKFLOW_INSTANCE_ID" );
        Assert.assertEquals( 55, instanceId );
    }

    public class Client{
        private int id;
        private String name;

        public Client( int id, String name ){
            this.id = id;
            this.name = name;
        }

        public int getId(){
            return id;
        }

        public String getName(){
            return name;
        }

    }

}
