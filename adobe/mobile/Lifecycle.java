package com.adobe.mobile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import com.adobe.mobile.AudienceManager;
import com.adobe.mobile.Config;
import com.adobe.mobile.StaticMethods;
import com.adobe.mobile.VisitorID;
import com.wyndham.rewards.Constants;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

final class Lifecycle {
    private static final Object _contextDataMutex = new Object();
    private static final HashMap<String, Object> _lifecycleContextData = new HashMap<>();
    private static final HashMap<String, Object> _lifecycleContextDataLowercase = new HashMap<>();
    private static final Object _lowercaseContextDataMutex = new Object();
    private static final HashMap<String, Object> _previousSessionlifecycleContextData = new HashMap<>();
    private static boolean appIsInBackground = false;
    protected static volatile boolean lifecycleHasRun = false;
    protected static long sessionStartTime;

    private static long secToMs(long j) {
        return j * 1000;
    }

    Lifecycle() {
    }

    protected static void start(Activity activity, Map<String, Object> map) {
        Activity activity2;
        appIsInBackground = false;
        updateLifecycleDataForUpgradeIfNecessary();
        if (!lifecycleHasRun) {
            lifecycleHasRun = true;
            try {
                SharedPreferences sharedPreferences = StaticMethods.getSharedPreferences();
                try {
                    activity2 = StaticMethods.getCurrentActivity();
                } catch (StaticMethods.NullActivityException unused) {
                    activity2 = null;
                }
                if (!(activity2 == null || activity == null || !activity2.getComponentName().toString().equals(activity.getComponentName().toString()))) {
                    Messages.checkForInAppMessage((Map<String, Object>) null, (Map<String, Object>) null, (Map<String, Object>) null);
                }
                StaticMethods.setCurrentActivity(activity);
                Map<String, Object> checkForAdobeLinkData = checkForAdobeLinkData(activity, "targetPreviewlink");
                if (checkForAdobeLinkData != null && TargetPreviewManager.getInstance().getToken() == null) {
                    extractTargetPreviewData(checkForAdobeLinkData);
                    TargetPreviewManager.getInstance().downloadAndShowTargetPreviewUI();
                }
                TargetPreviewManager.getInstance().setupPreviewButton(activity);
                MobileConfig instance = MobileConfig.getInstance();
                long msToSec = msToSec(sharedPreferences.getLong("ADMS_PauseDate", 0));
                int lifecycleTimeout = instance.getLifecycleTimeout();
                if (msToSec > 0) {
                    long timeSince1970 = StaticMethods.getTimeSince1970() - msToSec;
                    long msToSec2 = msToSec(sharedPreferences.getLong("ADMS_SessionStart", 0));
                    sessionStartTime = msToSec2;
                    AnalyticsTrackTimedAction.sharedInstance().trackTimedActionUpdateAdjustedStartTime(timeSince1970);
                    if (timeSince1970 < ((long) lifecycleTimeout) && msToSec2 > 0) {
                        try {
                            SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                            sharedPreferencesEditor.putLong("ADMS_SessionStart", secToMs(msToSec2 + timeSince1970));
                            sharedPreferencesEditor.putBoolean("ADMS_SuccessfulClose", false);
                            sharedPreferencesEditor.remove("ADMS_PauseDate");
                            sharedPreferencesEditor.commit();
                        } catch (StaticMethods.NullContextException e) {
                            StaticMethods.logErrorFormat("Lifecycle - Error while updating start time (%s).", e.getMessage());
                        }
                        sessionStartTime = msToSec(sharedPreferences.getLong("ADMS_SessionStart", 0));
                        checkForAdobeClickThrough(activity, false);
                        return;
                    }
                }
                VisitorIDService.sharedInstance().idSync((Map<String, String>) null, (Map<String, String>) null, (VisitorID.VisitorIDAuthenticationState) null, true);
                instance.downloadRemoteConfigs();
                _lifecycleContextData.clear();
                clearContextDataLowercase();
                HashMap hashMap = map != null ? new HashMap(map) : new HashMap();
                Map<String, Object> checkForAdobeLinkData2 = checkForAdobeLinkData(activity, "applink");
                if (checkForAdobeLinkData2 != null) {
                    hashMap.putAll(checkForAdobeLinkData2);
                }
                long secToMs = secToMs(StaticMethods.getTimeSince1970());
                if (!sharedPreferences.contains("ADMS_InstallDate")) {
                    addInstallData(hashMap, secToMs);
                } else {
                    addNonInstallData(hashMap, secToMs);
                    addUpgradeData(hashMap, secToMs);
                    addSessionLengthData(hashMap);
                    checkReferrerDataForLaunch();
                }
                addLifecycleGenericData(hashMap, secToMs);
                generateLifecycleToBeSaved(hashMap);
                persistLifecycleContextData();
                MobileConfig.getInstance().invokeAdobeDataCallback(Config.MobileDataEvent.MOBILE_EVENT_LIFECYCLE, hashMap);
                AnalyticsTrackInternal.trackInternal("Lifecycle", hashMap, StaticMethods.getTimeSince1970() - 1);
                if (!instance.getAamAnalyticsForwardingEnabled()) {
                    AudienceManagerWorker.SubmitSignal(_lifecycleContextData, (AudienceManager.AudienceManagerCallback<Map<String, Object>>) null);
                }
                checkForAdobeClickThrough(activity, true);
                resetLifecycleFlags(secToMs);
            } catch (StaticMethods.NullContextException e2) {
                StaticMethods.logErrorFormat("Lifecycle - Error starting lifecycle (%s).", e2.getMessage());
            }
        }
    }

