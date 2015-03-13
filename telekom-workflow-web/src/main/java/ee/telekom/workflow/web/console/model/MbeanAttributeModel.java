package ee.telekom.workflow.web.console.model;

import java.io.Serializable;

public class MbeanAttributeModel implements Serializable{

    private static final long serialVersionUID = 1L;
    private String name;
    private String description;
    private Object value;

    public String getName(){
        return name;
    }

    public void setName( String name ){
        this.name = name;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription( String description ){
        this.description = description;
    }

    public Object getValue(){
        return value;
    }

    public void setValue( Object value ){
        this.value = value;
    }

}
