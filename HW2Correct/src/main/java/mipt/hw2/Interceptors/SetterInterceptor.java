package mipt.hw2.Interceptors;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Empty;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SetterInterceptor implements Interceptor {
    @RuntimeType
    public static Object intercept(@This Object self,
                                   @Origin Method method,
                                   @AllArguments Object[] args,
                                   @SuperMethod(nullIfImpossible = true) Method superMethod,
                                   @Empty Object defaultValue) throws Throwable {
        if (superMethod == null) {
            System.out.println("SUPER METHOD = NULL");
            return defaultValue;
        }

        Field field = self.getClass().getDeclaredField("handler");
        field.setAccessible(true);
        Object fieldValue = field.get(self);
        Method getStringMethod = fieldValue.getClass().getDeclaredMethod("handleSetter");
        Object invokeRes = getStringMethod.invoke(fieldValue);
        System.out.println(invokeRes);

        System.out.println("Intercepted");
        return superMethod.invoke(self, args);
    }
}