    private static void extractTargetPreviewData(Map<String, Object> map) {
        if (map != null) {
            Object obj = map.get("at_preview_token");
            if (obj != null && (obj instanceof String)) {
                TargetPreviewManager.getInstance().setTargetPreviewToken((String) obj);
            }
            Object obj2 = map.get("at_preview_endpoint");
            if (obj2 != null && (obj2 instanceof String)) {
                TargetPreviewManager.getInstance().setTargetPreviewApiUiFetchUrlBaseOverride((String) obj2);
            }
        }
    }

    protected static void stop() {
        appIsInBackground = true;
        lifecycleHasRun = false;
        StaticMethods.updateLastKnownTimestamp(Long.valueOf(StaticMethods.getTimeSince1970()));
        try {
            SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
            sharedPreferencesEditor.putBoolean("ADMS_SuccessfulClose", true);
            sharedPreferencesEditor.putLong("ADMS_PauseDate", secToMs(StaticMethods.getTimeSince1970()));
            sharedPreferencesEditor.commit();
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logErrorFormat("Lifecycle - Error updating lifecycle pause data (%s)", e.getMessage());
        }
        try {
            if (StaticMethods.getCurrentActivity().isFinishing()) {
                Messages.resetAllInAppMessages();
            }
        } catch (StaticMethods.NullActivityException unused) {
        }
    }

    private static void persistLifecycleContextData() {
        try {
            SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
            sharedPreferencesEditor.putString("ADMS_LifecycleData", new JSONObject(_lifecycleContextData).toString());
            sharedPreferencesEditor.commit();
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logWarningFormat("Lifecycle - Error persisting lifecycle data (%s)", e.getMessage());
        }
    }

    protected static HashMap<String, Object> getContextData() {
        synchronized (_contextDataMutex) {
            if (_lifecycleContextData.size() > 0) {
                HashMap<String, Object> hashMap = new HashMap<>(_lifecycleContextData);
                return hashMap;
            } else if (_previousSessionlifecycleContextData.size() > 0) {
                HashMap<String, Object> hashMap2 = new HashMap<>(_previousSessionlifecycleContextData);
                return hashMap2;
            } else {
                addPersistedLifecycleToMap(_previousSessionlifecycleContextData);
                HashMap<String, Object> hashMap3 = new HashMap<>(_previousSessionlifecycleContextData);
                return hashMap3;
            }
        }
    }

    protected static void updateContextData(Map<String, Object> map) {
        synchronized (_contextDataMutex) {
            _lifecycleContextData.putAll(map);
        }
        synchronized (_lowercaseContextDataMutex) {
            for (Map.Entry next : map.entrySet()) {
                _lifecycleContextDataLowercase.put(((String) next.getKey()).toLowerCase(), next.getValue());
            }
        }
    }

