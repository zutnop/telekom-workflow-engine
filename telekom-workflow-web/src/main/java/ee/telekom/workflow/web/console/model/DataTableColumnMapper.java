package ee.telekom.workflow.web.console.model;

public enum DataTableColumnMapper{

    REF_NUM(1, "refNum"),
    NAME(2, "workflowNameWithVersion"),
    LABEL1(3, "label1"),
    LABEL2(4, "label2"),
    DATE_CREATED(5, "dateCreated"),
    NEXT_TIMER_DUE_DATE(6, "nextTimerDueDate"),
    HAS_ACTIVE_HUMAN_TASK(7, "hasActiveHumanTask"),
    STATUS(8, "status");

    private int columnId;
    private String fieldName;

    DataTableColumnMapper( int columnId, String fieldName ){
        this.columnId = columnId;
        this.fieldName = fieldName;
    }

    public static DataTableColumnMapper from( int columnId ){
        for( DataTableColumnMapper type : values() ){
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
