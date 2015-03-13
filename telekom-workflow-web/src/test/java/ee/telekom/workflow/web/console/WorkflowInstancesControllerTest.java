package ee.telekom.workflow.web.console;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ee.telekom.workflow.web.console.model.DataTableColumnMapper;
import ee.telekom.workflow.web.console.model.WorkflowInstanceSearchModel;

public class WorkflowInstancesControllerTest{

    private final WorkflowInstancesListController controller = new WorkflowInstancesListController();

    @Test
    public void shouldSortInstancesAscending(){
        List<WorkflowInstanceSearchModel> result = controller.sortSearchResult( mockInstances(), DataTableColumnMapper.LABEL1.getColumnId(), "asc" );
        assertListOrderEquals( result, 0, 1, 3, 2 );
    }

    @Test
    public void shouldSortInstancesDescending(){
        List<WorkflowInstanceSearchModel> result = controller.sortSearchResult( mockInstances(), DataTableColumnMapper.LABEL1.getColumnId(), "desc" );
        assertListOrderEquals( result, 2, 3, 1, 0 );
    }

    private void assertListOrderEquals( List<WorkflowInstanceSearchModel> result, Integer... expectedOrder ){
        int i = 0;
        for( Integer refNum : expectedOrder ){
            assertEquals( refNum.longValue(), result.get( i ).getRefNum().longValue() );
            i++;
        }
    }

    private List<WorkflowInstanceSearchModel> mockInstances(){
        List<WorkflowInstanceSearchModel> data = new ArrayList<>();
        data.add( createInstance( 0, "label1" ) );
        data.add( createInstance( 1, "label2" ) );
        data.add( createInstance( 2, null ) );
        data.add( createInstance( 3, "label4" ) );
        return data;
    }

    private WorkflowInstanceSearchModel createInstance( long refNum, String label1 ){
        WorkflowInstanceSearchModel instance = new WorkflowInstanceSearchModel();
        instance.setRefNum( refNum );
        instance.setLabel1( label1 );
        return instance;
    }
}