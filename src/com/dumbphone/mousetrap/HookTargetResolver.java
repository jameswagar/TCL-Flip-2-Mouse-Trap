package com.dumbphone.mousetrap;

import android.content.Context;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Fail-closed structural resolver for Dumb Launcher mouse-target predicates. */
final class HookTargetResolver {
    private static final List<String> KNOWN_HELPER_CLASS_NAMES = Collections.unmodifiableList(
            Arrays.asList("bc.w0", "bc.x0", "bc.z0"));

    private HookTargetResolver() {}

    static List<String> knownHelperClassNames() {
        return KNOWN_HELPER_CLASS_NAMES;
    }

    static Method selectUniqueCandidate(Iterable<Class<?>> classes) {
        Set<Method> matches = new LinkedHashSet<>();
        Set<Class<?>> seen = new LinkedHashSet<>();
        for (Class<?> clazz : classes) {
            if (clazz == null || !seen.add(clazz)) continue;
            try {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (isStaticBooleanContextString(method)) matches.add(method);
                }
            } catch (Throwable ignored) {
            }
        }
        return matches.size() == 1 ? matches.iterator().next() : null;
    }

    static List<Class<?>> collectCandidateClasses(Class<?> serviceClass, ClassLoader classLoader) {
        List<Class<?>> classes = new ArrayList<>();
        Set<Class<?>> seen = new LinkedHashSet<>();

        for (Class<?> current = serviceClass;
                current != null && current != Object.class;
                current = current.getSuperclass()) {
            try {
                for (java.lang.reflect.Field field : current.getDeclaredFields()) {
                    Class<?> fieldType = field.getType();
                    if (!fieldType.isInterface() && fieldType != serviceClass && seen.add(fieldType)) {
                        classes.add(fieldType);
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        for (String className : KNOWN_HELPER_CLASS_NAMES) {
            try {
                Class<?> helper = Class.forName(className, false, classLoader);
                if (seen.add(helper)) classes.add(helper);
            } catch (Throwable ignored) {
            }
        }

        if (seen.add(serviceClass)) classes.add(serviceClass);
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
