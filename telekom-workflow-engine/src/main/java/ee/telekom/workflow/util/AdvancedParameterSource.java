package ee.telekom.workflow.util;

import java.beans.PropertyDescriptor;
import java.sql.Types;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Like {@link BeanPropertySqlParameterSource}, supports adding beans like
 * {@link MapSqlParameterSource}, supports adding individual values
 * 
 * Encodes Enum's as VARCHAR types Encodes Map's as VARCHAR types
 */
public class AdvancedParameterSource implements SqlParameterSource{

    private final MapSqlParameterSource delegate = new MapSqlParameterSource();

    public AdvancedParameterSource addBean( Object bean ){
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess( bean );
        for( PropertyDescriptor pd : beanWrapper.getPropertyDescriptors() ){
            if( beanWrapper.isReadableProperty( pd.getName() ) ){
                String paramName = pd.getName();
                Object value = beanWrapper.getPropertyValue( paramName );
                addValue( paramName, value );
            }
        }
        return this;
    }

    public AdvancedParameterSource addMapWithLowercaseKeys( Map<String, Object> map ){
        for( Entry<String, Object> entry : map.entrySet() ){
            addValue( entry.getKey().toLowerCase(), entry.getValue() );
        }
        return this;
    }

    public AdvancedParameterSource addValue( String paramName, Object value ){
        if( isBoolean( value ) ){
            delegate.addValue( paramName, YesNoUtil.asString( (Boolean)value ), Types.VARCHAR );
        }
        else if( isEnum( value ) ){
            delegate.addValue( paramName, value, Types.VARCHAR );
        }
        else if( value instanceof Collection ){
            Collection<?> collection = (Collection<?>)value;
            if( !collection.isEmpty() && isEnum( collection.iterator().next() ) ){
                delegate.addValue( paramName, value, Types.VARCHAR );
            }
            else{
                delegate.addValue( paramName, value );
            }
        }
        else{
            delegate.addValue( paramName, value );
        }
        return this;
    }

    @Override
    public int getSqlType( String paramName ){
        return delegate.getSqlType( paramName );
    }

    @Override
    public String getTypeName( String paramName ){
        return delegate.getTypeName( paramName );
    }

    @Override
    public Object getValue( String paramName ){
        return delegate.getValue( paramName );
    }

    @Override
    public boolean hasValue( String paramName ){
        return delegate.hasValue( paramName );
    }

    private boolean isBoolean( Object value ){
        return value != null && value instanceof Boolean;
    }

    private boolean isEnum( Object value ){
        return value != null && value.getClass().isEnum();
    }

}
