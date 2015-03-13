package liquibase.ext.logging;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.logging.core.AbstractLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogger extends AbstractLogger{
    private static final int PRIORITY = 5;

    private Logger logger;
    private String changeLogName = null;
    private String changeSetName = null;

    @Override
    public void setName( String name ){
        this.logger = LoggerFactory.getLogger( name );
    }

    @Override
    public void setLogLevel( String logLevel, String logFile ){
        // Do nothing
    }

    @Override
    public void setChangeLog( DatabaseChangeLog databaseChangeLog ){
        changeLogName = (databaseChangeLog == null) ? null : databaseChangeLog.getFilePath();
    }

    @Override
    public void setChangeSet( ChangeSet changeSet ){
        changeSetName = (changeSet == null) ? null : changeSet.toString( false );
    }

    @Override
    public void severe( String message ){
        if( this.logger.isErrorEnabled() ){
            this.logger.error( buildMessage( message ) );
        }
    }

    @Override
    public void severe( String message, Throwable throwable ){
        if( this.logger.isErrorEnabled() ){
            this.logger.error( buildMessage( message ), throwable );
        }
    }

    @Override
    public void warning( String message ){
        if( this.logger.isWarnEnabled() ){
            this.logger.warn( buildMessage( message ) );
        }
    }

    @Override
    public void warning( String message, Throwable throwable ){
        if( this.logger.isWarnEnabled() ){
            this.logger.warn( buildMessage( message ), throwable );
        }
    }

    @Override
    public void info( String message ){
        if( this.logger.isInfoEnabled() ){
            this.logger.info( buildMessage( message ) );
        }
    }

    @Override
    public void info( String message, Throwable throwable ){
        if( this.logger.isInfoEnabled() ){
            this.logger.info( buildMessage( message ), throwable );
        }
    }

    @Override
    public void debug( String message ){
        if( this.logger.isDebugEnabled() ){
            this.logger.debug( buildMessage( message ) );
        }
    }

    @Override
    public void debug( String message, Throwable throwable ){
        if( this.logger.isDebugEnabled() ){
            this.logger.debug( buildMessage( message ), throwable );
        }
    }

    /**
     * Gets the logger priority for this logger. The priority is used by Liquibase to determine which logger to use.
     * The logger with the highest priority will be used. This implementation's priority is set to 5. Remove loggers
     * with higher priority numbers if needed.
     *
     * @return An integer (5)
     */
    @Override
    public int getPriority(){
        return PRIORITY;
    }

    /**
     * Build a log message with optional data if it exists.
     */
    private String buildMessage( String rawMessage ){
        StringBuilder msg = new StringBuilder();
        if( changeLogName != null ){
            msg.append( changeLogName ).append( ": " );
        }
        if( changeSetName != null ){
            msg.append( changeSetName.replace( changeLogName + "::", "" ) ).append( ": " );
        }
        msg.append( rawMessage );
        return msg.toString();
    }
}