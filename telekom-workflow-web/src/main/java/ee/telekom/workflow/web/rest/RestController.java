package ee.telekom.workflow.web.rest;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.google.gson.JsonObject;

import ee.telekom.workflow.core.common.UnexpectedStatusException;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.facade.WorkflowEngineFacade;
import ee.telekom.workflow.facade.model.CreateWorkflowInstance;
import ee.telekom.workflow.facade.model.WorkItemState;
import ee.telekom.workflow.facade.model.WorkflowInstanceState;
import ee.telekom.workflow.graph.WorkItemStatus;
import ee.telekom.workflow.util.JsonUtil;
import ee.telekom.workflow.web.rest.form.UpdateInstanceStatusForm;
import ee.telekom.workflow.web.rest.model.HumanTaskModel;
import ee.telekom.workflow.web.rest.model.WorkflowInstanceRestModel;
import ee.telekom.workflow.web.util.JsonParserUtil;

/**
 * Controller that implements a REST API.
 * <p/>
 * For details on individual API methods, please refer to the RequestMapping methods.
 * <p/>
 * Some method expect/support an argument/arguments/result field in the request body. Regardless of the deserializer (JSON: e.g. Jackson or GSON, XML:...), the values of these fields
 * are forwarded to lower level engine methods such that maps are implemented using LinkedHashMap's and lists or arrays are implemented using ArrayList's. Furthermore, do not mix up
 * plain JSON with the JSON base format that the engine internally uses to store, e.g., workflow instance attributes.
 * <pre>
 *  {"arg1":{"refNum":1, "date":"22.09.2014"}, "arg2":[true, 1, "text"]} is forwarded by the following map:
 *  Map<String, Object> map = new LinkedHashMap<>();
 *  Map<String, Object> arg1 = new LinkedHashMap<>();
 *  arg1.put("refNum",1);
 *  arg1.put("date","22.09.2014");
 *  List<?> arg2 = new ArrayList<>();
 *  arg2.add(true);
 *  arg2.add(1);
 *  arg2.add("text");
 *  map.put("arg1",arg1);
 *  map.put("arg2",arg2);
 * </pre>
 */
@Controller
@RequestMapping("/api")
public class RestController{

    private static final Logger log = LoggerFactory.getLogger( RestController.class );

    @Autowired
    private WorkflowEngineFacade facade;

    @Autowired
    private RequestMappingHandlerAdapter adapter;

