package mipt.hw2.Interceptors;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


//public class StaticSetterInterceptor implements StaticInterceptor {
//    @RuntimeType
//    static Object intercept(@Origin Method method,
//                            @AllArguments Object[] args) throws Throwable {
////        System.out.println("Intercepted!!!!!!");
//        return "Intercepted!!!!!!";
////        Field commonCounter = method.getDeclaringClass().getField("commonCounter");
////        long currentValue = commonCounter.getLong(null);
////        commonCounter.set(null, currentValue + 1);
////
////        return method.invoke(args);
//    }
//}


//public class StaticSetterInterceptor {
//    @RuntimeType
//    static String intercept() {
//        return "Intercepted!!!";
//    }
//}

//public class StaticSetterInterceptor implements StaticInterceptor {
//    @Setter
//    static void staticCacheMethod() {
//        System.out.println("inside staticCacheMethod");
//    }
//}

//public class StaticSetterInterceptor {
//
//    @RuntimeType
//    public static Object intercept(@SuperMethod Method method, @AllArguments Object[] args) {
//        for (Object arg: args) {
//            System.out.println(arg);
//        }
//        System.out.println("Intercepted!!!!!!!!!!!!!");
//        return "great";
//    }
//}


public class StaticSetterAdvice {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    static void enter(@Advice.Origin Method method,
                      @Advice.AllArguments Object[] ary) {
        Class<?> clazz = method.getDeclaringClass();
        Class<?> commonCounterClass = StaticClassVariables.staticClassMapper.get(clazz);
        if (commonCounterClass == null) {
            return;
        }
        try {
            Field commonCounterField = commonCounterClass.getDeclaredField("commonCounter");
            commonCounterField.setAccessible(true);
            Long commonValue = (Long) commonCounterField.get(null);
            commonCounterField.set(null, commonValue + 1);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
    static void exit(@Advice.Return Object res) {
//        System.out.println("inside exit: " + res);
        System.out.println("Inside exit method . . .");
    }
}


//public class StaticSetterAdvice {
//    @Advice.OnMethodEnter(suppress = Throwable.class)
//    static void enter(@Advice.FieldValue(value = "class", readOnly = false) Class<?> clazz,
//                      @Advice.Origin Method method,
//                      @Advice.AllArguments Object[] ary) {
////        commonCounter += 1;
//    }
//
//    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
//    static void exit(@Advice.Return Object res) {
////        System.out.println("inside exit: " + res);
//        System.out.println("Inside exit method . . .");
//    }
//}
