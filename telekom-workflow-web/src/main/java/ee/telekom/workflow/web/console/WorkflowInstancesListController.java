package ee.telekom.workflow.web.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ee.telekom.workflow.core.common.UnexpectedStatusException;
import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.facade.WorkflowEngineFacade;
import ee.telekom.workflow.facade.model.WorkflowInstanceFacadeStatus;
import ee.telekom.workflow.facade.model.WorkflowInstanceState;
import ee.telekom.workflow.web.console.form.SearchWorkflowInstancesForm;
import ee.telekom.workflow.web.console.helper.MessageHelper;
import ee.telekom.workflow.web.console.model.DataTable;
import ee.telekom.workflow.web.console.model.DataTableColumnMapper;
import ee.telekom.workflow.web.console.model.WorkflowInstanceSearchModel;

/**
 * Controller for workflow instances search and result view
 */
@Controller
@RequestMapping("/console")
@SessionAttributes("instanceSearchForm")
public class WorkflowInstancesListController{

    @Autowired
    private WorkflowEngineConfiguration configuration;
    @Autowired
    private WorkflowEngineFacade facade;
    @Autowired
    private MessageHelper messageHelper;

    @RequestMapping(value = "/workflow/instances", method = RequestMethod.GET)
    public String searchInstancesView( Model model, HttpServletRequest request, @ModelAttribute("instanceSearchForm") SearchWorkflowInstancesForm form ){
        request.getSession().removeAttribute( "instancesSearchResult" );
        form = createFormOnGetRequest( request, form );
        if( form.hasId() ){
            validateAndConvertIdsToRefNums( form, model );
        }
        else{
            form.setRefNum( null );
        }
        removeBlankLabels( form.getLabel1() );
        removeBlankLabels( form.getLabel2() );
        if( !model.containsAttribute( "error" ) ){
            if( form.hasSearchCriteria() ){
                request.getSession().setAttribute( "instancesSearchResult", createModels( facade.findWorkflowInstances( form ) ) );
            }
            else{
                model.addAttribute( "warning", "workflow.search.message.empty.filter" );
            }
        }
        model.addAttribute( "instanceSearchForm", form );
        model.addAttribute( "graphNames", facade.getDeployedWorkflowNames() );
        model.addAttribute( "workflowStatuses", WorkflowInstanceFacadeStatus.values() );
        return "console/workflow/instances";
    }

    private SearchWorkflowInstancesForm createFormOnGetRequest( HttpServletRequest request, SearchWorkflowInstancesForm form ){
        String workflowName = request.getParameter( "workflowName" );
        String status = request.getParameter( "status" );
        if( workflowName != null ){
            form = new SearchWorkflowInstancesForm();
            List<String> workflowNames = new ArrayList<>();
            workflowNames.add( workflowName );
            form.setWorkflowName( workflowNames );
            if( status != null ){
                form.setStatus( Collections.unmodifiableList( Arrays.asList( WorkflowInstanceFacadeStatus.valueOf( status ) ) ) );
            }
        }
        return form;
    }

    private void validateAndConvertIdsToRefNums( SearchWorkflowInstancesForm form, Model model ){
        List<Long> refNums = new ArrayList<>();
        for( String num : form.getId() ){
            String value = num.trim();
            if( value.length() > 0 && StringUtils.isNumeric( value ) ){
                refNums.add( Long.valueOf( value ) );
            }
            else{
                model.addAttribute( "error", "workflow.search.error.invalid.id" );
            }
        }
        form.setRefNum( refNums );
    }

    private List<WorkflowInstanceSearchModel> createModels( List<WorkflowInstanceState> instances ){
        List<WorkflowInstanceSearchModel> searchResult = new ArrayList<>();
        List<Long> refNums = new ArrayList<>();

        for( WorkflowInstanceState woin : instances ){
            refNums.add( woin.getRefNum() );
        }
        Map<Long, Date> nextTimerDueDate = facade.getNextActiveTimerDueDates( refNums );
        Set<Long> hasActiveHumanTask = facade.getWorkflowInstancesWithActiveHumanTask( refNums );

        for( WorkflowInstanceState instance : instances ){
            long refNum = instance.getRefNum();
            searchResult.add( createModel( instance, nextTimerDueDate.get( refNum ), hasActiveHumanTask.contains( refNum ) ) );
        }
        return searchResult;
    }

    private WorkflowInstanceSearchModel createModel( WorkflowInstanceState woin, Date nextTimerDueDate, boolean hasActiveHumanTask ){
        WorkflowInstanceSearchModel model = new WorkflowInstanceSearchModel();
        model.setRefNum( woin.getRefNum() );
        model.setWorkflowNameWithVersion( woin.getWorkflowName() + ":" + messageHelper.getVersionText( woin.getWorkflowVersion() ) );
        model.setLabel1( woin.getLabel1() );
        model.setLabel2( woin.getLabel2() );
        model.setDateCreated( woin.getDateCreated() );
        model.setNextTimerDueDate( nextTimerDueDate );
        model.setHasActiveHumanTask( messageHelper.getHasActiveHumanTaskText( hasActiveHumanTask ) );
        model.setDisplayStatus( messageHelper.getStatusText( WorkflowInstanceStatus.valueOf( woin.getStatus() ) ) );
        model.setStatus( woin.getStatus() );
        return model;
    }

