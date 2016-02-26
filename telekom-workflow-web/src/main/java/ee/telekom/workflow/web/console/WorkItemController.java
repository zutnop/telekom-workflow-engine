package ee.telekom.workflow.web.console;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ee.telekom.workflow.core.common.UnexpectedStatusException;
import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.facade.WorkflowEngineFacade;
import ee.telekom.workflow.facade.model.WorkItemState;
import ee.telekom.workflow.util.JsonUtil;
import ee.telekom.workflow.web.console.form.ExecuteWorkItemForm;

/**
 * Controller for the workflow item details view.
 */
@Controller
@RequestMapping("/console")
public class WorkItemController{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private WorkflowEngineConfiguration configuration;
    @Autowired
    private WorkflowEngineFacade facade;

    @RequestMapping(method = RequestMethod.GET, value = "/workflow/instances/{woinRefNum}/item/{woitRefNum}")
    public String view( Model model,
                        @PathVariable long woinRefNum,
                        @PathVariable long woitRefNum,
                        @ModelAttribute("skipTimer") ExecuteWorkItemForm skipTimerForm ){
        WorkItemState workItem = facade.findWorkItem( woitRefNum, null );
        if( workItem == null || workItem.getWoinRefNum() != woinRefNum ){
            throw new IllegalArgumentException( "Cannot find work item" );
        }
        model.addAttribute( "workItem", workItem );
        return "console/workflow/item";
    }

    @PreAuthorize("hasRole('ROLE_TWE_ADMIN')")
    @RequestMapping(method = RequestMethod.POST, value = "/workflow/instances/{woinRefNum}/item/{woitRefNum}")
    public String handleAction( RedirectAttributes model,
                                @PathVariable long woinRefNum,
                                @PathVariable long woitRefNum,
                                @ModelAttribute("form") ExecuteWorkItemForm form, Errors result ){
        WorkItemState workItem = facade.findWorkItem( woitRefNum, null );
        if( workItem == null || workItem.getWoinRefNum() != woinRefNum ){
            throw new IllegalArgumentException( "Cannot find work item" );
        }
        // parsing form result field
        Object formResult = null;
        try{
            formResult = JsonUtil.deserialize( form.getResult() );
        }
        catch( Exception e ){
            log.info( e.getMessage(), e );
            result.rejectValue( "result", "workflow.item.error.result." + workItem.getType() );
        }
        // perform action
        if( !result.hasErrors() ){
            try{
                doAction( workItem, formResult );
            }
            catch( UnexpectedStatusException e ){
                log.info( e.getMessage(), e );
                model.addFlashAttribute( "errorMessage", "workflow.item.error.status" );
                return "redirect:" + configuration.getConsoleMappingPrefix() + "/console/workflow/instances/" + workItem.getWoinRefNum();
            }
            catch( Exception e ){
                log.warn( "Unable to perform action on work item " + woitRefNum, e );
                model.addFlashAttribute( "errorMessage", "workflow.item.error.action." + workItem.getType() );
                model.addFlashAttribute( "error", e.getMessage() );
            }
        }
        // redirect
        if( result.hasErrors() || model.getFlashAttributes().containsKey( "error" ) ){
            model.addFlashAttribute( "form", form );
            model.addFlashAttribute( "org.springframework.validation.BindingResult.form", result );
            return "redirect:" + configuration.getConsoleMappingPrefix() + "/console/workflow/instances/" + workItem.getWoinRefNum() + "/item/" + workItem.getRefNum();
        }
        else{
            model.addFlashAttribute( "successMessage", "workflow.item.success." + workItem.getType() );
            return "redirect:" + configuration.getConsoleMappingPrefix() + "/console/workflow/instances/" + workItem.getWoinRefNum();
        }
    }

    private void doAction( WorkItemState workItem, Object result ){
        switch( workItem.getType() ) {
            case SIGNAL:
                facade.sendSignalToWorkItem( workItem.getRefNum(), workItem.getSignal(), result );
                break;
            case TIMER:
                facade.skipTimer( workItem.getRefNum() );
                break;
            case TASK:
                facade.submitTask( workItem.getRefNum(), result );
                break;
            case HUMAN_TASK:
                facade.submitHumanTask( workItem.getRefNum(), result );
                break;
        }
    }

    @ModelAttribute("form")
    public ExecuteWorkItemForm createForm(){
        return new ExecuteWorkItemForm();
    }
}
