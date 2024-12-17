package mipt.hw2;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.jar.asm.Opcodes;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class TestSettingValue {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Class<?> target = new ByteBuddy()
                .subclass(Object.class)
                .name("com.test.MyClass")
                .defineConstructor(Opcodes.ACC_PUBLIC)
                .withParameters(String.class)
                .intercept(MethodCall.invoke(Object.class.getConstructor()).andThen(FieldAccessor.ofField("hello").setsArgumentAt(0)))
                .defineField("hello", String.class, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL).value("world")
                .make()
                .load(ClassLoader.getSystemClassLoader()).getLoaded();

        Object targetObj = target.getConstructor(String.class).newInstance("world");

        Field f = target.getDeclaredField("hello");
        f.setAccessible(true);
        System.out.println(f.get(targetObj));
    }
}
