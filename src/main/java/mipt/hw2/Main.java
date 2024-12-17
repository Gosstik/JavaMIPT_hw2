package mipt.hw2;

import mipt.hw2.annotations.Cache;
import mipt.hw2.annotations.Setter;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldProxy;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.IgnoreForBinding;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.Pipe;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.named;

//class A {
//    String aString() {
//        return "A: aString";
//    }
//}
//
//class B extends A {
//    B() {
//        System.out.println("B: constructor");
//    }
//
//    int bValue = 10;
//
//    String bString() {
//        return aString();
//    }
//
//    String anotherBString() {
//        return "B: another B string";
//    }
//}

////////////////////////////////////////////////////////////////////////////////

interface I1 {
    int i1 = 0;

    @Cache
    default void I1Func() {
        System.out.println("I1: I1Func with cache");
    }
}

interface I1Child extends I1 {
    @Override
    default void I1Func() {
        System.out.println("I1Child: I1Func without cache");
        I1.super.I1Func();
    }
}

interface I2 {
    int i2 = 0;
}

interface I3 {
    int i3 = 0;
}

class A {
    int a = 0;
    String commonString = "common string";

    void aMethod() {

    }

    @Cache
    String commonCache() {
        return "A: " + commonString;
    }

    @Setter
    void commonSetter(String str) {
        System.out.println("A: commonSetter");
        commonString = str;
    }

    @Setter
    void aSetter() {

    }

    @Cache
    void aCache() {

    }

    @Cache
    void aOnlyCache() {

    }

    @Cache
    void aAndCCache() {
        System.out.println("A: aAndCCache");
    }
}

class B extends A implements I1, I2 {
    int b = 0;

    void bMethod() {

    }

    @Override
    @Cache
    String commonCache() {
        Method currentMethod = new Object(){}.getClass().getEnclosingMethod();
//        System.out.println("---annotations---");
//        for (Annotation annot: currentMethod.getDeclaredAnnotations()) {
//            System.out.println(annot);
//        }
//        System.out.println("------");
//        System.out.println(currentMethod);
        System.out.println("In B");
        return "B: " + commonString;
    }

    @Override
    @Setter
    void commonSetter(String str) {
        System.out.println("B: commonSetter");
        commonString = str;
    }

    @Setter
    void bSetter() {

    }

    @Cache
    void bCache() {

    }

    @Override
    void aOnlyCache() {
        System.out.println("B: aOnlyCache");
    }

    @Override
    void aAndCCache() {
        System.out.println("B: aAndCCache");
    }
}

class C extends B implements I3 {
    int c = 0;

    void cMethod() {

    }

    @Override
    @Cache
    String commonCache() {
        super.commonCache();
        return "C: " + commonString;
    }

    @Override
    @Setter
    void commonSetter(String str) {
        System.out.println("C: commonSetter");
        commonString = str;
    }


    @Setter
    void cSetter() {

    }

    @Cache
    void cCache() {

    }

    @Override
    @Cache
    void aAndCCache() {
        System.out.println("C: aAndCCache");
    }
}

class WithoutDefaultConstructor {
    int value = 0;

    WithoutDefaultConstructor(int a) {
        value = a;
    }

    WithoutDefaultConstructor(Integer a) {
        value = 2 * a;
    }

    @Setter
    public static Long staticCacheMethod() {
        System.out.println("inside staticCacheMethod");
        return 0L;
    }

//    WithoutDefaultConstructor(Integer a) {
//        value = a;
//    }
}

class TypesTester {
    static void test(Object a, Object b) {
        System.out.println(Object.class.isPrimitive());
        System.out.println(b.getClass().isPrimitive());
    }
}

////////////////////////////////////////////////////////////////////////////////

class PrimitiveStatic {
    static String name = "Marusya";
    public String notStaticFunction(String res1, String res2) {
        return res1 + " + " + res2;
    }

    @Setter
    public static String isStaticFunction(String res1, String res2) {
        return res1 + " + " + res2;
//        return "isStaticFunction";
    }
}

class Foo {
    public String sayHelloFoo() {
        return "Hello in Foo!";
    }
}

class Delegator {
    @RuntimeType
    static String intercept(@Pipe Object self, @AllArguments Object[] objects) {
        return "Intercepted!";
    }
}


class MyAdvices {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    static void enter(@Advice.AllArguments Object[] ary, @Advice.FieldValue(value = "name", readOnly = false) String nameField){
        System.out.println("NAME===========" + nameField);
        System.out.println("Inside enter method . . .  ");
//
//        if(ary != null) {
//            for(int i =0 ; i < ary.length ; i++){
//                System.out.println("Argument: " + i + " is " + ary[i]);
//            }
//        }
//
//        System.out.println("Origin :" + origin);
//        System.out.println("Detailed Origin :" + detaildOrigin);
//
//        nameField = "Jack";
//        return System.nanoTime();
    }

    @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
    static void exit(@Advice.Return Object res) {
        System.out.println("inside exit: " + res);
        System.out.println("Inside exit method . . .");
    }
}





public class Main {
    public static void main(String[] args) {
//        C c = CacheHandler.cache(C.class);
//        System.out.println(c.commonCache());

//        object.getClass().isPrimitive();
//        System.out.println(Integer.TYPE);

//        Class<?> clazz = WithoutDefaultConstructor.class;
//        Constructor<?> ctor = clazz.getDeclaredConstructor(int.get);
//        System.out.println(ctor);

//        System.out.println(int.class);
//        System.out.println(Integer.class);


//        ByteBuddyAgent.install();
//        new ByteBuddy()
//                .redefine(PrimitiveStatic.class)
//                .method(named("notStaticFunction"))
//                .intercept(FixedValue.value("Hello Foo Redefined"))
//                .make()
//                .load(
//                        PrimitiveStatic.class.getClassLoader(),
//                        ClassReloadingStrategy.fromInstalledAgent());
//
//        PrimitiveStatic f = new PrimitiveStatic();
//        System.out.println(f.notStaticFunction());


//        ByteBuddyAgent.install();
//        new ByteBuddy()
//                .rebase(PrimitiveStatic.class)
//                .method(named("isStaticFunction"))
//                .intercept(MethodDelegation.to(Delegator.class))
//                .make()
//                .load(
//                        PrimitiveStatic.class.getClassLoader(),
//                        ClassReloadingStrategy.fromInstalledAgent());
//
//        PrimitiveStatic p = new PrimitiveStatic();
//        System.out.println(PrimitiveStatic.isStaticFunction("res1", "res2"));

//        ByteBuddyAgent.install();
//        new ByteBuddy()
//                .redefine(PrimitiveStatic.class)
//                .visit(Advice.to(MyAdvices.class).on(ElementMatchers.isMethod()))
//                .make()
//                .load(PrimitiveStatic.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
//
//        PrimitiveStatic p = new PrimitiveStatic();
//        System.out.println(p.notStaticFunction("res1", "res2"));

        CacheHandler.cache(PrimitiveStatic.class);
        System.out.println(PrimitiveStatic.isStaticFunction("res1", "res2"));


//        WithoutDefaultConstructor obj = CacheHandler.cache(WithoutDefaultConstructor.class, new Object[]{1}, new Class<?>[]{Integer.class});
//        System.out.println(obj.value);
//        obj.staticCacheMethod();
    }

}
