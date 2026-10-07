package com.dumbphone.mousetrap;

import android.content.Context;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class Hook implements IXposedHookLoadPackage {
    private static final String TAG = "[MouseTrap] ";
    private static final String LAUNCHER = "com.offlineinc.dumbdownlauncher";
    private static final String SERVICE = LAUNCHER + ".MouseAccessibilityService";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam == null || !LAUNCHER.equals(lpparam.packageName)) return;
        try {
            Class<?> serviceClass = XposedHelpers.findClass(SERVICE, lpparam.classLoader);
            Method target = findLegacyTarget(serviceClass);
            String how;
            if (target != null) {
                how = "legacy";
            } else {
                String sourceApk = lpparam.appInfo == null ? null : lpparam.appInfo.sourceDir;
                target = findCollaboratorTarget(lpparam.classLoader, sourceApk);
                how = "collaborator";
            }
            if (target == null) {
                XposedBridge.log(TAG + "safe failure: no boolean(String) on " + SERVICE
                        + " and no boolean(Context, String) collaborator found");
                return;
            }
            final boolean targetIsStatic = java.lang.reflect.Modifier.isStatic(target.getModifiers());
            final String signature = target.getDeclaringClass().getName() + "."
                    + target.getName() + " (" + how + (targetIsStatic ? ", static" : ", instance") + ")";
            target.setAccessible(true);
            XposedBridge.hookMethod(target, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        if (Boolean.TRUE.equals(param.getResult())) return;
                        // Legacy: boolean e(String pkg) — instance; thisObject is the service.
                        // Collaborator: static boolean f(Context, String pkg) — thisObject is null.
                        Context context = null;
                        Object pkgArg = null;
                        if (targetIsStatic) {
                            if (param.args == null || param.args.length < 1) return;
                            if (param.args[0] instanceof Context) context = (Context) param.args[0];
                            pkgArg = param.args.length > 1 ? param.args[1] : null;
                        } else {
                            if (param.thisObject instanceof Context) {
                                context = (Context) param.thisObject;
                            }
                            if (param.args != null) {
                                for (Object arg : param.args) {
                                    if (arg instanceof String) {
                                        pkgArg = arg;
                                        break;
                                    }
                                }
                            }
                        }
                        if (context == null || !(pkgArg instanceof String)) return;
                        String pkg = (String) pkgArg;
                        if (TargetStore.contains(context, pkg)) {
                            param.setResult(Boolean.TRUE);
                        }
                    } catch (Throwable t) {
                        XposedBridge.log(TAG + "decision error: " + t);
                    }
                }
            });
            XposedBridge.log(TAG + "hooked " + signature);
        } catch (Throwable t) {
            XposedBridge.log(TAG + "failed to install hook: " + t);
        }
    }

    /** Original v1.0.1 target: exactly one declared boolean(String) method on the service. */
    private static Method findLegacyTarget(Class<?> serviceClass) {
        List<Method> candidates = new ArrayList<>();
        for (Method method : serviceClass.getDeclaredMethods()) {
            Class<?>[] params = method.getParameterTypes();
            if (method.getReturnType() == boolean.class
                    && params.length == 1 && params[0] == String.class) {
                candidates.add(method);
            }
        }
        return candidates.size() == 1 ? candidates.get(0) : null;
    }

    /**
     * Resolve the static launcher predicate structurally from the launcher's narrow
     * obfuscated package. Exactly one candidate is required.
     */
    private static Method findCollaboratorTarget(ClassLoader cl, String sourceApk) {
        return HookTargetResolver.selectUniqueCandidate(
                HookTargetResolver.collectCandidateClasses(cl, sourceApk));
    }
}
