package ee.telekom.workflow.web.console.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;

import ee.telekom.workflow.facade.model.WorkItemState;
import ee.telekom.workflow.facade.util.DateUtil;

public class WorkItemStateModel extends WorkItemState implements Serializable{

    private static final long serialVersionUID = 1L;

    public static WorkItemStateModel create( WorkItemState woit ){
        WorkItemStateModel model = new WorkItemStateModel();
        try{
            BeanUtils.copyProperties( model, woit );
        }
        catch( IllegalAccessException | InvocationTargetException e ){
            throw new RuntimeException( "Error creating model", e );
        }
        return model;
    }

    public String getDueDateText(){
        return DateUtil.formatDate( getDueDate() );
    }

    public String getDateCreatedText(){
        return DateUtil.formatDate( getDateCreated() );
    }

    public String getDateUpdatedText(){
        return DateUtil.formatDate( getDateUpdated() );
    }

    public boolean isDueDateInFuture(){
        return getDueDate().after( new Date() );
    }

}