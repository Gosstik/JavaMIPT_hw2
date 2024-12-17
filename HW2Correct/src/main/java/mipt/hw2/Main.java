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
        return "B: commonCache";
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
        ++c;
        return "str";
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

public class Main {
    public static void main(String[] args) {
        C c = CacheHandler.cache(C.class);
        System.out.println(c.commonCache());
        System.out.println(c.commonCache());
        System.out.println(c.commonCache());
        System.out.println(c.commonCache());
        System.out.println(c.commonCache());
        System.out.println("number of calls = " + c.c);
    }
}
