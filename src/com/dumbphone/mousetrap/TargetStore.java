package com.dumbphone.mousetrap;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class TargetStore {
    public static final String SETTINGS_KEY = "mousetrap_packages";
    private static final Set<String> DEFAULTS = Collections.unmodifiableSet(
            new LinkedHashSet<>(Arrays.asList(
                    "com.beeper.android",
                    "org.telegram.messenger",
                    "com.limebike")));

    private TargetStore() {}

    public static Set<String> read(Context context) {
        String raw = Settings.Secure.getString(context.getContentResolver(), SETTINGS_KEY);
        if (raw == null) return new LinkedHashSet<>(DEFAULTS);
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String line : raw.split("\n")) {
            String pkg = line.trim();
            if (!pkg.isEmpty()) result.add(pkg);
        }
        return result;
    }

    public static boolean contains(Context context, String packageName) {
        return packageName != null && read(context).contains(packageName);
    }

    public static void write(Context context, Set<String> packages) {
        StringBuilder value = new StringBuilder();
        for (String pkg : packages) {
            if (pkg == null || pkg.trim().isEmpty()) continue;
            if (value.length() > 0) value.append('\n');
            value.append(pkg.trim());
        }
        if (!Settings.Secure.putString(context.getContentResolver(), SETTINGS_KEY, value.toString())) {
            throw new IllegalStateException("Android rejected the Mouse Trap settings update");
        }
    }
}