    protected static Map<String, Object> getContextDataLowercase() {
        HashMap<String, Object> hashMap;
        synchronized (_lowercaseContextDataMutex) {
            if (_lifecycleContextDataLowercase.size() <= 0) {
                HashMap hashMap2 = new HashMap();
                addPersistedLifecycleToMap(hashMap2);
                for (Map.Entry entry : hashMap2.entrySet()) {
                    _lifecycleContextDataLowercase.put(((String) entry.getKey()).toLowerCase(), entry.getValue());
                }
            }
            hashMap = _lifecycleContextDataLowercase;
        }
        return hashMap;
    }

    private static void clearContextDataLowercase() {
        synchronized (_lowercaseContextDataMutex) {
            _lifecycleContextDataLowercase.clear();
        }
    }

    protected static boolean applicationIsInBackground() {
        return appIsInBackground;
    }

    private static void addPersistedLifecycleToMap(Map<String, Object> map) {
        try {
            String string = StaticMethods.getSharedPreferences().getString("ADMS_LifecycleData", (String) null);
            if (string != null && string.length() > 0) {
                map.putAll(StaticMethods.mapFromJson(new JSONObject(string)));
            }
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logErrorFormat("Lifecycle - Issue loading persisted lifecycle data", e.getMessage());
        } catch (JSONException e2) {
            StaticMethods.logWarningFormat("Lifecycle - Issue loading persisted lifecycle data (%s)", e2.getMessage());
        }
    }

    private static void generateLifecycleToBeSaved(Map<String, Object> map) {
        HashMap hashMap = map != null ? new HashMap(map) : new HashMap();
        hashMap.putAll(StaticMethods.getDefaultData());
        hashMap.put("a.locale", StaticMethods.getDefaultAcceptLanguage());
        hashMap.put("a.ltv.amount", AnalyticsTrackLifetimeValueIncrease.getLifetimeValue());
        _lifecycleContextData.putAll(hashMap);
        clearContextDataLowercase();
        for (Map.Entry next : _lifecycleContextData.entrySet()) {
            _lifecycleContextDataLowercase.put(((String) next.getKey()).toLowerCase(), next.getValue());
        }
    }

    private static void resetLifecycleFlags(long j) {
        try {
            SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
            if (!StaticMethods.getSharedPreferences().contains("ADMS_SessionStart")) {
                sharedPreferencesEditor.putLong("ADMS_SessionStart", j);
                sessionStartTime = j / 1000;
            }
            sharedPreferencesEditor.putString("ADMS_LastVersion", StaticMethods.getApplicationVersion());
            sharedPreferencesEditor.putBoolean("ADMS_SuccessfulClose", false);
            sharedPreferencesEditor.remove("ADMS_PauseDate");
            sharedPreferencesEditor.commit();
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logErrorFormat("Lifecycle - Error resetting lifecycle flags (%s).", e.getMessage());
        }
    }

    private static void checkReferrerDataForLaunch() {
        Map<String, Object> referrerDataFromSharedPreferences = getReferrerDataFromSharedPreferences();
        if (referrerDataFromSharedPreferences != null && referrerDataFromSharedPreferences.size() != 0) {
            updateContextData(referrerDataFromSharedPreferences);
            MobileConfig.getInstance().invokeAdobeDataCallback(Config.MobileDataEvent.MOBILE_EVENT_ACQUISITION_LAUNCH, referrerDataFromSharedPreferences);
        }
    }

