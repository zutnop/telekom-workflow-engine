package ee.telekom.workflow.core.workunit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;

@Service
@Transactional
public class WorkUnitServiceImpl implements WorkUnitService{

    @Autowired
    private WorkUnitDao dao;
    @Autowired
    private WorkflowInstanceService workflowInstanceService;
    @Autowired
    private WorkflowEngineConfiguration config;

    @Override
    public List<WorkUnit> findNewWorkUnits( Date now ){
        String clusterName = config.getClusterName();
        List<WorkUnit> newWorkUnits = dao.findNewWorkUnits( now, clusterName );

        // The DAO returns a list of new work units that may contain more than one item per workflow instance.
        // However, our concurrency strategy only supports one work unit per workflow instance at a time.
        // If there is more than one work unit, we need to select one of them. When doing this selection, we
        // make use of the fact that the list is ordered such that the first work unit for each workflow instance
        // reflects the action with highest priority.
        // (1) ABORT_PROCESS work units are of highest priority. If there is a ABORT_PROCESS work unit, we select this one.
        // (2) First come, first serve. We take the work unit with the earliest "waiting since date". If two such work units have
        //     an identical "waiting since date", then we choose the one earlier inserted into the database (i.e. the one with
        //     lower work_item.ref_num.
        int initialSize = newWorkUnits.size();
        List<WorkUnit> workUnits = new ArrayList<>( initialSize );
        Long previousWoinRefNum = null;
        for( WorkUnit wu : newWorkUnits ){
            Long woinRefNum = wu.getWoinRefNum();
            if( !woinRefNum.equals( previousWoinRefNum ) ){
                workUnits.add( wu );
                previousWoinRefNum = woinRefNum;
            }
        }
        return workUnits;
    }

    @Override
    public void lock( List<WorkUnit> workUnits ){
        List<Long> woinRefNums = new ArrayList<Long>( workUnits.size() );
        for( WorkUnit wu : workUnits ){
            woinRefNums.add( wu.getWoinRefNum() );
        }
        workflowInstanceService.lock( woinRefNums );
    }

}
