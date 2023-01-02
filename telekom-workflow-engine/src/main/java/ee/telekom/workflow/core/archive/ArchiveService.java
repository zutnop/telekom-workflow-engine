package ee.telekom.workflow.core.archive;

/**
 * Provides a method to archive a workflow instance's data once it is fully executed or aborted.
 * 
 * @author Christian Klock
 */
public interface ArchiveService{

    /**
     * Archive a workflow instance's data once it is fully executed or aborted.
     */
    void archive( long woinRefNum, int archivePeriodLength );

}
