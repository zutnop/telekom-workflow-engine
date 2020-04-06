package ee.telekom.workflow.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.aop.framework.Advised;

import ee.telekom.workflow.graph.WorkflowException;


public class CallUtil {

    public static Object call(Object target, String methodName, Object[] arguments) {
        Method method = MethodUtil.findMethod(target.getClass(), methodName, MethodUtil.getArgumentClasses(arguments));
        String className = getTargetName(target);
        try {
            return method.invoke(target, arguments);
        } catch (IllegalAccessException e) {
            throw new WorkflowException("Invoking method '" + method.getName() + "' on class '" + className + "' failed", e);
        } catch (IllegalArgumentException e) {
            throw new WorkflowException("Invoking method '" + method.getName() + "' on class '" + className + "' failed", e);
        } catch (InvocationTargetException e) {
            throw new WorkflowException("Invoking method '" + method.getName() + "' on class '" + className + "' failed", e);
        }
    }

    private static String getTargetName(final Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            if (target instanceof Advised) {
                try {
                    return ((Advised)target).getTargetSource().getTarget().getClass().getName();
                } catch (Exception e) {
                    // Nothing to do here.
                }
            }
            return "Unresolved classname from proxy class (" + target.getClass().getName() + ")";
        }
        return target.getClass().getName();
    }

}
