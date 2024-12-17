package mipt.hw2;

//import mipt.hw2.Interceptors.StaticSetterInterceptor;
import mipt.hw2.Interceptors.StaticClassVariables;
import mipt.hw2.Interceptors.StaticSetterAdvice;
import mipt.hw2.Interceptors.StaticSetterAdvice;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import net.bytebuddy.agent.ByteBuddyAgent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import mipt.hw2.annotations.Cache;
import mipt.hw2.annotations.Setter;
import mipt.hw2.Interceptors.CacheInterceptor;
import mipt.hw2.Interceptors.InterceptorHandler;
import mipt.hw2.Interceptors.SetterInterceptor;


public class CacheHandler {
    ////////////////////////////////////////////////////////////////////////////
    ////////                           API                              ////////
    ////////////////////////////////////////////////////////////////////////////

    public static <T> T cache(Class<T> delegateClass) {
        return cache(delegateClass, new Object[]{}, new Class<?>[]{});
    }

    public static <T> T cache(Class<T> delegateClass,
                              Object[] args,
                              Class<?>[] argsClasses) {
        AnnotatedMethods<T> methods = new AnnotatedMethods<>(delegateClass);
//        redefineAnnotatedStaticMethods(delegateClass, methods);
        Class<?> staticClass = getStaticCounterClass(delegateClass);
        StaticClassVariables.staticClassMapper.put(delegateClass, staticClass);
        Class<?> proxyClass = buildProxyClass(delegateClass, methods, args, argsClasses);
        Constructor<?> proxyCtor = getProxyConstructor(proxyClass);
        return getProxyInstance(proxyCtor, staticClass);
    }


    ////////////////////////////////////////////////////////////////////////////
    ////////                       Internals.                           ////////
    ////////////////////////////////////////////////////////////////////////////



    ////////////////////////////////////////////////////////////////////////////
    /**
     * BuildProxyClass
     */

