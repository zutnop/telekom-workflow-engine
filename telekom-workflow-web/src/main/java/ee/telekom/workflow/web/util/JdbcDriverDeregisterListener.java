package ee.telekom.workflow.web.util;

import java.lang.invoke.MethodHandles;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet context destory hook: de-register JDBC drivers to properly release resources.
 * 
 * @author Christian Klock
 * @author Erko Hansar
 * @see http://stackoverflow.com/a/23912257/2767952
 */
public class JdbcDriverDeregisterListener implements ServletContextListener{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Override
    public void contextInitialized( ServletContextEvent sce ){
        // nothing to do here
    }

    @Override
    public void contextDestroyed( ServletContextEvent sce ){
        ClassLoader thisClassLoader = this.getClass().getClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while( drivers.hasMoreElements() ){
            Driver driver = drivers.nextElement();
            // only process drivers loaded by this web application 
            if( driver.getClass().getClassLoader() == thisClassLoader ){
                try{
                    log.info( "Deregistering JDBC driver {}", driver );
                    DriverManager.deregisterDriver( driver );
                }
                catch( SQLException ex ){
                    log.error( "Error deregistering JDBC driver {}", driver, ex );
                }
            }
        }
    }

}
