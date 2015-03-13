package ee.telekom.workflow.web.rest.form;

public class UpdateInstanceStatusForm{
    private String status;

    public UpdateInstanceStatusForm(){
    }

    public UpdateInstanceStatusForm( String status ){
        this.status = status;
    }

    public String getStatus(){
        return status;
    }

}
