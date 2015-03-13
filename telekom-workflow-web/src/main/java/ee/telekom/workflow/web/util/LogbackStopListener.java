package ee.telekom.workflow.web.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * Servlet context destory hook: stop logback to properly release resources.
 * 
 * @author Erko Hansar
 * @see http://logback.qos.ch/manual/configuration.html#stopContext
 */
public class LogbackStopListener implements ServletContextListener{

    @Override
    public void contextInitialized( ServletContextEvent sce ){
        // nothing to do here
    }

    @Override
    public void contextDestroyed( ServletContextEvent sce ){
        // assume SLF4J is bound to logback-classic in the current environment
        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }

}
