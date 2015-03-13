package ee.telekom.workflow.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;

import ee.telekom.workflow.graph.WorkflowException;

public class MethodUtil{

    public static Class<?>[] getArgumentClasses( Object[] arguments ){
        if( arguments == null || arguments.length == 0 ){
            return new Class<?>[0];
        }
        else{
            Class<?>[] argumentClasses = new Class[arguments.length];
            for( int i = 0; i < arguments.length; i++ ){
                argumentClasses[i] = arguments[i] == null ? null : arguments[i].getClass();
            }
            return argumentClasses;
        }
    }

    public static Method findMethod( Class<?> clazz, String methodName, Class<?>[] argumentClasses ) throws SecurityException{
        List<Method> candidates = new ArrayList<>();

        for( Method method : clazz.getMethods() ){
            if( !Modifier.isPublic( method.getModifiers() )
                    || !method.getName().equals( methodName )
                    || !hasAssignableArguments( method, argumentClasses, false ) ){
                continue;
            }
            candidates.add( method );
        }

        if( candidates.size() == 0 ){
            throw new WorkflowException( "No suitable method " + methodName
                    + " found on target of class " + clazz.getName()
                    + " with arguments " + Arrays.toString( argumentClasses ) );
        }
        else if( candidates.size() > 1 ){

            for( Method method : candidates ){
                if( hasAssignableArguments( method, argumentClasses, true ) ){
                    return method;
                }
            }

            throw new WorkflowException( "Multiple suitable methods " + methodName
                    + " found on target of class " + clazz.getName()
                    + " with arguments " + Arrays.toString( argumentClasses ) );
        }
        else{
            return candidates.get( 0 );
        }
    }

    private static boolean hasAssignableArguments( Method method, Class<?>[] argumentClasses, boolean strict ){
        Class<?>[] parameterClasses = method.getParameterTypes();
        if( parameterClasses.length == argumentClasses.length ){
            return hasAssignableArgumentsWithoutVarArgs( parameterClasses, argumentClasses, strict );
        }
        else if( method.isVarArgs() ){
            return hasAssignableArgumentsWithVarArgs( parameterClasses, argumentClasses, strict );
        }
        return false;
    }

    private static boolean hasAssignableArgumentsWithoutVarArgs( Class<?>[] parameterClasses, Class<?>[] argumentClasses, boolean strict ){
        for( int i = 0; i < argumentClasses.length; i++ ){
            if( argumentClasses[i] != null ){
                if( !isAssignable( parameterClasses[i], argumentClasses[i], strict ) ){
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasAssignableArgumentsWithVarArgs( Class<?>[] parameterClasses, Class<?>[] argumentClasses, boolean strict ){
        for( int i = 0; i < parameterClasses.length - 1; i++ ){
            if( argumentClasses[i] != null ){
                if( !isAssignable( parameterClasses[i], argumentClasses[i], strict ) ){
                    return false;
                }
            }
        }
        Class<?> varArgArrayComponentType = parameterClasses[parameterClasses.length - 1].getComponentType();
        for( int i = parameterClasses.length; i < argumentClasses.length; i++ ){
            if( !isAssignable( varArgArrayComponentType, argumentClasses[i], strict ) ){
                return false;
            }
        }
        return true;
    }

    private static boolean isAssignable( Class<?> parameterClass, Class<?> argumentClass, boolean strict ){
        return strict ? parameterClass.equals( argumentClass ) : ClassUtils.isAssignable( argumentClass, parameterClass, !strict );
    }

}
