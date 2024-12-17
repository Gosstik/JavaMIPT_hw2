package mipt.hw2.Interceptors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InterceptorHandler {
    ////////////////////////////////////////////////////////////////////////////
    ////////                           API                              ////////
    ////////////////////////////////////////////////////////////////////////////

    public InterceptorHandler(Class<?> staticClassCounter) {
        this.staticClassCounter = staticClassCounter;
    }

    public void handleSetter() {
        cacheMap.clear();
    }

    public Object handleCache(Object object, Method method, Object[] args) {
        List<Object> key = toKey(method, args);
        return cacheMap.computeIfAbsent(key, k -> {
            try {
                return method.invoke(object, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////                       Internals.                           ////////
    ////////////////////////////////////////////////////////////////////////////

    private static List<Object> toKey(Method method, Object[] args) {
        List<Object> key = new ArrayList<>();
        key.add(method.getName());
//        key.add(method);
        key.addAll(Arrays.asList(args));
        return key;
    }

    ////////////////////////////////////////////////////////////////////////////
    //////// Class members.

    private final Map<List<Object>, Object> cacheMap = new HashMap<>();
    Class<?> staticClassCounter;
}
