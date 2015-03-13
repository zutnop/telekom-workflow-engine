package ee.telekom.workflow.facade.util;

import static ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus.ABORT;
import static ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus.ABORTING;
import static ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus.ABORTING_ERROR;
import static ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus.EXECUTED;
import static ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus.EXECUTING;
import static ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus.EXECUTING_ERROR;
import static ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus.NEW;
import static ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus.STARTING;
import static ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus.STARTING_ERROR;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.facade.model.WorkflowInstanceFacadeStatus;

/**
 * Utility class to map between {@link WorkflowInstanceFacadeStatus} and {@link WorkflowInstanceStatus}.
 *
 * @author Christian Klock
 */
public class StatusUtil{

    private static final Map<WorkflowInstanceFacadeStatus, List<WorkflowInstanceStatus>> MAPPING = new HashMap<>();

    static{
        MAPPING.put( WorkflowInstanceFacadeStatus.ACTIVE, Collections.unmodifiableList( Arrays.asList( NEW, STARTING, EXECUTING, ABORT, ABORTING ) ) );
        MAPPING.put( WorkflowInstanceFacadeStatus.ERROR, Collections.unmodifiableList( Arrays.asList( STARTING_ERROR, EXECUTING_ERROR, ABORTING_ERROR ) ) );
        MAPPING.put( WorkflowInstanceFacadeStatus.SUSPENDED, Collections.unmodifiableList( Arrays.asList( WorkflowInstanceStatus.SUSPENDED ) ) );
        MAPPING.put( WorkflowInstanceFacadeStatus.ABORTED, Collections.unmodifiableList( Arrays.asList( WorkflowInstanceStatus.ABORTED ) ) );
        MAPPING.put( WorkflowInstanceFacadeStatus.EXECUTED, Collections.unmodifiableList( Arrays.asList( EXECUTED ) ) );
    }

    private static final Map<WorkflowInstanceStatus, WorkflowInstanceFacadeStatus> REVERSE_MAPPING = new HashMap<>();

    static{
        for( Entry<WorkflowInstanceFacadeStatus, List<WorkflowInstanceStatus>> entry : MAPPING.entrySet() ){
            for( WorkflowInstanceStatus status : entry.getValue() ){
                REVERSE_MAPPING.put( status, entry.getKey() );
            }
        }
    }

    public static Collection<WorkflowInstanceStatus> toInternal( List<WorkflowInstanceFacadeStatus> facadeStatuses ){
        Set<WorkflowInstanceStatus> set = new HashSet<>();
        for( WorkflowInstanceFacadeStatus status : facadeStatuses ){
            set.addAll( MAPPING.get( status ));
        }
        return set;
    }

    public static WorkflowInstanceFacadeStatus toFacade( WorkflowInstanceStatus internalStatus ){
        return REVERSE_MAPPING.get( internalStatus );
    }

}
