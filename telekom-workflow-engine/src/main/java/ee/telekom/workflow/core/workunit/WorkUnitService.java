package ee.telekom.workflow.core.workunit;

import java.util.Date;
import java.util.List;

/**
 * Provides services to the work unit producer part of the engine.
 * 
 * @author Christian Klock
 */
public interface WorkUnitService{

    /**
     * Polls new units of work from database.
     * 
     * @param now the current date
     */
    List<WorkUnit> findNewWorkUnits( Date now );

    /**
     * Locks the workflow instances associated with the work units.
     */
    void lock( List<WorkUnit> workUnits );

}
