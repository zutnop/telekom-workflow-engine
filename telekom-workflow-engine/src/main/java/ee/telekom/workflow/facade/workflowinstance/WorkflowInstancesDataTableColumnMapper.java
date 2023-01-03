package ee.telekom.workflow.facade.workflowinstance;

public enum WorkflowInstancesDataTableColumnMapper {

    REF_NUM(1, "ref_num"),
    NAME(2, "workflow_name"),
    LABEL1(3, "label1"),
    LABEL2(4, "label2"),
    DATE_CREATED(5, "date_created"),
    STATUS(8, "status");

    private int columnId;
    private String fieldName;

    WorkflowInstancesDataTableColumnMapper( int columnId, String fieldName ){
        this.columnId = columnId;
        this.fieldName = fieldName;
    }

    public static WorkflowInstancesDataTableColumnMapper from( int columnId ){
        for( WorkflowInstancesDataTableColumnMapper type : values() ){
            if( type.columnId == columnId ){
                return type;
            }
        }
        return DATE_CREATED;
    }

    public String getFieldName(){
        return fieldName;
    }

    public int getColumnId(){
        return columnId;
    }
}
