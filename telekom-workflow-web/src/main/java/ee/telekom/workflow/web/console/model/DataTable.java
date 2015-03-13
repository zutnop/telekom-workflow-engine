package ee.telekom.workflow.web.console.model;

import java.util.ArrayList;
import java.util.List;

public class DataTable{

    private int draw;
    private int recordsTotal;
    private int recordsFiltered;
    private List<WorkflowInstanceSearchModel> data = new ArrayList<>();

    public int getDraw(){
        return draw;
    }

    public void setDraw( int draw ){
        this.draw = draw;
    }

    public int getRecordsTotal(){
        return recordsTotal;
    }

    public void setRecordsTotal( int recordsTotal ){
        this.recordsTotal = recordsTotal;
    }

    public int getRecordsFiltered(){
        return recordsFiltered;
    }

    public void setRecordsFiltered( int recordsFiltered ){
        this.recordsFiltered = recordsFiltered;
    }

    public List<WorkflowInstanceSearchModel> getData(){
        return data;
    }

    public void setData( List<WorkflowInstanceSearchModel> data ){
        this.data = data;
    }
}