    @PostConstruct
    public void init(){
        adapter.getMessageConverters().add( new GsonHttpMessageConverter( true ) );
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String errorHandler( Exception e ){
        log.error( "Rest exception occured", e );
        return e.getMessage();
    }

    /**
     * Creates a new workflow instance.
     * <p/>
     * <pre>
     * Request:  POST /workflowInstance {workflowName: "credit.step1", workflowVersion: null, arguments: {"arg1":{"refNum":1, "date":"22.09.2014"}, "arg2":[true, 1, "text"]}, label1: "one", label2: null }
     * Response: OK, {refNum: 1, workflowName: "credit.step1", workflowVersion: null, label1: "one", label2: null, status: NEW}
     * </pre>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/workflowInstance", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<WorkflowInstanceRestModel> create( @RequestBody String body ){
        JsonObject root = JsonParserUtil.parseJson( body );
        CreateWorkflowInstance request = new CreateWorkflowInstance();
        request.setWorkflowName( JsonParserUtil.getAsNullSafeString( root, "workflowName" ) );
        request.setWorkflowVersion( JsonParserUtil.getAsNullSafeInteger( root, "workflowVersion" ) );
        request.setLabel1( JsonParserUtil.getAsNullSafeString( root, "label1" ) );
        request.setLabel2( JsonParserUtil.getAsNullSafeString( root, "label2" ) );
        request.setArguments( JsonUtil.deserializeHashMap( JsonParserUtil.toNullSafeJsonString( root, "arguments" ), String.class, Object.class ) );

        facade.createWorkflowInstance( request );
        WorkflowInstanceState woin = facade.findWorkflowInstance( request.getRefNum(), true );
        return new ResponseEntity<>( createInstanceModel( woin ), HttpStatus.OK );
    }

    /**
     * Finds a workflow instance.
     * <p/>
     * <pre>
     * Request:  GET /workflowInstance/1
     * Response: OK {refNum: 1, workflowName: "credit.step1", workflowVersion: null, label1: "one", label2: null, status: NEW}
     * Response: NOT_FOUND, if no such workflow instance exists
     * </pre>
     */
    @RequestMapping(method = RequestMethod.GET, value = "/workflowInstance/{woinRefNum}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<WorkflowInstanceRestModel> find( @PathVariable long woinRefNum ){
        WorkflowInstanceState woin = facade.findWorkflowInstance( woinRefNum, null );
        if( woin == null ){
            return new ResponseEntity<>( HttpStatus.NOT_FOUND );
        }
        else{
            return new ResponseEntity<>( createInstanceModel( woin ), HttpStatus.OK );
        }
    }

    /**
     * Aborts, suspends or resumes a workflow instance.
     * <p/>
     * <pre>
     * Request:  POST /workflowInstance/{woinRefNum} {status: "ABORT"}
     * Response: OK, {refNum: 1, workflowName: "credit.step1", workflowVersion: null, label1: "one", label2: null, status: ABORT}
     * Response: NOT_FOUND, if no such workflow instance exists
     * Response: CONFLICT, if the workflow's current status does not allow the requested status transition
     * Response: BAD_REQUEST, if the new status is not allowed.
     * </pre>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/workflowInstance/{woinRefNum}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<WorkflowInstanceRestModel> updateIntance( @PathVariable long woinRefNum, @RequestBody UpdateInstanceStatusForm form ){
        WorkflowInstanceState woin = facade.findWorkflowInstance( woinRefNum, true );
        if( woin == null ){
            return new ResponseEntity<>( HttpStatus.NOT_FOUND );
        }
        try{
            if( WorkflowInstanceStatus.ABORT.name().equals( form.getStatus() ) ){
                facade.abortWorkflowInstance( woinRefNum );
            }
            else if( WorkflowInstanceStatus.SUSPENDED.name().equals( form.getStatus() ) ){
                facade.suspendWorkflowInstance( woinRefNum );
            }
            else if( WorkflowInstanceStatus.EXECUTING.name().equals( form.getStatus() ) ){
                facade.resumeWorkflowInstance( woinRefNum );
            }
            else{
                return new ResponseEntity<>( HttpStatus.BAD_REQUEST );
            }
        }
        catch( UnexpectedStatusException e ){
            return new ResponseEntity<>( HttpStatus.CONFLICT );
        }
        woin = facade.findWorkflowInstance( woinRefNum, true );
        return new ResponseEntity<>( createInstanceModel( woin ), HttpStatus.OK );
    }

    /**
     * Notifies all waiting signal work items of the given workflow instance and the given signal name.
     * <p/>
     * Technically, sets the work item's result value and updates the status to EXECUTED.
     * <p/>
     * <pre>
     * Request:  POST /workflowInstance/1/signal/invoice {argument: {refNum:3, invoiceAmount: "10 Euro"}}
     * Response: NO_CONTENT
     * </pre>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/workflowInstance/{woinRefNum}/signal/{signal}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<Void> sendSignal( @PathVariable long woinRefNum, @PathVariable String signal, @RequestBody String argument ){
        facade.sendSignalToWorkflowInstance( woinRefNum, signal, JsonUtil.deserialize( argument ) );
        return new ResponseEntity<>( HttpStatus.NO_CONTENT );
    }

    /**
     * (Un)assigns a human task to a user or submits its result.
     * <p/>
     * In order to unassign a human task from a user, set an empty user name.
     * <p/>
     * Technically, assigning means updating the user field while submitting means to update the status field to EXECUTED and setting the result value.
     * <p/>
     * <pre>
     * Request:  POST /workflowInstance/1/humanTask/2 {result: {resolution: "completed"}}
     * Response: OK, {refNum:3, woinRefNum:1, tokenId:2, status:EXECUTED, role:"auditor", user:"hans", arguments:{task:"audit customer 500"}, result: {resolution: "completed"}}
     * Response: NOT_FOUND, if no such human task exists
     * Response: CONFLICT, if the human task's status does not allow the requested status transition
     * </pre>
     */
    @RequestMapping(method = RequestMethod.POST, value = "/workflowInstance/{woinRefNum}/humanTask/{tokenId}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<HumanTaskModel> updateHumanTask( @PathVariable long woinRefNum, @PathVariable int tokenId, @RequestBody String body ){
        WorkItemState woit = facade.findActiveWorkItemByTokenId( woinRefNum, tokenId );
        if( woit == null ){
            return new ResponseEntity<>( HttpStatus.NOT_FOUND );
        }
        try{
            JsonObject root = JsonParserUtil.parseJson( body );
            String user = JsonParserUtil.getAsNullSafeString( root, "user" );
            if( user != null ){
                String userName = user.trim().isEmpty() ? null : user.trim();
                facade.assignHumanTask( woit.getRefNum(), userName );
            }
            else{
                facade.submitHumanTask( woit.getRefNum(), JsonUtil.deserialize( JsonParserUtil.toNullSafeJsonString( root, "result" ) ) );
            }
        }
        catch( UnexpectedStatusException e ){
            return new ResponseEntity<>( HttpStatus.CONFLICT );
        }
        woit = facade.findWorkItem( woit.getRefNum(), true );
        return new ResponseEntity<>( createHumanTaskModel( woit ), HttpStatus.OK );
    }

    /**
     * Searches workflow instance's that match the given criteria.
     * <p/>
     * <pre>
     * Request:  GET /workflowInstance/search?label1=aClientId
     * Response: OK, [{refNum: 1, workflowName: "credit.step1", workflowVersion: null, label1: "aClientId", label2: null, status: NEW}, {refNum: 2, workflowName: "credit.client.step1", workflowVersion: null, label1: "aClientId", label2: "anotherIdentificator", status: NEW}]
     * </pre>
     */
    @RequestMapping(method = RequestMethod.GET, value = "/workflowInstance/search",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<List<WorkflowInstanceRestModel>> findInstances( @RequestParam(required = false) String label1,
                                                                          @RequestParam(required = false) String label2,
                                                                          @RequestParam(required = false) Boolean activeOnly ){
        if( activeOnly == null ){
            activeOnly = true;
        }
        label1 = StringUtils.trimToNull( label1 );
        label2 = StringUtils.trimToNull( label2 );
        List<WorkflowInstanceState> woins = facade.findWorkflowInstancesByLabels( label1, label2, activeOnly );
        return new ResponseEntity<>( createInstancesModel( woins ), HttpStatus.OK );
    }

    /**
     * Searches active human tasks by role and/or user.
     * <p/>
     * <pre>
     * Request:  GET /humanTask/search?role=auditor&user=hans
     * Response: OK, [{refNum:3, woinRefNum:1, tokenId:2, status:EXECUTED, role:"auditor", user:"hans", arguments:{task:"audit customer 500"}}]
     * </pre>
     */
    @RequestMapping(method = RequestMethod.GET, value = "/humanTask/search",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<List<HumanTaskModel>> findActiveHumanTasks( @RequestParam(required = false) String role,
                                                                      @RequestParam(required = false) String user ){
        if( StringUtils.isBlank( role ) && StringUtils.isBlank( user ) ){
            return new ResponseEntity<>( HttpStatus.BAD_REQUEST );
        }
        List<WorkItemState> woits = facade.findActiveHumanTasksByRoleAndUser( role, user );
        return new ResponseEntity<>( createHumanTasksModel( woits ), HttpStatus.OK );
    }

    ///// PRIVATE METHODS /////

    private WorkflowInstanceRestModel createInstanceModel( WorkflowInstanceState woin ){
        return new WorkflowInstanceRestModel( woin.getRefNum(),
                woin.getWorkflowName(),
                woin.getWorkflowVersion(),
                woin.getLabel1(),
                woin.getLabel2(),
                WorkflowInstanceStatus.valueOf( woin.getStatus() ) );
    }

    private List<WorkflowInstanceRestModel> createInstancesModel( List<WorkflowInstanceState> woins ){
        List<WorkflowInstanceRestModel> result = new ArrayList<>();
        for( WorkflowInstanceState woin : woins ){
            result.add( createInstanceModel( woin ) );
        }
        return result;
    }

    private List<HumanTaskModel> createHumanTasksModel( List<WorkItemState> woits ){
        List<HumanTaskModel> result = new ArrayList<>();
        for( WorkItemState woit : woits ){
            result.add( createHumanTaskModel( woit ) );
        }
        return result;
    }

    private HumanTaskModel createHumanTaskModel( WorkItemState woit ){
        return new HumanTaskModel( woit.getRefNum(),
                woit.getWoinRefNum(),
                woit.getTokenId(),
                WorkItemStatus.valueOf( woit.getStatus() ),
                woit.getRole(),
                woit.getUserName(),
                woit.getArguments(),
                woit.getResult() );
    }

}
