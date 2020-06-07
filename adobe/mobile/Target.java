package com.adobe.mobile;

public final class Target {
    public static void clearCookies() {
        StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
            public void run() {
                TargetWorker.setTntIdFromOldCookieValues();
                StaticMethods.logDebugFormat("Target - resetting experience for this user", new Object[0]);
                TargetWorker.setTntId((String) null);
                TargetWorker.setThirdPartyId((String) null);
            }
        });
    }
}
