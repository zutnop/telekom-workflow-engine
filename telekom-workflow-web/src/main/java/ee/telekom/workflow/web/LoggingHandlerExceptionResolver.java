package ee.telekom.workflow.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

public class LoggingHandlerExceptionResolver extends SimpleMappingExceptionResolver{

    private static final Logger log = LoggerFactory.getLogger( LoggingHandlerExceptionResolver.class );

    @Override
    protected ModelAndView doResolveException( HttpServletRequest request, HttpServletResponse response, Object handler, Exception e ){
        log.info( "An unhandled exception occured", e );
        return super.doResolveException( request, response, handler, e );
    }
}
