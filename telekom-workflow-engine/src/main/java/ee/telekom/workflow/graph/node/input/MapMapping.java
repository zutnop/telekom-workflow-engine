package ee.telekom.workflow.graph.node.input;

import java.util.HashMap;
import java.util.Map;

import ee.telekom.workflow.graph.GraphInstance;

/**
 * InputMapping that evaluates to a Map. The class is mainly intended to created argument maps for human task activities.
 */
public class MapMapping implements InputMapping<Map<String, Object>>{

    private Map<String, InputMapping<Object>> entryMappings;

    public MapMapping(){
        this( new HashMap<String, InputMapping<Object>>() );
    }

    public MapMapping( Map<String, InputMapping<Object>> entryMappings ){
        this.entryMappings = entryMappings;
    }

    public Map<String, InputMapping<Object>> getEntryMappings(){
        return entryMappings;
    }

    public void addEntryMapping( String name, InputMapping<Object> inputMapping ){
        entryMappings.put( name, inputMapping );
    }

    @Override
    public Map<String, Object> evaluate( GraphInstance instance ){
        if( entryMappings == null ){
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        for( Map.Entry<String, InputMapping<Object>> entry : entryMappings.entrySet() ){
            map.put( entry.getKey(), entry.getValue().evaluate( instance ) );
        }
        return map;
    }

}