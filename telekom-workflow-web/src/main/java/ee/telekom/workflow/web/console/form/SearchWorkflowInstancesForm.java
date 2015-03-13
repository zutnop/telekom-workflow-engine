package ee.telekom.workflow.web.console.form;

import java.util.List;

import ee.telekom.workflow.facade.model.SearchWorkflowInstances;

public class SearchWorkflowInstancesForm extends SearchWorkflowInstances{

    private static final long serialVersionUID = 1L;

    private List<String> id;

    public List<String> getId(){
        return id;
    }

    public void setId( List<String> id ){
        this.id = id;
    }

    public boolean hasSearchCriteria(){
        return getRefNum() != null && getRefNum().size() > 0 || getWorkflowName() != null && getWorkflowName().size() > 0 || getLabel1() != null && getLabel1().size() > 0
                || getLabel2() != null && getLabel2().size() > 0 || getStatus() != null;
    }

    public boolean hasId(){
        return getId() != null && getId().size() > 0;
    }

}
