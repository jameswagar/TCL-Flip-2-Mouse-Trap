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

        // --- Launcher 671 moved the predicate into the bounded ec namespace. ---

        List<Class<?>> legacyNamespace = HookTargetResolver.collectDexCandidateClasses(
                Collections.enumeration(Arrays.asList("bc.a1")),
                HookTargetResolverTest.class.getClassLoader());
        Method legacyOnly = HookTargetResolver.selectUniqueCandidate(legacyNamespace);
        assertNotNull(legacyOnly, "older bc-only launchers must keep resolving");
        assertEquals("bc.a1.f", describe(legacyOnly), "older bc compatibility retained");

        List<Class<?>> currentNamespace = HookTargetResolver.collectDexCandidateClasses(
                Collections.enumeration(Arrays.asList("ec.t0")),
                HookTargetResolverTest.class.getClassLoader());
        Method currentOnly = HookTargetResolver.selectUniqueCandidate(currentNamespace);
        assertNotNull(currentOnly, "ec namespace must resolve for launcher 671");
        assertEquals("ec.t0.i", describe(currentOnly), "671 predicate resolved by structure");

        // Real 671 shape: the bc namespace still exists and holds lambda holders only.
        List<Class<?>> mixedReal = HookTargetResolver.collectDexCandidateClasses(
                Collections.enumeration(Arrays.asList("bc.b1", "ec.t0")),
                HookTargetResolverTest.class.getClassLoader());
        assertEquals(Integer.valueOf(2), Integer.valueOf(mixedReal.size()),
                "both bounded namespaces are scanned");
        Method mixedRealTarget = HookTargetResolver.selectUniqueCandidate(mixedReal);
        assertNotNull(mixedRealTarget,
                "a candidate-free bc namespace must not block the ec predicate");
        assertEquals("ec.t0.i", describe(mixedRealTarget), "unique match across namespaces wins");

        List<Class<?>> crossAmbiguous = HookTargetResolver.collectDexCandidateClasses(
                Collections.enumeration(Arrays.asList("bc.a1", "ec.a1")),
                HookTargetResolverTest.class.getClassLoader());
        assertNull(HookTargetResolver.selectUniqueCandidate(crossAmbiguous),
                "one candidate in bc and one in ec must fail closed");

        List<Class<?>> ecDrifted = HookTargetResolver.collectDexCandidateClasses(
                Collections.enumeration(Arrays.asList("ec.a1", "ec.z0")),
                HookTargetResolverTest.class.getClassLoader());
        assertNull(HookTargetResolver.selectUniqueCandidate(ecDrifted),
                "multiple ec candidates must fail closed");

        List<Class<?>> wrongNamespace = HookTargetResolver.collectDexCandidateClasses(
                Collections.enumeration(Arrays.asList("ed.a1", "bd.a1")),
                HookTargetResolverTest.class.getClassLoader());
        assertEquals(Integer.valueOf(0), Integer.valueOf(wrongNamespace.size()),
                "near-miss namespaces must never be scanned");

        List<Class<?>> boundedWithNearMiss = HookTargetResolver.collectDexCandidateClasses(
                Collections.enumeration(Arrays.asList("ec.t0", "ed.a1")),
                HookTargetResolverTest.class.getClassLoader());
        Method boundedTarget = HookTargetResolver.selectUniqueCandidate(boundedWithNearMiss);
        assertNotNull(boundedTarget, "a same-signature class outside the bounds is ignored");
        assertEquals("ec.t0.i", describe(boundedTarget), "bounds are exact prefix labels");

        assertThrowsDiscovery(
                Collections.enumeration(Arrays.asList("ec.a1", "ec.missing")),
                "an incomplete ec namespace scan must fail closed");

        List<Class<?>> ecInspectionFailure = HookTargetResolver.collectDexCandidateClasses(
                Collections.enumeration(Arrays.asList("ec.a1", "ec.InspectionFailure")),
                HookTargetResolverTest.class.getClassLoader());
        assertEquals(Integer.valueOf(2), Integer.valueOf(ecInspectionFailure.size()),
                "ec inspection fixtures must load before method resolution");
        assertThrowsInspection(ecInspectionFailure,
                "an incomplete ec method scan must fail closed");

        System.out.println("HookTargetResolverTest: PASS");
    }

    private static String describe(Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName();
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
