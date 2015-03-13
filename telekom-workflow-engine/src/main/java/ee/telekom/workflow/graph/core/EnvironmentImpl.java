package ee.telekom.workflow.graph.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ee.telekom.workflow.graph.Environment;
import ee.telekom.workflow.graph.el.ElUtil;

public class EnvironmentImpl implements Environment{

    private Map<String, Object> attributes = new HashMap<String, Object>();

    @Override
    public Object getAttribute( String name ){
        return attributes.get( name );
    }

    @Override
    public void removeAttribute( final String name ){
        attributes.remove( name );
    }

    @Override
    public void setAttribute( String name, Object value ){
        if( ElUtil.isReservedVariable( name ) ){
            throw new IllegalArgumentException( "Variable '" + name + "' is a reserved keyword and SHOULD NOT be used as a key for an Environment attribute." );
        }
        attributes.put( name, value );
    }

    @Override
    public void importEnvironment( Environment environment ){
        for( String name : environment.getAttributeNames() ){
            setAttribute( name, environment.getAttribute( name ) );
        }
    }

    @Override
    public Iterable<String> getAttributeNames(){
        return Collections.unmodifiableSet( attributes.keySet() );
    }

    @Override
    public boolean containsAttribute( String name ){
        return attributes.containsKey( name );
    }

    @Override
    public Map<String, Object> getAttributesAsMap(){
        return Collections.unmodifiableMap( attributes );
    }

    @Override
    public void clear(){
        attributes.clear();
    }

    public Map<String, Object> getAttributes(){
        return attributes;
    }

    public void setAttributes( Map<String, Object> attributes ){
        this.attributes = attributes;
    }

}