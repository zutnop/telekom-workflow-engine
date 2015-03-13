package ee.telekom.workflow.core.notification;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExceptionNotificationServiceImpl implements ExceptionNotificationService{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    private volatile Date nextPossibleNotification = new Date();
    private final AtomicInteger newExceptionsCount = new AtomicInteger( 0 );

    @Value("${workflowengine.exception.mailer.host}")
    private String mailServer;

    @Value("${workflowengine.exception.mail.from}")
    private String mailFrom;

    @Value("${workflowengine.exception.mail.recipients}")
    private String recipients;

    @Value("${workflowengine.exception.mail.notification.intervalMinutes}")
    private int notificationIntervalMinutes;

    @Value("${environment.level}")
    private String mailSubjectEnvironment;

    @Value("${workflowengine.exception.mail.notification.enabled}")
    private String notificationServiceEnabled;

    @PostConstruct
    public void init(){
        if( isServiceEnabled() ){
            log.info( "ExceptionNotification has been initilized using the smpt host {}, a notification interval of {} minute(s), and a recipients list of {}",
                    mailServer, notificationIntervalMinutes, recipients );
        }
    }

    public void handleException( Exception exception ){
        if( isServiceEnabled() ){
            Date now = new Date();
            newExceptionsCount.incrementAndGet();

            if( nextPossibleNotification.before( now ) ){
                int count = newExceptionsCount.getAndSet( 0 );

                String subject = count + " exception(s) occured in " + mailSubjectEnvironment + " Workflow Engine";
                String body = ""
                        + count + " exception(s) occured with ip " + getHostIpAddress() + ". \n"
                        + "\n"
                        + "The most recent exception occured at " + format( now ) + " with the following stacktrace:\n"
                        + "\n"
                        + ExceptionUtils.getFullStackTrace( exception );

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
            props.put( "mail.smtp.host", mailServer );
            Session session = Session.getDefaultInstance( props, null );

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

}