    private static <T> Class<?> buildProxyClass(Class<T> delegateClass,
                                                AnnotatedMethods<T> methods,
                                                Object[] args,
                                                Class<?>[] argsClasses) {
        // Builder for class load.
        ReceiverTypeDefinition<T> builder =
                BuilderCreator.create(delegateClass, args, argsClasses);

        // Add methods for intercept to subclass.
        builder = addInterceptions(builder, methods.setters, SetterInterceptor.class);
        builder = addInterceptions(builder, methods.caches, CacheInterceptor.class);

        // Load subclass.
        return builder.make()
                .load(delegateClass.getClassLoader(),
                        ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
    }

    private static <T> Class<?> getStaticCounterClass(Class<T> delegateClass) {
        Class<?> staticClass = staticClassMap.get(delegateClass);
        if (staticClass == null) {
            staticClass = staticClassMap
                    .computeIfAbsent(delegateClass, k -> new ByteBuddy()
                            .subclass(Object.class)
                            .defineField("commonCounter",
                                    Long.class,
                                    Visibility.PUBLIC,
                                    Ownership.STATIC)
                            .make()
                            .load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                            .getLoaded());
        }
        try {
            staticClass.getField("commonCounter").set(null, 0L);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return staticClass;
    }

//    private static <T> void redefineAnnotatedStaticMethods(
//            Class<T> delegateClass,
//            AnnotatedMethods<T> methods) {
//
//        redefineMethods(delegateClass, methods.staticSetters, StaticSetterAdvice.class);
////        redefineMethods(delegateClass, methods.staticCaches, StaticCacheInterceptor.class);
//    }

//    private static <T> void redefineMethods(
//            Class<T> delegateClass,
//            List<Method> methods,
//            Class<?> adviceClass) {
//        for (Method m : methods) {
//            System.out.println("redefineMethod: " + m);
//            ByteBuddyAgent.install();
//            new ByteBuddy()
//                    .redefine(delegateClass)
//                    .visit(Advice.to(adviceClass).on(ElementMatchers.anyOf(m)))
//                    .make()
//                    .load(PrimitiveStatic.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
//
////            ByteBuddyAgent.install();
////            new ByteBuddy()
////                    .redefine(delegateClass)
////                    .method(ElementMatchers.anyOf(m))
////                    .intercept(MethodDelegation.to(StaticSetterInterceptor.class))
////                    .make()
////                    .load(
////                            delegateClass.getClassLoader(),
////                            ClassReloadingStrategy.fromInstalledAgent());
//
////            new ByteBuddy()
////                    .redefine(delegateClass)
////                    .method(ElementMatchers.anyOf(m))
////                    .intercept(MethodDelegation.to(interceptorClass))
////                    .make()
////                    .load(delegateClass.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
//
////                         ClassFileLocator.ForClassLoader.ofSystemLoader()
////                new ByteBuddy()
////                        .redefine(delegateClass, ClassFileLocator.ForClassLoader.ofClassPath())
////                        .method(ElementMatchers.anyOf(m)).intercept(MethodDelegation.to(interceptorClass))
////                        .make()
////                        .load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.INJECTOR);
//        }
//    }

    private static Constructor<?> getProxyConstructor(Class<?> proxyClass) {
        Constructor<?> proxyCtor;
        try {
            proxyCtor = proxyClass.getDeclaredConstructor(InterceptorHandler.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("""
                    Unable to find constructor of proxy class.
                    """ + e);
        }

        return proxyCtor;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getProxyInstance(Constructor<?> proxyCtor,
                                          Class<?> staticClass) {
        T proxy;
        try {
            proxy = (T) proxyCtor.newInstance(new InterceptorHandler(staticClass));
        } catch (InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e) {
            throw new RuntimeException("""
                    Unable to create instance of subclass.
                    """ + e);
        }

        setCommonCounterToProxy(staticClass, proxy);

        return proxy;
    }

    private static <T> void setCommonCounterToProxy(Class<?> staticClass,
                                                    T proxy) {
        Object commonCounter;
        try {
            commonCounter = staticClass
                    .getField("commonCounter")
                    .get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        try {
            proxy.getClass().getField("commonCounter").set(proxy, commonCounter);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    /**
     * BuilderCreator.
     */

    private static class BuilderCreator {
        public static <T> ReceiverTypeDefinition<T> create(
                Class<T> delegateClass,
                Object[] args,
                Class<?>[] argsClasses) {
            DynamicType.Builder<T> subclass = createSubclass(delegateClass);
            return addConstructor(subclass, delegateClass, args, argsClasses);
        }

        private static <T> DynamicType.Builder<T> createSubclass(
                Class<T> delegateClass) {
            return new ByteBuddy().subclass(delegateClass);
        }

        private static <T> ReceiverTypeDefinition<T> addConstructor(
                DynamicType.Builder<T> subclass,
                Class<T> delegateClass,
                Object[] args,
                Class<?>[] argsClasses) {
            // Get constructor.
            Constructor<?> ctor = null;
            try {
                ctor = delegateClass.getDeclaredConstructor(argsClasses);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("""
                        Unable to find constructor with given argsClasses.
                        """ + e);
            }

            return subclass
                    .defineField("handler",
                            InterceptorHandler.class,
                            Visibility.PUBLIC)
                    .defineField("localCounter", // for staticMethods
                            long.class,
                            Visibility.PUBLIC)
                    .value(0L)
                    .defineField("commonCounter",
                            Long.class,
                            Visibility.PUBLIC,
                            Ownership.STATIC)
                    .defineConstructor(Visibility.PUBLIC)
                    .withParameters(InterceptorHandler.class)
                    .intercept(MethodCall
                            .invoke(ctor)
                            .with(args)
                            .andThen(FieldAccessor.ofField("handler")
                                    .setsArgumentAt(0)));
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * AncestorsGetter.
     */

    private static class AncestorsGetter {
        public static Stream<Class<?>> getAllInterfaces(Class<?> clazz) {
            return Stream.of(clazz.getInterfaces())
                    .flatMap(interfaceType -> Stream
                            .concat(Stream.of(interfaceType),
                                    getAllInterfaces(interfaceType)));
        }

        public static Stream<Class<?>> getAll(Class<?> clazz) {
            if (clazz == null) {
                return Stream.empty();
            }
            Stream<Class<?>> res = Stream
                    .concat(Stream.of(clazz),
                            getAllInterfaces(clazz));
            clazz = clazz.getSuperclass();
            while (clazz != null) {
                res = Stream.concat(res, Stream.of(clazz));
                res = Stream.concat(res, getAllInterfaces(clazz));
                clazz = clazz.getSuperclass();
            }
            return res;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * AnnotatedMethods.
     */

    private static class AnnotatedMethods<T> {
        AnnotatedMethods(Class<T> delegateClass) {
            Stream<Method> ancestorsMethods = AncestorsGetter
                    .getAll(delegateClass)
                    .flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()));

            CreateEqualMethodsMap(ancestorsMethods);

//            FillListsWithAnnotatedMethods();

//            try {
//                methodTestList = new ArrayList<>(List.of(delegateClass.getDeclaredMethod("commonCache")));
//            } catch (NoSuchMethodException e) {
//                throw new RuntimeException("no such method" + e);
//            }
        }

        private void CreateEqualMethodsMap(Stream<Method> ancestorsMethods) {
            ancestorsMethods.forEach(method -> {
                if (method.isAnnotationPresent(Setter.class)
                        && method.isAnnotationPresent(Cache.class)) {
                    throw new RuntimeException("""
                        It is prohibited for method to have \
                        both Setter and Cache annotation.
                        """);
                }
//                List<Object> key = KeyForMethod
//                        .Generate(method, method.getParameterTypes());
//                if (equalMethodsMap.containsKey(key)) {
//                    equalMethodsMap.get(key).add(method);
//                } else {
//                    equalMethodsMap.put(key, new ArrayList<>(List.of(method)));
//                }
                if (method.isAnnotationPresent(Setter.class)) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        staticSetters.add(method);
                    } else {
                        setters.add(method);
                    }
                }
                if (method.isAnnotationPresent(Cache.class)) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        staticCaches.add(method);
                    } else {
                        caches.add(method);
                    }
                }
            });
        }

//        private void FillListsWithAnnotatedMethods() {
//            for (var entry : equalMethodsMap.entrySet()) {
//                boolean isSetter = true;
//                boolean isCache = true;
//                for (Method method: entry.getValue()) {
//                    isSetter &= method.isAnnotationPresent(Setter.class);
//                    isCache &= method.isAnnotationPresent(Cache.class);
//                }
//                if (isSetter) {
//                    setters.add(entry.getValue().get(0));
//                } else if (isCache) {
//                    caches.add(entry.getValue().get(0));
//                }
//            }
//        }

//        private final Map<List<Object>, List<Method>> equalMethodsMap = new HashMap<>();

        public List<Method> setters = new ArrayList<>();
        public List<Method> caches = new ArrayList<>();
        public List<Method> staticSetters = new ArrayList<>();
        public List<Method> staticCaches = new ArrayList<>();
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * addInterceptions.
     */

    private static <T> ReceiverTypeDefinition<T> addInterceptions(
            ReceiverTypeDefinition<T> builder,
            List<Method> methods,
            Class<?> interceptorClass) {
        for (Method m: methods) {
            System.out.println(m);
            builder = builder
                    .method(ElementMatchers.anyOf(m))
                    .intercept(MethodDelegation.to(interceptorClass));
        }

//        for (Method method: methods) {
//            builder = builder
//                    .method(ElementMatchers
//                            .named(method.getName())
//                            .and(ElementMatchers.takesArguments(method
//                                    .getParameterTypes())))
//                    .intercept(MethodDelegation.to(interceptorClass));
//        }

//        // Version with binders.
//        builder = builder.method(ElementMatchers.anyOf(setters)).intercept(MethodDelegation
////                            .withDefaultConfiguration()
////                            .withBinders(FieldProxy.Binder.install(InterceptorHandler.class))
//                .to(CacheInterceptor.class));

        return builder;
    }

    private static final Map<Class<?>, Class<?>> staticClassMap = new HashMap<>();
}


//class StaticSetterInterceptor {
//    @RuntimeType
//    static String intercept() {
//        return "Intercepted!!!";
//    }
//}