    private static Map<String, Object> getReferrerDataFromSharedPreferences() {
        HashMap<String, Object> parseOtherReferrerFields;
        try {
            if (StaticMethods.getSharedPreferences().contains("ADMS_Referrer_ContextData_Json_String")) {
                HashMap hashMap = new HashMap();
                String processReferrerDataFromV3Server = ReferrerHandler.processReferrerDataFromV3Server(StaticMethods.getSharedPreferences().getString("ADMS_Referrer_ContextData_Json_String", (String) null));
                hashMap.putAll(ReferrerHandler.parseV3ContextDataFromResponse(processReferrerDataFromV3Server));
                if (hashMap.size() > 0) {
                    hashMap.putAll(ReferrerHandler.processV3ResponseAndReturnAdobeData(processReferrerDataFromV3Server));
                } else {
                    HashMap<String, Object> parseGoogleReferrerFields = ReferrerHandler.parseGoogleReferrerFields(processReferrerDataFromV3Server);
                    if (parseGoogleReferrerFields.containsKey("a.referrer.campaign.name") && parseGoogleReferrerFields.containsKey("a.referrer.campaign.source")) {
                        hashMap.putAll(parseGoogleReferrerFields);
                    }
                    if (hashMap.size() == 0 && (parseOtherReferrerFields = ReferrerHandler.parseOtherReferrerFields(processReferrerDataFromV3Server)) != null && parseOtherReferrerFields.size() > 0) {
                        hashMap.putAll(parseOtherReferrerFields);
                    }
                }
                return hashMap;
            }
            if (StaticMethods.getSharedPreferences().contains("utm_campaign")) {
                String string = StaticMethods.getSharedPreferences().getString("utm_source", (String) null);
                String string2 = StaticMethods.getSharedPreferences().getString("utm_medium", (String) null);
                String string3 = StaticMethods.getSharedPreferences().getString("utm_term", (String) null);
                String string4 = StaticMethods.getSharedPreferences().getString("utm_content", (String) null);
                String string5 = StaticMethods.getSharedPreferences().getString("utm_campaign", (String) null);
                String string6 = StaticMethods.getSharedPreferences().getString("trackingcode", (String) null);
                if (!(string == null || string5 == null)) {
                    HashMap hashMap2 = new HashMap();
                    hashMap2.put("a.referrer.campaign.source", string);
                    hashMap2.put("a.referrer.campaign.medium", string2);
                    hashMap2.put("a.referrer.campaign.term", string3);
                    hashMap2.put("a.referrer.campaign.content", string4);
                    hashMap2.put("a.referrer.campaign.name", string5);
                    hashMap2.put("a.referrer.campaign.trackingcode", string6);
                    try {
                        SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("googleReferrerData", new JSONObject(hashMap2));
                        sharedPreferencesEditor.putString("ADMS_Referrer_ContextData_Json_String", jSONObject.toString());
                        sharedPreferencesEditor.commit();
                    } catch (StaticMethods.NullContextException e) {
                        StaticMethods.logErrorFormat("Analytics - Error persisting referrer data (%s)", e.getMessage());
                    } catch (JSONException e2) {
                        StaticMethods.logErrorFormat("Analytics - Error persisting referrer data (%s)", e2.getMessage());
                    }
                    return hashMap2;
                }
            }
            return null;
        } catch (StaticMethods.NullContextException e3) {
            StaticMethods.logErrorFormat("Lifecycle - Error pulling persisted Acquisition data (%s)", e3.getMessage());
        }
    }

    private static void addInstallData(Map<String, Object> map, long j) {
        map.put("a.InstallDate", new SimpleDateFormat("M/d/yyyy", Locale.US).format(Long.valueOf(j)));
        map.put("a.InstallEvent", "InstallEvent");
        map.put("a.DailyEngUserEvent", "DailyEngUserEvent");
        map.put("a.MonthlyEngUserEvent", "MonthlyEngUserEvent");
        try {
            if (!StaticMethods.getSharedPreferences().contains("ADMS_Referrer_ContextData_Json_String")) {
                if (!StaticMethods.getSharedPreferences().contains("utm_campaign")) {
                    if (MobileConfig.getInstance().mobileReferrerConfigured() && MobileConfig.getInstance().getReferrerTimeout() > 0) {
                        ReferrerHandler.setReferrerProcessed(false);
                        Messages.block3rdPartyCallbacksQueueForReferrer();
                    }
                    SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                    sharedPreferencesEditor.putLong("ADMS_InstallDate", j);
                    sharedPreferencesEditor.commit();
                }
            }
            Map<String, Object> referrerDataFromSharedPreferences = getReferrerDataFromSharedPreferences();
            ReferrerHandler.triggerDeepLink(ReferrerHandler.getDeepLinkFromJSON(ReferrerHandler.translateV3StringResponseToJSONObject(StaticMethods.getSharedPreferences().getString("ADMS_Referrer_ContextData_Json_String", (String) null))));
            if (referrerDataFromSharedPreferences != null && referrerDataFromSharedPreferences.size() >= 0) {
                map.putAll(referrerDataFromSharedPreferences);
                MobileConfig.getInstance().invokeAdobeDataCallback(Config.MobileDataEvent.MOBILE_EVENT_ACQUISITION_INSTALL, referrerDataFromSharedPreferences);
            }
            SharedPreferences.Editor sharedPreferencesEditor2 = StaticMethods.getSharedPreferencesEditor();
            sharedPreferencesEditor2.putLong("ADMS_InstallDate", j);
            sharedPreferencesEditor2.commit();
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logErrorFormat("Lifecycle - Error setting install data (%s).", e.getMessage());
        }
    }

