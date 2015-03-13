package ee.telekom.workflow.web.console.form;

import java.util.List;

public class BatchCreateWorkflowInstancesForm{

    private List<Long> refNums;
    private String batchRequest;

    public List<Long> getRefNums(){
        return refNums;
    }

    public void setRefNums( List<Long> refNums ){
        this.refNums = refNums;
    }

    public String getBatchRequest(){
        return batchRequest;
    }

    public void setBatchRequest( String batchRequest ){
        this.batchRequest = batchRequest;
    }

}
