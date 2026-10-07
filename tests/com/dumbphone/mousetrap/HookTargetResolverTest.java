package com.dumbphone.mousetrap;

import android.content.Context;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class HookTargetResolverTest {
    private static final class Beta7Helper {
        public static boolean f(Context context, String pkg) { return false; }
    }

    private static final class StableHelper {
        public static boolean f(Context context, String pkg) { return false; }
    }

    private static final class WrongSignature {
        public static Boolean f(Context context, String pkg) { return Boolean.FALSE; }
    }

    public static void main(String[] args) {
        Method beta7 = HookTargetResolver.selectUniqueCandidate(
                Arrays.<Class<?>>asList(Beta7Helper.class));
        assertNotNull(beta7, "beta.7-shaped helper must resolve");
        assertTrue(Modifier.isStatic(beta7.getModifiers()), "resolved helper must be static");
        assertEquals("f", beta7.getName(), "resolved beta.7 method");

        Method ambiguous = HookTargetResolver.selectUniqueCandidate(
                Arrays.<Class<?>>asList(Beta7Helper.class, StableHelper.class));
        assertNull(ambiguous, "multiple matching helpers must fail closed");

        Method wrong = HookTargetResolver.selectUniqueCandidate(
                Arrays.<Class<?>>asList(WrongSignature.class));
        assertNull(wrong, "boxed Boolean return must not match");

        List<Class<?>> discovered = HookTargetResolver.collectDexCandidateClasses(
                Collections.enumeration(Arrays.asList(
                        "bc.a1", "bc.b1", "bd.a1")),
                HookTargetResolverTest.class.getClassLoader());
        Method current = HookTargetResolver.selectUniqueCandidate(discovered);
        assertNotNull(current, "current helper must be discovered without a name profile");
        assertEquals("bc.a1", current.getDeclaringClass().getName(),
                "only classes in the bc namespace participate");

        assertThrowsDiscovery(
                Collections.enumeration(Arrays.asList("bc.a1", "bc.missing")),
                "an incomplete namespace scan must fail closed");

        List<Class<?>> inspectionFailure = HookTargetResolver.collectDexCandidateClasses(
                Collections.enumeration(Arrays.asList("bc.a1", "bc.InspectionFailure")),
                HookTargetResolverTest.class.getClassLoader());
        assertEquals(Integer.valueOf(2), Integer.valueOf(inspectionFailure.size()),
                "inspection fixture classes must load before method resolution");
        assertThrowsInspection(inspectionFailure,
                "an incomplete method scan must fail closed");

        List<Class<?>> drifted = HookTargetResolver.collectDexCandidateClasses(
                Collections.enumeration(Arrays.asList("bc.a1", "bc.z0")),
                HookTargetResolverTest.class.getClassLoader());
        assertNull(HookTargetResolver.selectUniqueCandidate(drifted),
                "multiple dynamic candidates must fail closed");

        System.out.println("HookTargetResolverTest: PASS");
    }

    private static void assertNotNull(Object value, String message) {
        if (value == null) throw new AssertionError(message);
    }

    private static void assertNull(Object value, String message) {
        if (value != null) throw new AssertionError(message + ": " + value);
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertThrowsDiscovery(
            java.util.Enumeration<String> classNames, String message) {
        try {
            HookTargetResolver.collectDexCandidateClasses(
                    classNames, HookTargetResolverTest.class.getClassLoader());
        } catch (HookTargetResolver.DiscoveryException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void assertThrowsInspection(List<Class<?>> classes, String message) {
        try {
            HookTargetResolver.selectUniqueCandidate(classes);
        } catch (HookTargetResolver.DiscoveryException expected) {
            return;
        }
        throw new AssertionError(message);
    }
}
