package ee.telekom.workflow.graph.el;

import jakarta.el.BeanNameResolver;

import ee.telekom.workflow.graph.Environment;

/**
 * A custom Expression Language 3.0 BeanNameResolver that lives on top of Environment instance. Used when evaluating conditions and attribute expressions.
 *
 * @author Erko Hansar
 */
public class EnvironmentBeanNameResolver extends BeanNameResolver{

    private Environment environment;

    public EnvironmentBeanNameResolver( Environment environment ){
        this.environment = environment;
    }

    @Override
    public boolean isNameResolved( String beanName ){
        return environment.containsAttribute( beanName );
    }

    @Override
    public Object getBean( String beanName ){
        return environment.getAttribute( beanName );
    }

    @Override
    public void setBeanValue( String beanName, Object value ){
        environment.setAttribute( beanName, value );
    }

    @Override
    public boolean isReadOnly( String beanName ){
        return false;
    }

    @Override
    public boolean canCreateBean( String beanName ){
        return true;
    }

}