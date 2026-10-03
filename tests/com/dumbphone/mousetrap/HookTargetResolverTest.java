package com.dumbphone.mousetrap;

import android.content.Context;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

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
        assertEquals(
                Arrays.asList("bc.w0", "bc.x0", "bc.z0"),
                HookTargetResolver.knownHelperClassNames(),
                "known launcher helper profiles");

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
}
