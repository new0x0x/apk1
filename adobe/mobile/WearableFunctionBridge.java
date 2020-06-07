package com.adobe.mobile;

import android.content.Context;

final class WearableFunctionBridge {
    private static Class<?> configSynchronizerClassLoader;
    private static Class<?> wearableFunctionClassLoader;

    WearableFunctionBridge() {
    }

    private static Class<?> getWearableFunctionClass() {
        Class<?> cls = wearableFunctionClassLoader;
        if (cls != null) {
            return cls;
        }
        try {
            wearableFunctionClassLoader = WearableFunctionBridge.class.getClassLoader().loadClass("com.adobe.mobile.WearableFunction");
        } catch (Exception e) {
            StaticMethods.logDebugFormat("Wearable - Failed to load class com.adobe.mobile.WearableFunction", e.getLocalizedMessage());
        }
        return wearableFunctionClassLoader;
    }

    private static Class<?> getConfigSynchronizerClass() {
        Class<?> cls = configSynchronizerClassLoader;
        if (cls != null) {
            return cls;
        }
        try {
            configSynchronizerClassLoader = WearableFunctionBridge.class.getClassLoader().loadClass("com.adobe.mobile.ConfigSynchronizer");
        } catch (Exception e) {
            StaticMethods.logDebugFormat("Wearable - Failed to load class com.adobe.mobile.ConfigSynchronizer", e.getLocalizedMessage());
        }
        return configSynchronizerClassLoader;
    }

    protected static boolean isGooglePlayServicesEnabled() {
        try {
            Class<?> loadClass = WearableFunctionBridge.class.getClassLoader().loadClass("com.google.android.gms.common.GoogleApiAvailability");
            Object invoke = loadClass.getDeclaredMethod("getInstance", new Class[0]).invoke((Object) null, new Object[0]);
            Object invoke2 = loadClass.getDeclaredMethod("isGooglePlayServicesAvailable", new Class[]{Context.class}).invoke(invoke, new Object[]{StaticMethods.getSharedContext()});
            if (invoke2 instanceof Integer) {
                if (((Integer) invoke2).intValue() == 0) {
                    return true;
                }
                return false;
            }
        } catch (IllegalStateException e) {
            StaticMethods.logDebugFormat("Wearable - Google Play Services is not enabled in your app's AndroidManifest.xml", e.getLocalizedMessage());
        } catch (Exception unused) {
        }
        try {
            Object invoke3 = WearableFunctionBridge.class.getClassLoader().loadClass("com.google.android.gms.common.GooglePlayServicesUtil").getDeclaredMethod("isGooglePlayServicesAvailable", new Class[]{Context.class}).invoke((Object) null, new Object[]{StaticMethods.getSharedContext()});
            if (!(invoke3 instanceof Integer) || ((Integer) invoke3).intValue() != 0) {
                return false;
            }
            return true;
        } catch (IllegalStateException e2) {
            StaticMethods.logDebugFormat("Wearable - Google Play Services is not enabled in your app's AndroidManifest.xml", e2.getLocalizedMessage());
        } catch (Exception unused2) {
        }
        return false;
    }

    protected static boolean shouldSendHit() {
        if (!StaticMethods.isWearableApp()) {
            return true;
        }
        try {
            Object invoke = getWearableFunctionClass().getDeclaredMethod("shouldSendHit", new Class[0]).invoke((Object) null, new Object[0]);
            if (invoke instanceof Boolean) {
                return ((Boolean) invoke).booleanValue();
            }
        } catch (Exception e) {
            StaticMethods.logDebugFormat("Wearable - Error checking status of handheld app (%s)", e.getLocalizedMessage());
        }
        return true;
    }

    protected static void sendGenericRequest(String str, int i, String str2) {
        try {
            getWearableFunctionClass().getDeclaredMethod("sendGenericRequest", new Class[]{String.class, Integer.TYPE}).invoke((Object) null, new Object[]{str, Integer.valueOf(i)});
            StaticMethods.logDebugFormat("%s - Request Sent(%s)", str2, str);
        } catch (Exception e) {
            StaticMethods.logDebugFormat("Wearable - Error sending request (%s)", e.getLocalizedMessage());
        }
    }

