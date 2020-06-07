package com.adobe.mobile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

final class RequestBuilder {
    RequestBuilder() {
    }

    protected static void buildAndSendRequest(Map<String, Object> map, Map<String, Object> map2, long j) {
        if (WearableFunctionBridge.shouldSendHit()) {
            HashMap hashMap = new HashMap();
            hashMap.putAll(StaticMethods.getDefaultData());
            long timeSinceLaunch = StaticMethods.getTimeSinceLaunch();
            if (timeSinceLaunch > 0) {
                hashMap.put("a.TimeSinceLaunch", String.valueOf(timeSinceLaunch));
            }
            if (map != null) {
                hashMap.putAll(map);
            }
            if (MobileConfig.getInstance().getPrivacyStatus() == MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_UNKNOWN) {
                hashMap.put("a.privacy.mode", "unknown");
            }
            HashMap hashMap2 = map2 != null ? new HashMap(map2) : new HashMap();
            if (StaticMethods.getAID() != null) {
                hashMap2.put("aid", StaticMethods.getAID());
            }
            if (StaticMethods.getVisitorID() != null) {
                hashMap2.put("vid", StaticMethods.getVisitorID());
            }
            hashMap2.put("ce", MobileConfig.getInstance().getCharacterSet());
            if (MobileConfig.getInstance().getOfflineTrackingEnabled()) {
                hashMap2.put("ts", Long.toString(j));
            }
            hashMap2.put("t", StaticMethods.getTimestampString());
            hashMap2.put("cp", Lifecycle.applicationIsInBackground() ? "background" : "foreground");
            Iterator it = hashMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String str = (String) entry.getKey();
                if (str == null) {
                    it.remove();
                } else if (str.startsWith("&&")) {
                    hashMap2.put(str.substring(2), entry.getValue());
                    it.remove();
                }
            }
            if (MobileConfig.getInstance().getVisitorIdServiceEnabled()) {
                hashMap2.putAll(VisitorIDService.sharedInstance().getAnalyticsIdVisitorParameters());
            }
            Messages.checkForInAppMessage(new HashMap(hashMap2), new HashMap(hashMap), new HashMap(Lifecycle.getContextDataLowercase()));
            Messages.checkFor3rdPartyCallbacks(new HashMap(hashMap2), new HashMap(hashMap));
            hashMap2.put("c", StaticMethods.translateContextData(hashMap));
            StringBuilder sb = new StringBuilder(2048);
            sb.append("ndh=1");
            if (MobileConfig.getInstance().getVisitorIdServiceEnabled()) {
                sb.append(VisitorIDService.sharedInstance().getAnalyticsIdString());
            }
            StaticMethods.serializeToQueryString(hashMap2, sb);
            StaticMethods.logDebugFormat("Analytics - Request Queued (%s)", sb);
            AnalyticsWorker.sharedInstance().queue(sb.toString(), j);
        }
    }
}