    @RequestMapping(value = "/workflow/instances", method = RequestMethod.POST)
    public String searchInstancesAction( @ModelAttribute("instanceSearchForm") SearchWorkflowInstancesForm form, RedirectAttributes model ){
        model.addFlashAttribute( "instanceSearchForm", form );
        return "redirect:" + configuration.getConsoleMappingPrefix() + "/console/workflow/instances";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/workflow/instances/action", method = RequestMethod.POST)
    public String abortInstances( @RequestParam String action, @ModelAttribute("refNums") List<Long> refNums, RedirectAttributes model ){
        switch( action ) {
            case "abort":
                invokeAction( refNums, model, new WorkflowInstanceActionInvoker(){
                    @Override
                    void invoke( Long refNum ){
                        facade.abortWorkflowInstance( refNum );
                    }
                } );
                break;
            case "suspend":
                invokeAction( refNums, model, new WorkflowInstanceActionInvoker(){
                    @Override
                    void invoke( Long refNum ){
                        facade.suspendWorkflowInstance( refNum );
                    }
                } );
                break;
            case "resume":
                invokeAction( refNums, model, new WorkflowInstanceActionInvoker(){
                    @Override
                    void invoke( Long refNum ){
                        facade.resumeWorkflowInstance( refNum );
                    }
                } );
                break;
            case "retry":
                invokeAction( refNums, model, new WorkflowInstanceActionInvoker(){
                    @Override
                    void invoke( Long refNum ){
                        facade.retryWorkflowInstance( refNum );
                    }
                } );
                break;

        }
        return "redirect:" + configuration.getConsoleMappingPrefix() + "/console/workflow/instances";
    }

    private void invokeAction( List<Long> refNums, RedirectAttributes model, WorkflowInstanceActionInvoker handler ){
        for( Long refNum : refNums ){
            try{
                handler.invoke( refNum );
            }
            catch( UnexpectedStatusException e ){
                model.addFlashAttribute( "actionError", "workflow.instances.action.error.unexpectedstatus" );
            }
        }
        model.addFlashAttribute( "actionMessage", "workflow.instances.action.success" );
    }

    @RequestMapping(method = RequestMethod.POST, value = "/workflow/instances/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataTable> searchInstancesAjax( Model model,
            @ModelAttribute("instanceSearchForm") SearchWorkflowInstancesForm form,
            HttpServletRequest request ){
        @SuppressWarnings("unchecked")
        List<WorkflowInstanceSearchModel> searchResult = (List<WorkflowInstanceSearchModel>)request.getSession().getAttribute( "instancesSearchResult" );
        List<WorkflowInstanceSearchModel> page = new ArrayList<>();
        if( searchResult != null && searchResult.size() > 0 ){
            Integer column = Integer.valueOf( request.getParameter( "order[0][column]" ) );
            String direction = request.getParameter( "order[0][dir]" );
            List<WorkflowInstanceSearchModel> sortedSearchResult = sortSearchResult( searchResult, column, direction );
            int pageStart = Math.min( form.getStart(), Math.max( searchResult.size() - 1, 0 ) );
            int pageEnd = Math.min( form.getStart() + form.getLength(), searchResult.size() );
            page = sortedSearchResult.subList( pageStart, pageEnd );
        }
        return new ResponseEntity<>( createDataTable( request, searchResult, page ), HttpStatus.OK );
    }

    private DataTable createDataTable( HttpServletRequest request, List<WorkflowInstanceSearchModel> searchResult, List<WorkflowInstanceSearchModel> page ){
        DataTable dataTable = new DataTable();
        dataTable.setDraw( Integer.valueOf( request.getParameter( "draw" ) ) );
        if( searchResult != null ){
            dataTable.setRecordsTotal( searchResult.size() );
            dataTable.setRecordsFiltered( searchResult.size() );
        }
        dataTable.setData( page );
        return dataTable;
    }

    protected List<WorkflowInstanceSearchModel> sortSearchResult( List<WorkflowInstanceSearchModel> result, int column, String direction ){
        List<WorkflowInstanceSearchModel> unorderedSource = new ArrayList<>( result );
        String fieldName = DataTableColumnMapper.from( column ).getFieldName();
        BeanComparator<WorkflowInstanceSearchModel> beanComparator;
        if( "asc".equalsIgnoreCase( direction ) ){
            beanComparator = new BeanComparator<>( fieldName, new NullComparator() );
        }
        else{
            beanComparator = new BeanComparator<>( fieldName, new ReverseComparator( new NullComparator() ) );
        }
        Collections.sort( unorderedSource, beanComparator );
        return unorderedSource;
    }

    @ModelAttribute("instanceSearchForm")
    private SearchWorkflowInstancesForm instanceSearchForm(){
        return new SearchWorkflowInstancesForm();
    }

    private static void removeBlankLabels( List<String> labels ){
        if( labels != null ){
            Iterator<String> iterator = labels.iterator();
            while( iterator.hasNext() ){
                String label = iterator.next();
                if( StringUtils.isBlank( label ) ){
                    iterator.remove();
                }
            }
        }
    }

    private static abstract class WorkflowInstanceActionInvoker{
        abstract void invoke( Long refNum );
    }

}
