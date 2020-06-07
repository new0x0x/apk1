package com.adobe.mobile;

import android.app.Activity;
import android.content.Context;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public final class Config {

    public interface AdobeDataCallback {
        void call(MobileDataEvent mobileDataEvent, Map<String, Object> map);
    }

    public enum ApplicationType {
        APPLICATION_TYPE_HANDHELD(0),
        APPLICATION_TYPE_WEARABLE(1);
        
        private final int value;

        private ApplicationType(int i) {
            this.value = i;
        }
    }

    public enum MobileDataEvent {
        MOBILE_EVENT_LIFECYCLE(0),
        MOBILE_EVENT_ACQUISITION_INSTALL(1),
        MOBILE_EVENT_ACQUISITION_LAUNCH(2);
        
        private final int value;

        private MobileDataEvent(int i) {
            this.value = i;
        }
    }

    public static void setContext(Context context) {
        setContext(context, ApplicationType.APPLICATION_TYPE_HANDHELD);
    }

    public static void setContext(Context context, ApplicationType applicationType) {
        StaticMethods.setSharedContext(context);
        setApplicationType(applicationType);
        if (applicationType == ApplicationType.APPLICATION_TYPE_WEARABLE) {
            StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
                public void run() {
                    WearableFunctionBridge.syncConfigFromHandheld();
                }
            });
        }
    }

    public static String getUserIdentifier() {
        FutureTask futureTask = new FutureTask(new Callable<String>() {
            public String call() throws Exception {
                return StaticMethods.getVisitorID();
            }
        });
        StaticMethods.getAnalyticsExecutor().execute(futureTask);
        try {
            return (String) futureTask.get();
        } catch (Exception e) {
            StaticMethods.logErrorFormat("Analytics - Unable to get UserIdentifier (%s)", e.getMessage());
            return null;
        }
    }

    public static void setApplicationType(ApplicationType applicationType) {
        StaticMethods.setApplicationType(applicationType);
    }

    public static void setDebugLogging(Boolean bool) {
        StaticMethods.setDebugLogging(bool.booleanValue());
    }

    public static void collectLifecycleData() {
        if (StaticMethods.isWearableApp()) {
            StaticMethods.logWarningFormat("Analytics - Method collectLifecycleData is not available for Wearable", new Object[0]);
        } else {
            StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
                public void run() {
                    Lifecycle.start((Activity) null, (Map<String, Object>) null);
                }
            });
        }
    }

    public static void collectLifecycleData(final Activity activity) {
        if (StaticMethods.isWearableApp()) {
            StaticMethods.logWarningFormat("Analytics - Method collectLifecycleData is not available for Wearable", new Object[0]);
        } else {
            StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
                public void run() {
                    Lifecycle.start(activity, (Map<String, Object>) null);
                }
            });
        }
    }

    public static void collectLifecycleData(final Activity activity, final Map<String, Object> map) {
        if (StaticMethods.isWearableApp()) {
            StaticMethods.logWarningFormat("Analytics - Method collectLifecycleData is not available for Wearable", new Object[0]);
        } else {
            StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
                public void run() {
                    Lifecycle.start(activity, map);
                }
            });
        }
    }

    public static void pauseCollectingLifecycleData() {
        if (StaticMethods.isWearableApp()) {
            StaticMethods.logWarningFormat("Analytics - Method pauseCollectingLifecycleData is not available for Wearable", new Object[0]);
            return;
        }
        MessageAlert.clearCurrentDialog();
        StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
            public void run() {
                Lifecycle.stop();
            }
        });
    }
}
