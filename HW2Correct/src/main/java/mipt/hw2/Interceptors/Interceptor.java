package mipt.hw2.Interceptors;

import mipt.hw2.annotations.Cache;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Empty;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;

//public class Interceptor {
//    @RuntimeType
//    public static Object intercept(@This Object self,
//                                   @Origin Method method,
//                                   @AllArguments Object[] args,
//                                   @SuperMethod Method superMethod) throws Throwable {
//        System.out.println("Intercepted");
//        return superMethod.invoke(self, args);
//    }
//}

//public class Interceptor {
//    @RuntimeType
//    public static Object intercept(@This Object self,
//                                   @Origin Method method,
//                                   @AllArguments Object[] args,
//                                   @SuperMethod(nullIfImpossible = true) Method superMethod,
//                                   @Empty Object defaultValue,
//                                   @FieldValue(value = "handler", declaringType = InterceptorHandler.class) InterceptorHandler handler) throws Throwable {
//        if (superMethod == null) {
//            System.out.println("SUPER METHOD = NULL");
//            return defaultValue;
//        }
//
//        System.out.println("Intercepted");
//        return superMethod.invoke(self, args);
////        return handler.HandleCall(self, superMethod, args);
//
////        System.out.println("Intercepted");
////        return superMethod.invoke(self, args);
//    }
//}


public interface Interceptor {
    @RuntimeType
    static Object intercept(@This Object self,
                            @Origin Method method,
                            @AllArguments Object[] args,
                            @SuperMethod(nullIfImpossible = true) Method superMethod,
                            @Empty Object defaultValue) throws Throwable {
        if (superMethod == null) {
            System.out.println("SUPER METHOD = NULL");
            return defaultValue;
        }
        return superMethod.invoke(self, args);
    }
}
