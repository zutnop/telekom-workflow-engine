package ee.telekom.workflow.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ee.telekom.workflow.graph.WorkflowException;

public class CallUtil{

    public static Object call( Object target, String methodName, Object[] arguments ){
        Method method = MethodUtil.findMethod( target.getClass(), methodName, MethodUtil.getArgumentClasses( arguments ) );
        try{
            return method.invoke( target, arguments );
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

}
