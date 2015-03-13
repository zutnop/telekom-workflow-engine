package ee.telekom.workflow.web.util;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Request logging filter that writes a log entry after the request is completed. It also sets up MDC parameters for request durations.
 *
 * @author Erko Hansar
 */
public class RequestLoggingFilter extends OncePerRequestFilter{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
    private static final String MDC_KEY = "requestmdc";
    private boolean includeQueryString = false;

    public boolean isIncludeQueryString(){
        return includeQueryString;
    }

    public void setIncludeQueryString( boolean includeQueryString ){
        this.includeQueryString = includeQueryString;
    }

    @Override
    protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain filterChain ) throws ServletException, IOException{
        long start = System.currentTimeMillis();
        MDC.put( MDC_KEY, getMappedDiagnosticContextMessage( request ) );

        try{
            filterChain.doFilter( request, response );
        }
        finally{
            if( log.isDebugEnabled() ){
                log.info( createMessage( request, start ) );
            }
            MDC.remove( MDC_KEY );
        }
    }

    private String createMessage( HttpServletRequest request, long start ){
        StringBuilder msg = new StringBuilder();
        msg.append( request.getRequestURI() );
        if( isIncludeQueryString() ){
            msg.append( '?' ).append( request.getQueryString() );
        }
        msg.append( " took " );
        msg.append( System.currentTimeMillis() - start );
        msg.append( "ms" );
        return msg.toString();
    }

    private String getMappedDiagnosticContextMessage( HttpServletRequest request ){
        String user = request.getRemoteUser();
        if( user != null ){
            return (new StringBuilder()).append( "[" ).append( user ).append( "]" ).toString();
        }
        return "[anonymous]";
    }
}
