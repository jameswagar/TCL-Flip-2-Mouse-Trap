package com.dumbphone.mousetrap;

import android.content.Context;
import dalvik.system.DexFile;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Fail-closed structural resolver for Dumb Launcher mouse-target predicates. */
final class HookTargetResolver {
    private static final String HELPER_PACKAGE_PREFIX = "bc.";

    static final class DiscoveryException extends RuntimeException {
        DiscoveryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private HookTargetResolver() {}

    static Method selectUniqueCandidate(Iterable<Class<?>> classes) {
        Set<Method> matches = new LinkedHashSet<>();
        Set<Class<?>> seen = new LinkedHashSet<>();
        for (Class<?> clazz : classes) {
            if (clazz == null || !seen.add(clazz)) continue;
            try {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (isStaticBooleanContextString(method)) matches.add(method);
                }
            } catch (Throwable failure) {
                throw new DiscoveryException("candidate method inspection failed", failure);
            }
        }
        return matches.size() == 1 ? matches.iterator().next() : null;
    }

    static List<Class<?>> collectCandidateClasses(ClassLoader classLoader, String sourceApk) {
        if (sourceApk == null || sourceApk.trim().isEmpty()) return new ArrayList<>();
        DexFile dexFile = null;
        try {
            dexFile = new DexFile(sourceApk);
            return collectDexCandidateClasses(dexFile.entries(), classLoader);
        } catch (Throwable failure) {
            throw new DiscoveryException("launcher DEX discovery failed", failure);
        } finally {
            if (dexFile != null) {
                try {
                    dexFile.close();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    static List<Class<?>> collectDexCandidateClasses(
            Enumeration<String> classNames, ClassLoader classLoader) {
        List<Class<?>> classes = new ArrayList<>();
        Set<Class<?>> seen = new LinkedHashSet<>();
        if (classNames == null || classLoader == null) return classes;
        while (classNames.hasMoreElements()) {
            String className = classNames.nextElement();
            if (className == null || !className.startsWith(HELPER_PACKAGE_PREFIX)) continue;
            try {
                Class<?> candidate = Class.forName(className, false, classLoader);
                if (seen.add(candidate)) classes.add(candidate);
            } catch (Throwable failure) {
                throw new DiscoveryException("launcher class loading failed", failure);
            }
        }
        return classes;
    }

    private static boolean isStaticBooleanContextString(Method method) {
        Class<?>[] params = method.getParameterTypes();
        return Modifier.isStatic(method.getModifiers())
                && method.getReturnType() == boolean.class
                && params.length == 2
                && params[0] == Context.class
                && params[1] == String.class;
    }
}
