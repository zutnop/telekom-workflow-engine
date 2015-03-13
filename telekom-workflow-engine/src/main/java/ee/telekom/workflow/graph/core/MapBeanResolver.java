package ee.telekom.workflow.graph.core;

import java.util.HashMap;
import java.util.Map;

import ee.telekom.workflow.graph.BeanResolver;

public class MapBeanResolver implements BeanResolver{

    private Map<String, Object> beans = new HashMap<>();

    @Override
    public Object getBean( String name ){
        return beans.get( name );
    }

    public void addBean( String name, Object bean ){
        beans.put( name, bean );
    }

}
