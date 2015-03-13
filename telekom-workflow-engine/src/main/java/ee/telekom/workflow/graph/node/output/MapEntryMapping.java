package ee.telekom.workflow.graph.node.output;

import java.util.Map;

import ee.telekom.workflow.graph.Environment;

/**
 * Given a node execution result which is a Map, this {@link OutputMapping} maps each entry of the execution result
 * on an attribute in the {@link Environment}. This {@link OutputMapping} enables to return "multiple values" to the
 * {@link Environment}.
 * <p>
 * If the result object map does not contain an entry for any given key, then this parameter value is set to NULL. 
 */
public class MapEntryMapping implements OutputMapping{

    private Map<String, String> mappings;

    public MapEntryMapping( Map<String, String> mappings ){
        this.mappings = mappings;
    }

    public Map<String, String> getMappings(){
        return mappings;
    }

    @Override
    public void map( Environment environment, Object result ){
        @SuppressWarnings("unchecked")
        Map<String, ?> map = (Map<String, ?>)result;
        for( Map.Entry<String, String> mapping : mappings.entrySet() ){
            String resultKey = mapping.getKey();
            String environmentKey = mapping.getValue();
            if( map.containsKey( resultKey ) ){
                environment.setAttribute( environmentKey, map.get( resultKey ) );
            }
            else{
                environment.setAttribute( environmentKey, null );
            }
        }
    }

}
