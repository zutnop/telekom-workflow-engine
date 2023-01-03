package ee.telekom.workflow.graph;

import java.util.Date;
import java.util.Map;

import ee.telekom.workflow.api.AutoRecovery;

/**
 * When a {@link Node} execution does not immediately return a result it may
 * entitle an external system to perform some actions asynchronously. The action
 * to be done is described by a work item (e.g. wait for a timer or an incoming
 * signal, call a method on a bean or ask a human to perform a task).
 * <p>
 * Another way of describing a {@link GraphWorkItem} is: A work item is a handle/reference
 * to an uncompleted node execution in order to complete it at some future point in time.
 */
public interface GraphWorkItem{

    Long getExternalId();

    Long getExternalGraphInstanceId();

    Token getToken();

    String getSignal();

    Date getDueDate();

    String getBean();

    String getMethod();

    String getRole();

    String getUser();

    Object[] getTaskArguments();

    Map<String, Object> getHumanTaskArguments();

    Object getResult();

    WorkItemStatus getStatus();

    void setStatus( WorkItemStatus status );

    void setResult( Object result );

    void setAutoRecovery( AutoRecovery autoRecovery);

    AutoRecovery autoRecovery();

}