    protected static byte[] retrieveData(String str, int i) {
        try {
            Object invoke = getWearableFunctionClass().getDeclaredMethod("retrieveData", new Class[]{String.class, Integer.TYPE}).invoke((Object) null, new Object[]{str, Integer.valueOf(i)});
            if (invoke instanceof byte[]) {
                return (byte[]) invoke;
            }
        } catch (Exception e) {
            StaticMethods.logDebugFormat("Wearable - Error sending request (%s)", e.getLocalizedMessage());
        }
        return null;
    }

    protected static byte[] retrieveAnalyticsRequestData(String str, String str2, int i, String str3) {
        try {
            Object invoke = getWearableFunctionClass().getDeclaredMethod("retrieveAnalyticsRequestData", new Class[]{String.class, String.class, Integer.TYPE}).invoke((Object) null, new Object[]{str, str2, Integer.valueOf(i)});
            if (invoke instanceof byte[]) {
                return (byte[]) invoke;
            }
        } catch (Exception e) {
            StaticMethods.logDebugFormat("Wearable - Error sending request (%s)", e.getLocalizedMessage());
        }
        return null;
    }

    protected static boolean sendThirdPartyRequest(String str, String str2, int i, String str3, String str4) {
        try {
            Object invoke = getWearableFunctionClass().getDeclaredMethod("sendThirdPartyRequest", new Class[]{String.class, String.class, Integer.TYPE, String.class}).invoke((Object) null, new Object[]{str, str2, Integer.valueOf(i), str3});
            if (invoke instanceof Boolean) {
                if (((Boolean) invoke).booleanValue()) {
                    StaticMethods.logDebugFormat("%s - Successfully forwarded hit (url:%s body:%s contentType:%s)", str4, str, str2, str3);
                } else {
                    StaticMethods.logDebugFormat("%s - Failed to forwarded hit (url:%s body:%s contentType:%s)", str4, str, str2, str3);
                }
                return ((Boolean) invoke).booleanValue();
            }
        } catch (Exception e) {
            StaticMethods.logDebugFormat("Wearable - Error sending request (%s)", e.getLocalizedMessage());
        }
        return false;
    }

    protected static void syncVidServiceToWearable(String str, String str2, String str3, long j, long j2, String str4) {
        if (!StaticMethods.isWearableApp() && MobileConfig.getInstance().mobileUsingGooglePlayServices()) {
            try {
                getConfigSynchronizerClass().getDeclaredMethod("syncVidService", new Class[]{String.class, String.class, String.class, Long.TYPE, Long.TYPE, String.class}).invoke((Object) null, new Object[]{str, str2, str3, Long.valueOf(j), Long.valueOf(j2), str4});
            } catch (Exception e) {
                StaticMethods.logDebugFormat("Wearable - Unable to sync visitor id service (%s)", e.getLocalizedMessage());
            }
        }
    }

    protected static void syncPrivacyStatusToWearable(int i) {
        if (!StaticMethods.isWearableApp() && MobileConfig.getInstance().mobileUsingGooglePlayServices()) {
            try {
                getConfigSynchronizerClass().getDeclaredMethod("syncPrivacyStatus", new Class[]{Integer.TYPE}).invoke((Object) null, new Object[]{Integer.valueOf(i)});
            } catch (Exception e) {
                StaticMethods.logDebugFormat("Wearable - Unable to sync privacy status (%s)", e.getLocalizedMessage());
            }
        }
    }

    protected static void syncConfigFromHandheld() {
        if (StaticMethods.isWearableApp()) {
            try {
                getConfigSynchronizerClass().getDeclaredMethod("syncConfigFromHandheld", new Class[0]).invoke((Object) null, new Object[0]);
            } catch (Exception e) {
                StaticMethods.logDebugFormat("Wearable - Unable to sync config (%s)", e.getLocalizedMessage());
            }
        }
    }
}
