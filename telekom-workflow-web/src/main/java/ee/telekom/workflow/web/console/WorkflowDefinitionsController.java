package ee.telekom.workflow.web.console;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import ee.telekom.workflow.facade.WorkflowEngineFacade;
import ee.telekom.workflow.facade.model.WorkflowInstanceFacadeStatus;

@Controller
@RequestMapping("/console")
public class WorkflowDefinitionsController{

    @Autowired
    private WorkflowEngineFacade facade;

    @RequestMapping("/workflow/definitions")
    public String viewWorkflowDefinitions( Model model ){
        model.addAttribute( "workflows", facade.getWorkflowStatistics() );
        model.addAttribute( "statuses", WorkflowInstanceFacadeStatus.values() );
        return "console/workflow/definitions";
    }

}
