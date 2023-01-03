package ee.telekom.workflow.graph.core;

import java.util.Date;
import java.util.Map;

import ee.telekom.workflow.api.AutoRecovery;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.Token;
import ee.telekom.workflow.graph.WorkItemStatus;

public class GraphWorkItemImpl implements GraphWorkItem{

    private Long externalId;
    private Long externalGraphIntanceId;

    private Token token;

    private String signal;
    private Date dueDate;

    private String bean;
    private String method;

    private String role;
    private String user;

    private Object arguments;
    private Object result;

    private WorkItemStatus status;

    private AutoRecovery autoRecovery;

    public static GraphWorkItemImpl createSignalItem( Token token, String signal ){
        GraphWorkItemImpl result = new GraphWorkItemImpl();
        result.setExternalGraphInstanceId( token.getInstance().getExternalId() );
        result.setToken( token );
        result.setStatus( WorkItemStatus.NEW );
        result.setSignal( signal );
        return result;
    }

    public static GraphWorkItemImpl createTimerItem( Token token, Date dueDate ){
        GraphWorkItemImpl result = new GraphWorkItemImpl();
        result.setExternalGraphInstanceId( token.getInstance().getExternalId() );
        result.setToken( token );
        result.setStatus( WorkItemStatus.NEW );
        result.setDueDate( dueDate );
        return result;
    }

    public static GraphWorkItemImpl createTaskItem( Token token, String bean, String method, AutoRecovery autoRecovery, Object[] arguments ){
        GraphWorkItemImpl result = new GraphWorkItemImpl();
        result.setExternalGraphInstanceId( token.getInstance().getExternalId() );
        result.setToken( token );
        result.setStatus( WorkItemStatus.NEW );
        result.setBean( bean );
        result.setMethod( method );
        result.setArguments( arguments );
        result.setAutoRecovery(autoRecovery);
        return result;
    }

    public static GraphWorkItemImpl createHumanTaskItem( Token token, String role, String user, Map<String, Object> arguments ){
        GraphWorkItemImpl result = new GraphWorkItemImpl();
        result.setExternalGraphInstanceId( token.getInstance().getExternalId() );
        result.setToken( token );
        result.setStatus( WorkItemStatus.NEW );
        result.setRole( role );
        result.setUser( user );
        result.setArguments( arguments );
        return result;
    }

    @Override
    public Long getExternalId(){
        return externalId;
    }

    public void setExternalId( Long externalId ){
        this.externalId = externalId;
    }

    @Override
    public Long getExternalGraphInstanceId(){
        return externalGraphIntanceId;
    }

    public void setExternalGraphInstanceId( Long externalGraphIntanceId ){
        this.externalGraphIntanceId = externalGraphIntanceId;
    }

    @Override
    public Token getToken(){
        return token;
    }

    public void setToken( Token token ){
        this.token = token;
    }

    @Override
    public String getSignal(){
        return signal;
    }

    public void setSignal( String signal ){
        this.signal = signal;
    }

    @Override
    public Date getDueDate(){
        return dueDate;
    }

    public void setDueDate( Date dueDate ){
        this.dueDate = dueDate;
    }

    @Override
    public String getBean(){
        return bean;
    }

    public void setBean( String bean ){
        this.bean = bean;
    }

    @Override
    public String getMethod(){
        return method;
    }

    public void setMethod( String method ){
        this.method = method;
    }

    @Override
    public String getRole(){
        return role;
    }

    public void setRole( String role ){
        this.role = role;
    }

    @Override
    public String getUser(){
        return user;
    }

    public void setUser( String user ){
        this.user = user;
    }

    @Override
    public Object[] getTaskArguments(){
        return (Object[])arguments;
    }

    @Override
    public Map<String, Object> getHumanTaskArguments(){
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>)arguments;
        return result;
    }

    public void setArguments( Object arguments ){
        this.arguments = arguments;
    }

    @Override
    public Object getResult(){
        return result;
    }

    @Override
    public void setResult( Object result ){
        this.result = result;
    }

    @Override
    public void setAutoRecovery(AutoRecovery autoRecovery){
        this.autoRecovery = autoRecovery;
    }

    @Override
    public AutoRecovery autoRecovery() {
        return autoRecovery;
    }

    @Override
    public WorkItemStatus getStatus(){
        return status;
    }

    @Override
    public void setStatus( WorkItemStatus status ){
        this.status = status;
    }

}
