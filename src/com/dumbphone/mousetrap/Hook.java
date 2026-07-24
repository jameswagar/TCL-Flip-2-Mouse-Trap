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
            List<Method> candidates = new ArrayList<>();
            for (Method method : serviceClass.getDeclaredMethods()) {
                Class<?>[] params = method.getParameterTypes();
                if (method.getReturnType() == boolean.class
                        && params.length == 1 && params[0] == String.class) {
                    candidates.add(method);
                }
            }
            if (candidates.size() != 1) {
                XposedBridge.log(TAG + "safe failure: expected one boolean(String) target method, found "
                        + candidates.size());
                return;
            }
            Method target = candidates.get(0);
            target.setAccessible(true);
            XposedBridge.hookMethod(target, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        if (Boolean.TRUE.equals(param.getResult())) return;
                        if (!(param.thisObject instanceof Context)) return;
                        if (param.args == null || param.args.length != 1
                                || !(param.args[0] instanceof String)) return;
                        String pkg = (String) param.args[0];
                        if (TargetStore.contains((Context) param.thisObject, pkg)) {
                            param.setResult(Boolean.TRUE);
                        }
                    } catch (Throwable t) {
                        XposedBridge.log(TAG + "decision error: " + t);
                    }
                }
            });
            XposedBridge.log(TAG + "hooked " + SERVICE + "." + target.getName()
                    + " using signature boolean(String)");
        } catch (Throwable t) {
            XposedBridge.log(TAG + "failed to install hook: " + t);
        }
    }
}
