package ee.telekom.workflow.web.console.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.facade.model.WorkflowInstanceFacadeStatus;
import ee.telekom.workflow.facade.util.StatusUtil;

@Service
public class MessageHelper{

    @Autowired
    private MessageSource messageSource;

    public String getStatusText( WorkflowInstanceStatus status ){
        WorkflowInstanceFacadeStatus facadeStatus = StatusUtil.toFacade( status );
        String message = "workflowinstance.status.facadedetailed." + facadeStatus.name();
        Object[] args = {status};
        return messageSource.getMessage( message, args, LocaleContextHolder.getLocale() );
    }

    public String getHasActiveHumanTaskText( boolean hasActiveHumanTask ){
        String message = "workflow.instances.humantask." + hasActiveHumanTask;
        return messageSource.getMessage( message, null, LocaleContextHolder.getLocale() );
    }

    public String getVersionText( Integer version ){
        if( version != null ){
            return version.toString();
        }
        else{
            String message = "workflowinstance.workflowversion.latest";
            return messageSource.getMessage( message, null, LocaleContextHolder.getLocale() );
        }
    }
}
