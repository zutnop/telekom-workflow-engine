package ee.telekom.workflow.core.workunit;

public enum WorkType{

    START_WORKFLOW("start"),

    ABORT_WORKFLOW("abort"),

    COMPLETE_WORK_ITEM("complete"),

    EXECUTE_TASK("execute");

    private String description;

    private WorkType( String description ){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

}
