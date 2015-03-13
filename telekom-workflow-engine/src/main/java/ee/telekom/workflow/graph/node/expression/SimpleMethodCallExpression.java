package ee.telekom.workflow.graph.node.expression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ee.telekom.workflow.graph.WorkflowException;
import ee.telekom.workflow.util.MethodUtil;

/**
 * {@link Expression} implementation that executes a method with given name on a given object.
 */
public class SimpleMethodCallExpression<T> implements Expression<T>{

    private Object target;
    private String methodName;

    public SimpleMethodCallExpression( Object target, String methodName ){
        this.target = target;
        this.methodName = methodName;
    }

    public Object getTarget(){
        return target;
    }

    public String getMethodName(){
        return methodName;
    }

    @Override
    public T execute( Object... arguments ){
        Method method = getMethod( target.getClass(), arguments );
        try{
            @SuppressWarnings("unchecked")
            T result = (T)method.invoke( target, arguments );
            return result;
        }
        catch( IllegalAccessException e ){
            throw new WorkflowException( "Invoking method '" + method.getName() + "' on class '" + target.getClass().getName() + "' failed", e );
        }
        catch( IllegalArgumentException e ){
            throw new WorkflowException( "Invoking method '" + method.getName() + "' on class '" + target.getClass().getName() + "' failed", e );
        }
        catch( InvocationTargetException e ){
            throw new WorkflowException( "Invoking method '" + method.getName() + "' on class '" + target.getClass().getName() + "' failed", e );
        }
    }

    private Method getMethod( Class<?> clazz, Object... arguments ){
        return MethodUtil.findMethod( clazz, methodName, MethodUtil.getArgumentClasses( arguments ) );
    }

}
