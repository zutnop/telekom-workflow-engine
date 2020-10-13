package ee.telekom.workflow.core.workunit;

public enum WorkType{

    START_WORKFLOW("s"),

    ABORT_WORKFLOW("a"),

    COMPLETE_WORK_ITEM("c"),

    EXECUTE_TASK("e");

    private String code;

    WorkType( String code ){
        this.code = code;
    }

    public String getCode(){
        return code;
    }

}
