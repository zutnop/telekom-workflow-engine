package ee.telekom.workflow.web.console;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.facade.WorkflowEngineFacade;
import ee.telekom.workflow.facade.model.CreateWorkflowInstance;
import ee.telekom.workflow.util.JsonUtil;
import ee.telekom.workflow.web.console.form.BatchCreateWorkflowInstancesForm;
import ee.telekom.workflow.web.console.form.CreateWorkflowInstanceForm;

/**
 * Web controller for starting one or multiple workflow instances based on the user input data.
 */
@Controller
@RequestMapping("/console")
public class WorkflowInstanceCreateController{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private WorkflowEngineConfiguration configuration;
    @Autowired
    private WorkflowEngineFacade facade;

    @RequestMapping(method = RequestMethod.GET, value = "/workflow/create")
    public String viewStart( Model model ){
        model.addAttribute( "workflowNames", facade.getDeployedWorkflowNames() );
        return "console/workflow/create";
    }

    @PreAuthorize("hasRole('ROLE_TWE_ADMIN')")
    @RequestMapping(method = RequestMethod.POST, value = "/workflow/create")
    public String create( Model model, RedirectAttributes redirectAttrs, @ModelAttribute("form") CreateWorkflowInstanceForm form, Errors result ){
        Integer version = null;
        Map<String, Object> arguments = null;
        if( StringUtils.isBlank( form.getWorkflowName() ) ){
            result.rejectValue( "workflowName", "workflow.create.single.error.workflowname" );
        }
        if( !StringUtils.isBlank( form.getWorkflowVersion() ) && !NumberUtils.isNumber( form.getWorkflowVersion() ) ){
            result.rejectValue( "workflowVersion", "workflow.create.single.error.workflowversion" );
        }
        else{
            version = StringUtils.isBlank( form.getWorkflowVersion() ) ? null : Integer.valueOf( form.getWorkflowVersion() );
        }
        try{
            arguments = JsonUtil.deserializeHashMap( form.getArguments(), String.class, Object.class );
        }
        catch( Exception e ){
            log.info( e.getMessage(), e );
            result.rejectValue( "arguments", "workflow.create.single.error.arguments" );
        }

        if( !result.hasErrors() ){
            try{
                CreateWorkflowInstance request = new CreateWorkflowInstance();
                request.setWorkflowName( form.getWorkflowName() );
                request.setWorkflowVersion( version );
                request.setLabel1( form.getLabel1() );
                request.setLabel2( form.getLabel2() );
                request.setArguments( arguments );
                facade.createWorkflowInstance( request );
                redirectAttrs.addFlashAttribute( "createdRefNum", request.getRefNum() );
                return "redirect:" + configuration.getConsoleMappingPrefix() + "/console/workflow/create";
            }
            catch( Exception e ){
                model.addAttribute( "error", e.getMessage() );
                log.error( e.getMessage(), e );
            }
        }

        // If post request failed, render view again
        return viewStart( model );
    }

    @PreAuthorize("hasRole('ROLE_TWE_ADMIN')")
    @RequestMapping(method = RequestMethod.POST, value = "/workflow/batchCreate")
    public String batchCreate( Model model, RedirectAttributes redirectAttrs, @ModelAttribute("batchForm") BatchCreateWorkflowInstancesForm batchForm, Errors result ){
        List<CreateWorkflowInstance> requests = null;

        try{
            if( StringUtils.isNotBlank( batchForm.getBatchRequest() ) ){
                requests = (List<CreateWorkflowInstance>)JsonUtil.deserializeCollection( batchForm.getBatchRequest(), ArrayList.class, CreateWorkflowInstance.class );
            }
            if( requests == null || requests.isEmpty() ){
                result.rejectValue( "batchRequest", "workflow.create.batch.error.empty" );
            }
        }
        catch( Exception e ){
            log.info( e.getMessage(), e );
            result.rejectValue( "batchRequest", "workflow.create.batch.error.syntax" );
        }

        if( !result.hasErrors() ){
            Set<String> deployedWorkflowNames = facade.getDeployedWorkflowNames();
            for( CreateWorkflowInstance element : requests ){
                if( StringUtils.isBlank( element.getWorkflowName() ) || !deployedWorkflowNames.contains( element.getWorkflowName() ) ){
                    result.rejectValue( "batchRequest", "workflow.create.batch.error.workflowname" );
                    break;
                }
            }
        }

        if( !result.hasErrors() ){
            try{
                facade.createWorkflowInstances( requests );
                redirectAttrs.addFlashAttribute( "createdRefNums", getRefNums( requests ) );
                return "redirect:" + configuration.getConsoleMappingPrefix() + "/console/workflow/create";
            }
            catch( Exception e ){
                model.addAttribute( "error", e.getMessage() );
                log.error( e.getMessage(), e );
            }
        }

        // If post request failed, render view again
        return viewStart( model );
    }

    private List<Long> getRefNums( List<CreateWorkflowInstance> requests ){
        List<Long> result = new ArrayList<>( requests.size() );
        for( CreateWorkflowInstance request : requests ){
            result.add( request.getRefNum() );
        }
        return result;
    }

    @ModelAttribute("form")
    public CreateWorkflowInstanceForm createSingleModel(){
        return new CreateWorkflowInstanceForm();
    }

    @ModelAttribute("batchForm")
    public BatchCreateWorkflowInstancesForm createBatchModel(){
        return new BatchCreateWorkflowInstancesForm();
    }

}
