package mipt.hw2.Interceptors;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Empty;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;

public interface StaticInterceptor {
    @RuntimeType
    static Object intercept(@Origin Method method,
                            @AllArguments Object[] args,
                            @SuperMethod(nullIfImpossible = true) Method superMethod,
                            @Empty Object defaultValue) throws Throwable {
        if (superMethod == null) {
            System.out.println("SUPER METHOD = NULL");
            return defaultValue;
        }

        return method.invoke(args);
    }
}


