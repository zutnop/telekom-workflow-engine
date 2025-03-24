package ee.telekom.workflow.core.notification;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PostConstruct;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExceptionNotificationServiceImpl implements ExceptionNotificationService{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    private volatile Date nextPossibleNotification = new Date();
    private final AtomicInteger newExceptionsCount = new AtomicInteger( 0 );

    @Value("${workflowengine.exception.mail.notification.enabled}")
    private String notificationServiceEnabled;

    @Value("${workflowengine.exception.mail.notification.intervalMinutes}")
    private int notificationIntervalMinutes;

    @Value("${workflowengine.exception.mail.environment}")
    private String mailEnvironment;

    @Value("${workflowengine.exception.mail.host}")
    private String smtpHost;

    @Value("${workflowengine.exception.mail.port}")
    private String smtpPort;

    @Value("${workflowengine.exception.mail.username}")
    private String smtpUsername;

    @Value("${workflowengine.exception.mail.password}")
    private String smtpPassword;

    @Value("${workflowengine.exception.mail.from}")
    private String mailFrom;

    @Value("${workflowengine.exception.mail.recipients}")
    private String recipients;

    @PostConstruct
    public void init(){
        if( isServiceEnabled() ){
            log.info( "ExceptionNotification has been initilized using the smpt host {}, a notification interval of {} minute(s), and a recipients list of {}",
                    smtpHost, notificationIntervalMinutes, recipients );
        }
    }

    public void handleException( Exception exception ){
        if( isServiceEnabled() ){
            Date now = new Date();
            newExceptionsCount.incrementAndGet();

            if( nextPossibleNotification.before( now ) ){
                int count = newExceptionsCount.getAndSet( 0 );

                String subject = count + " exception(s) occured in " + mailEnvironment + " Workflow Engine";
                String body = ""
                        + count + " exception(s) occured with ip " + getHostIpAddress() + ". \n"
                        + "\n"
                        + "The most recent exception occured at " + format( now ) + " with the following stacktrace:\n"
                        + "\n"
                        + ExceptionUtils.getStackTrace( exception );

                sendEmail( mailFrom, recipients, subject, body );

                nextPossibleNotification = DateUtils.addMinutes( now, notificationIntervalMinutes );
            }
        }
    }

    private boolean isServiceEnabled(){
        return "true".equalsIgnoreCase( notificationServiceEnabled );
    }

    private String getHostIpAddress(){
        try{
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch( UnknownHostException e ){
            return "";
        }
    }

    private void sendEmail( String from, String to, String subject, String body ){
        log.info( "Sending exception email from:{} to:{} subject:{}", from, to, subject );
        try{
            Properties props = new Properties();
            props.put( "mail.smtp.host", smtpHost );
            if (StringUtils.isNotBlank(smtpPort)) {
                props.put( "mail.smtp.port", smtpPort );
            }
            Authenticator authenticator = null;
            if (StringUtils.isNotBlank(smtpUsername)) {
                props.put( "mail.smtp.auth", true );
                authenticator = new SmtpAuthenticator();
            }
            Session session = Session.getDefaultInstance( props, authenticator );

            Message msg = new MimeMessage( session );
            msg.setFrom( new InternetAddress( from ) );

            InternetAddress[] addresses = InternetAddress.parse( to );
            msg.setRecipients( Message.RecipientType.TO, addresses );

            msg.setSubject( subject );
            msg.setSentDate( new Date() );
            msg.setText( body );
            Transport.send( msg );
        }
        catch( Exception e ){
            log.warn( "Sending email failed: ", e );
        }
    }

    private String format( Date date ){
        return new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" ).format( date );
    }

    private class SmtpAuthenticator extends Authenticator {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(smtpUsername, smtpPassword);
        }
    }

}