    private static void addUpgradeData(Map<String, Object> map, long j) {
        try {
            SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
            long j2 = StaticMethods.getSharedPreferences().getLong("ADMS_UpgradeDate", 0);
            if (isApplicationUpgrade()) {
                map.put("a.UpgradeEvent", "UpgradeEvent");
                sharedPreferencesEditor.putLong("ADMS_UpgradeDate", j);
                sharedPreferencesEditor.putInt("ADMS_LaunchesAfterUpgrade", 0);
            } else if (j2 > 0) {
                map.put("a.DaysSinceLastUpgrade", calculateDaysSince(j2, j));
            }
            if (j2 > 0) {
                int i = StaticMethods.getSharedPreferences().getInt("ADMS_LaunchesAfterUpgrade", 0) + 1;
                map.put("a.LaunchesSinceUpgrade", "" + i);
                sharedPreferencesEditor.putInt("ADMS_LaunchesAfterUpgrade", i);
            }
            sharedPreferencesEditor.commit();
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logErrorFormat("Lifecycle - Error setting upgrade data (%s).", e.getMessage());
        }
    }

    private static boolean isApplicationUpgrade() {
        try {
            return true ^ StaticMethods.getApplicationVersion().equalsIgnoreCase(StaticMethods.getSharedPreferences().getString("ADMS_LastVersion", ""));
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logErrorFormat("Lifecycle - Unable to get application version (%s)", e.getLocalizedMessage());
            return false;
        }
    }

    private static void updateLifecycleDataForUpgradeIfNecessary() {
        HashMap<String, Object> contextData;
        if (isApplicationUpgrade() && (contextData = getContextData()) != null && contextData.size() > 0) {
            contextData.put("a.AppID", StaticMethods.getApplicationID());
            HashMap<String, Object> hashMap = _lifecycleContextData;
            if (hashMap == null || hashMap.size() <= 0) {
                try {
                    synchronized (_contextDataMutex) {
                        _previousSessionlifecycleContextData.put("a.AppID", StaticMethods.getApplicationID());
                    }
                    SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                    sharedPreferencesEditor.putString("ADMS_LifecycleData", new JSONObject(contextData).toString());
                    sharedPreferencesEditor.commit();
                    clearContextDataLowercase();
                } catch (StaticMethods.NullContextException e) {
                    StaticMethods.logWarningFormat("Lifecycle - Error persisting lifecycle data (%s)", e.getMessage());
                }
            } else {
                updateContextData(contextData);
            }
        }
    }

    private static void addLifecycleGenericData(Map<String, Object> map, long j) {
        map.putAll(StaticMethods.getDefaultData());
        map.put("a.LaunchEvent", "LaunchEvent");
        map.put("a.OSVersion", StaticMethods.getOperatingSystem());
        map.put("a.HourOfDay", new SimpleDateFormat("H", Locale.US).format(Long.valueOf(j)));
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(j);
        map.put("a.DayOfWeek", Integer.toString(instance.get(7)));
        String advertisingIdentifier = StaticMethods.getAdvertisingIdentifier();
        if (advertisingIdentifier != null) {
            map.put("a.adid", advertisingIdentifier);
        }
        try {
            SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
            int i = StaticMethods.getSharedPreferences().getInt("ADMS_Launches", 0) + 1;
            map.put("a.Launches", Integer.toString(i));
            sharedPreferencesEditor.putInt("ADMS_Launches", i);
            sharedPreferencesEditor.putLong("ADMS_LastDateUsed", j);
            sharedPreferencesEditor.commit();
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logErrorFormat("Lifecycle - Error adding generic data (%s).", e.getMessage());
        }
    }

