package ee.telekom.workflow.web.console;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ee.telekom.workflow.core.common.UnexpectedStatusException;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.facade.WorkflowEngineFacade;
import ee.telekom.workflow.facade.model.WorkItemState;
import ee.telekom.workflow.facade.model.WorkflowInstanceState;
import ee.telekom.workflow.web.console.model.WorkItemStateModel;
import ee.telekom.workflow.web.console.model.WorkflowInstanceStateModel;

/**
 * Controller for the workflow instance details view.
 *  
 * @author Christian Klock
 */
@Controller
@RequestMapping("/console")
public class WorkflowInstanceDetailsController{

    @Autowired
    private WorkflowEngineFacade facade;

    @RequestMapping(method = RequestMethod.GET, value = "/workflow/instances/{woinRefNum}")
    public String viewInstance( @PathVariable long woinRefNum, Model model ){
        WorkflowInstanceState woin = facade.findWorkflowInstance( woinRefNum, null );
        model.addAttribute( "workflowInstance", WorkflowInstanceStateModel.create( woin ) );
        List<WorkItemState> workItems = facade.findWorkItems( woinRefNum, isActive( woin ) );
        model.addAttribute( "workItems", createModels( workItems ) );
        if( isActive( woin ) ){
            model.addAttribute( "executionError", facade.findExecutionError( woinRefNum ) );
        }
        return "console/workflow/instance";
    }

    private boolean isActive( WorkflowInstanceState woin ){
        return !WorkflowInstanceStatus.EXECUTED.name().equals( woin.getStatus() )
                && !WorkflowInstanceStatus.ABORTED.name().equals( woin.getStatus() );
    }

    /**
     * Aborts, suspends or resumes a workflow instance or retries the last failed operation
     * on the instance.<br>
     * Returns a redirect for a GET-after-POST.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.POST, value = "/workflow/instances/{woinRefNum}")
    public String performActionOnInstance( RedirectAttributes model, @PathVariable long woinRefNum, @RequestParam() String action ){
        if( "abort".equals( action ) || "suspend".equals( action ) || "resume".equals( action ) || "retry".equals( action ) ){
            try{
                if( "abort".equals( action ) ){
                    facade.abortWorkflowInstance( woinRefNum );
                    model.addFlashAttribute( "successMessage", "workflow.instance.action.abort.success" );
                }
                else if( "suspend".equals( action ) ){
                    facade.suspendWorkflowInstance( woinRefNum );
                    model.addFlashAttribute( "successMessage", "workflow.instance.action.suspend.success" );
                }
                else if( "resume".equals( action ) ){
                    facade.resumeWorkflowInstance( woinRefNum );
                    model.addFlashAttribute( "successMessage", "workflow.instance.action.resume.success" );
                }
                else if( "retry".equals( action ) ){
                    facade.retryWorkflowInstance( woinRefNum );
                    model.addFlashAttribute( "successMessage", "workflow.instance.action.retry.success" );
                }
            }
            catch( UnexpectedStatusException e ){
                model.addFlashAttribute( "errorMessage", "workflow.instance.action.error.unexpectedstatus" );
            }
        }
        else{
            model.addFlashAttribute( "unknownAction", action );
            model.addFlashAttribute( "errorMessage", "workflow.instance.action.error.unknownaction" );
        }
        return "redirect:/console/workflow/instances/" + woinRefNum;
    }

    private List<WorkItemStateModel> createModels( List<WorkItemState> woits ){
        List<WorkItemStateModel> result = new ArrayList<>();
        for( WorkItemState woit : woits ){
            result.add( WorkItemStateModel.create( woit ) );
        }
        return result;
    }

}
