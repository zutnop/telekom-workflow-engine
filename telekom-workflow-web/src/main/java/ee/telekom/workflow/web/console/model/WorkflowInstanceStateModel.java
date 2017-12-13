package ee.telekom.workflow.web.console.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.executor.marshall.TokenState;
import ee.telekom.workflow.facade.model.WorkflowInstanceFacadeStatus;
import ee.telekom.workflow.facade.model.WorkflowInstanceState;
import ee.telekom.workflow.facade.util.DateUtil;
import ee.telekom.workflow.facade.util.HistoryUtil;
import ee.telekom.workflow.facade.util.StatusUtil;
import ee.telekom.workflow.util.JsonUtil;

public class WorkflowInstanceStateModel extends WorkflowInstanceState implements Serializable{

    private static final long serialVersionUID = 1L;
    private static final Gson gson = new GsonBuilder().serializeNulls().create();
    private String keepHistory;

    public static WorkflowInstanceStateModel create( WorkflowInstanceState woin ){
        WorkflowInstanceStateModel model = new WorkflowInstanceStateModel();
        try{
            BeanUtils.copyProperties( model, woin );
        }
        catch( IllegalAccessException | InvocationTargetException e ){
            throw new RuntimeException( "Error creating model", e );
        }
        return model;
    }

    public WorkflowInstanceFacadeStatus getFacadeStatus(){
        return StatusUtil.toFacade( WorkflowInstanceStatus.valueOf( getStatus() ) );
    }

    public String getDateCreatedText(){
        return DateUtil.formatDate( getDateCreated() );
    }

    public String getDateUpdatedText(){
        return DateUtil.formatDate( getDateUpdated() );
    }

    public Collection<TokenState> getTokenList(){
        if( getState() == null ){
            return Collections.emptyList();
        }
        return JsonUtil.deserializeCollection( getState(), ArrayList.class, TokenState.class );
    }

    public List<Pair<String, String>> getAttributeList(){
        if( getAttributes() == null ){
            return Collections.emptyList();
        }
        JsonParser parser = new JsonParser();
        JsonElement attributesObject = parser.parse( getAttributes() );
        List<Pair<String, String>> result = new ArrayList<>();
        for( Entry<String, JsonElement> attribute : attributesObject.getAsJsonObject().entrySet() ){
            result.add( Pair.of( attribute.getKey(), gson.toJson( attribute.getValue() ) ) );
        }
        return result;
    }

    public List<String> getExecutionSteps(){
        return HistoryUtil.getExecutionSteps( getHistory() );
    }

	public String getKeepHistory() {
		return keepHistory;
	}

	public void setKeepHistory(String keepHistory) {
		this.keepHistory = keepHistory;
	}
}