    private static void addNonInstallData(Map<String, Object> map, long j) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/M/d", Locale.US);
            long j2 = StaticMethods.getSharedPreferences().getLong("ADMS_LastDateUsed", 0);
            if (!simpleDateFormat.format(Long.valueOf(j)).equalsIgnoreCase(simpleDateFormat.format(new Date(j2)))) {
                map.put("a.DailyEngUserEvent", "DailyEngUserEvent");
            }
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy/M", Locale.US);
            if (!simpleDateFormat2.format(Long.valueOf(j)).equalsIgnoreCase(simpleDateFormat2.format(new Date(j2)))) {
                map.put("a.MonthlyEngUserEvent", "MonthlyEngUserEvent");
            }
            map.put("a.DaysSinceFirstUse", calculateDaysSince(StaticMethods.getSharedPreferences().getLong("ADMS_InstallDate", 0), j));
            map.put("a.DaysSinceLastUse", calculateDaysSince(j2, j));
            if (!StaticMethods.getSharedPreferences().getBoolean("ADMS_SuccessfulClose", false)) {
                SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                sharedPreferencesEditor.remove("ADMS_PauseDate");
                sharedPreferencesEditor.remove("ADMS_SessionStart");
                sessionStartTime = StaticMethods.getTimeSince1970();
                sharedPreferencesEditor.commit();
                long j3 = StaticMethods.getSharedPreferences().getLong("ADBLastKnownTimestampKey", 0);
                if (j3 <= 0 || !MobileConfig.getInstance().mobileUsingAnalytics() || !MobileConfig.getInstance().getOfflineTrackingEnabled() || !MobileConfig.getInstance().getBackdateSessionInfoEnabled()) {
                    map.put("a.CrashEvent", "CrashEvent");
                } else {
                    try {
                        SharedPreferences sharedPreferences = StaticMethods.getSharedPreferences();
                        HashMap hashMap = new HashMap();
                        hashMap.put("a.CrashEvent", "CrashEvent");
                        hashMap.put("a.OSVersion", sharedPreferences.getString("ADOBEMOBILE_STOREDDEFAULTS_OS", ""));
                        hashMap.put("a.AppID", sharedPreferences.getString("ADOBEMOBILE_STOREDDEFAULTS_APPID", ""));
                        AnalyticsTrackInternal.trackInternal("Crash", hashMap, j3 + 1);
                        _lifecycleContextData.put("a.CrashEvent", "CrashEvent");
                    } catch (StaticMethods.NullContextException e) {
                        StaticMethods.logWarningFormat("Config - Unable to get crash data for backdated hit (%s)", e.getLocalizedMessage());
                    }
                }
                AnalyticsTrackTimedAction.sharedInstance().trackTimedActionUpdateActionsClearAdjustedStartTime();
            }
        } catch (StaticMethods.NullContextException e2) {
            StaticMethods.logErrorFormat("Lifecycle - Error setting non install data (%s).", e2.getMessage());
        }
    }

    private static void addSessionLengthData(Map<String, Object> map) {
        try {
            long msToSec = msToSec(StaticMethods.getSharedPreferences().getLong("ADMS_PauseDate", 0));
            if (StaticMethods.getTimeSince1970() - msToSec >= ((long) MobileConfig.getInstance().getLifecycleTimeout())) {
                long msToSec2 = msToSec - msToSec(StaticMethods.getSharedPreferences().getLong("ADMS_SessionStart", 0));
                sessionStartTime = StaticMethods.getTimeSince1970();
                if (msToSec2 <= 0 || msToSec2 >= 604800) {
                    map.put("a.ignoredSessionLength", Long.toString(msToSec2));
                } else {
                    long j = StaticMethods.getSharedPreferences().getLong("ADBLastKnownTimestampKey", 0);
                    if (j <= 0 || !MobileConfig.getInstance().mobileUsingAnalytics() || !MobileConfig.getInstance().getOfflineTrackingEnabled() || !MobileConfig.getInstance().getBackdateSessionInfoEnabled()) {
                        map.put("a.PrevSessionLength", Long.toString(msToSec2));
                    } else {
                        try {
                            SharedPreferences sharedPreferences = StaticMethods.getSharedPreferences();
                            HashMap hashMap = new HashMap();
                            hashMap.put("a.PrevSessionLength", String.valueOf(msToSec2));
                            hashMap.put("a.OSVersion", sharedPreferences.getString("ADOBEMOBILE_STOREDDEFAULTS_OS", ""));
                            hashMap.put("a.AppID", sharedPreferences.getString("ADOBEMOBILE_STOREDDEFAULTS_APPID", ""));
                            AnalyticsTrackInternal.trackInternal("SessionInfo", hashMap, j + 1);
                            _lifecycleContextData.put("a.PrevSessionLength", String.valueOf(msToSec2));
                        } catch (StaticMethods.NullContextException e) {
                            StaticMethods.logWarningFormat("Config - Unable to get session data for backdated hit (%s)", e.getLocalizedMessage());
                        }
                    }
                }
                SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                sharedPreferencesEditor.remove("ADMS_SessionStart");
                sharedPreferencesEditor.commit();
            }
        } catch (StaticMethods.NullContextException e2) {
            StaticMethods.logErrorFormat("Lifecycle - Error adding session length data (%s).", e2.getMessage());
        }
    }

    private static String calculateDaysSince(long j, long j2) {
        return Integer.toString((int) ((j2 - j) / Constants.ONE_DAY_MS));
    }

    private static void checkForAdobeClickThrough(Activity activity, boolean z) {
        Intent intent;
        if (activity != null && (intent = activity.getIntent()) != null) {
            String str = null;
            String stringExtra = intent.getStringExtra("adb_m_id");
            String stringExtra2 = intent.getStringExtra("adb_m_l_id");
            Map<String, Object> adobeDeepLinkQueryParameters = getAdobeDeepLinkQueryParameters(intent.getData(), "applink");
            HashMap hashMap = new HashMap();
            if (!z && adobeDeepLinkQueryParameters != null) {
                str = "AdobeLink";
                hashMap.putAll(adobeDeepLinkQueryParameters);
                updateContextData(hashMap);
            }
            if (stringExtra != null && stringExtra.length() > 0) {
                hashMap.put("a.push.payloadId", stringExtra);
                str = "PushMessage";
                updateContextData(hashMap);
            } else if (stringExtra2 != null && stringExtra2.length() > 0) {
                hashMap.put("a.message.id", stringExtra2);
                hashMap.put("a.message.clicked", 1);
                str = "In-App Message";
                updateContextData(hashMap);
            }
            if (str != null && MobileConfig.getInstance().mobileUsingAnalytics()) {
                AnalyticsTrackInternal.trackInternal(str, hashMap, StaticMethods.getTimeSince1970());
            }
        }
    }

    private static Map<String, Object> checkForAdobeLinkData(Activity activity, String str) {
        Intent intent;
        Uri data;
        if (activity == null || (intent = activity.getIntent()) == null || (data = intent.getData()) == null) {
            return null;
        }
        Map<String, Object> adobeDeepLinkQueryParameters = getAdobeDeepLinkQueryParameters(data, str);
        clearTargetPreviewTokenInIntent(intent, adobeDeepLinkQueryParameters);
        return adobeDeepLinkQueryParameters;
    }

    private static void clearTargetPreviewTokenInIntent(Intent intent, Map<String, Object> map) {
        try {
            Uri data = intent.getData();
            if (data != null && map != null) {
                if (!map.isEmpty()) {
                    if (map.containsKey("at_preview_token")) {
                        intent.setData(data.buildUpon().encodedQuery("").build());
                    }
                }
            }
        } catch (Exception e) {
            StaticMethods.logErrorFormat("Lifecycle - Exception while attempting to remove target token parameters from Uri (%s).", e.getMessage());
        }
    }

    private static Map<String, Object> getAdobeDeepLinkQueryParameters(Uri uri, String str) {
        if (uri == null) {
            return null;
        }
        String query = uri.getQuery();
        String str2 = "a.deeplink.id";
        if (str.equals("targetPreviewlink")) {
            str2 = "at_preview_token";
        }
        if (query != null && query.length() > 0) {
            if (query.contains(str2 + "=")) {
                HashMap hashMap = new HashMap();
                for (String str3 : query.split("&")) {
                    if (str3 != null && str3.length() > 0) {
                        String[] split = str3.split("=", 2);
                        if (split.length == 1 || (split.length == 2 && split[1].isEmpty())) {
                            StaticMethods.logWarningFormat("Deep Link - Skipping an invalid variable on the URI query (%s).", split[0]);
                        } else {
                            String str4 = split[0];
                            String str5 = split[1];
                            if (str4.startsWith("ctx")) {
                                hashMap.put(str4.substring(3), str5);
                            } else if (str4.startsWith("adb")) {
                                hashMap.put("a.acquisition.custom.".concat(str4.substring(3)), str5);
                            } else {
                                hashMap.put(str4, str5);
                            }
                        }
                    }
                }
                return hashMap;
            }
        }
        return null;
    }

    private static long msToSec(long j) {
        return j / 1000;
    }
}
