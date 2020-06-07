package com.adobe.mobile;

import android.content.Context;
import android.content.Intent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public final class Analytics {
    public static void trackState(final String str, Map<String, Object> map) {
        final HashMap hashMap = map != null ? new HashMap(map) : null;
        StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
            public void run() {
                AnalyticsTrackState.trackState(str, hashMap);
            }
        });
    }

    public static void trackAction(final String str, Map<String, Object> map) {
        final HashMap hashMap = map != null ? new HashMap(map) : null;
        StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
            public void run() {
                AnalyticsTrackAction.trackAction(str, hashMap);
            }
        });
    }

    public static void processReferrer(Context context, final Intent intent) {
        StaticMethods.setSharedContext(context);
        StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
            public void run() {
                ReferrerHandler.processIntent(intent);
            }
        });
    }

    public static String getTrackingIdentifier() {
        FutureTask futureTask = new FutureTask(new Callable<String>() {
            public String call() throws Exception {
                return StaticMethods.getAID();
            }
        });
        StaticMethods.getAnalyticsExecutor().execute(futureTask);
        try {
            return (String) futureTask.get();
        } catch (Exception e) {
            StaticMethods.logErrorFormat("Analytics - Unable to get TrackingIdentifier (%s)", e.getMessage());
            return null;
        }
    }
}
