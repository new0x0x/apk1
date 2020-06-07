package com.adobe.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import com.adobe.mobile.Config;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oooooo.nnoonn;
import oooooo.ononon;
import oooooo.qqvvqq;
import oooooo.vqqqvq;
import oooooo.vqvvqq;
import oooooo.vvqqvq;
import org.json.JSONException;
import org.json.JSONObject;

final class StaticMethods {
    private static final char[] BYTE_TO_HEX = "000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F303132333435363738393A3B3C3D3E3F404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F606162636465666768696A6B6C6D6E6F707172737475767778797A7B7C7D7E7F808182838485868788898A8B8C8D8E8F909192939495969798999A9B9C9D9E9FA0A1A2A3A4A5A6A7A8A9AAABACADAEAFB0B1B2B3B4B5B6B7B8B9BABBBCBDBEBFC0C1C2C3C4C5C6C7C8C9CACBCCCDCECFD0D1D2D3D4D5D6D7D8D9DADBDCDDDEDFE0E1E2E3E4E5E6E7E8E9EAEBECEDEEEFF0F1F2F3F4F5F6F7F8F9FAFBFCFDFEFF".toCharArray();
    private static WeakReference<Activity> _activity = null;
    private static final Object _advertisingIdentifierMutex = new Object();
    private static boolean _aidDone = false;
    private static final Object _aidMutex = new Object();
    private static final Object _analyticsExecutorMutex = new Object();
    private static Config.ApplicationType _appType = Config.ApplicationType.APPLICATION_TYPE_HANDHELD;
    private static final Object _applicationIDMutex = new Object();
    private static final Object _applicationNameMutex = new Object();
    private static final Object _applicationVersionCodeMutex = new Object();
    private static final Object _applicationVersionMutex = new Object();
    private static final Object _audienceExecutorMutex = new Object();
    private static final Object _carrierMutex = new Object();
    static final Map<String, String> _contextDataKeyWhiteList = new HashMap(256);
    static int _contextDataKeyWhiteListCount = 0;
    private static final Object _currentActivityMutex = new Object();
    private static boolean _debugLogging;
    private static final Object _defaultDataMutex = new Object();
    private static boolean _isWearable = false;
    private static final Object _mediaExecutorMutex = new Object();
    private static final Object _messageImageCachingExecutorMutex = new Object();
    private static final Object _messagesExecutorMutex = new Object();
    private static final Object _operatingSystemMutex = new Object();
    private static final Object _piiExecutorMutex = new Object();
    private static final Object _pushEnabledMutex = new Object();
    private static final Object _pushIdentifierMutex = new Object();
    private static final Object _resolutionMutex = new Object();
    private static final Object _sharedExecutorMutex = new Object();
    private static final Object _sharedPreferencesMutex = new Object();
    private static final Object _thirdPartyCallbacksExecutorMutex = new Object();
    private static final Object _timedActionsExecutorMutex = new Object();
    private static final Object _timestampMutex = new Object();
    private static final Object _userAgentMutex = new Object();
    private static final Object _userIdentifierMutex = new Object();
    private static final Object _visitorIDMutex = new Object();
    private static String advertisingIdentifier = null;
    private static String aid = null;
    private static ExecutorService analyticsExecutor = null;
    private static String appID = null;
    private static String appName = null;
    private static ExecutorService audienceExecutor = null;
    private static String carrier = null;
    private static final boolean[] contextDataMask = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
    private static HashMap<String, Object> defaultData = null;
    private static final String[] encodedChars = {"%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07", "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F", "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17", "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F", "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27", "%28", "%29", qqvvqq.f664b04250425, "%2B", "%2C", "-", ".", "%2F", "0", "1", "2", "3", nnoonn.f260b0439043904390439, "5", "6", vqqqvq.f849b0432, "8", vqqqvq.f842b043204320432, "%3A", "%3B", "%3C", "%3D", "%3E", "%3F", "%40", "A", "B", "C", "D", nnoonn.f268b0439043904390439, "F", vqqqvq.f845b04320432, "H", "I", nnoonn.f261b0439043904390439, "K", "L", "M", "N", "O", vqvvqq.f909b04250425, "Q", "R", "S", "T", "U", ononon.f449b042204220422, vvqqvq.f943b04320432, "X", "Y", "Z", "%5B", "%5C", "%5D", "%5E", "_", "%60", "a", "b", "c", "d", "e", "f", "g", "h", "i", vvqqvq.f931b04320432043204320432, "k", "l", "m", vqqqvq.f846b04320432, "o", "p", qqvvqq.f655b0425042504250425, "r", "s", "t", "u", "v", "w", "x", "y", "z", "%7B", "%7C", "%7D", "%7E", "%7F", "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87", "%88", "%89", "%8A", "%8B", "%8C", "%8D", "%8E", "%8F", "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97", "%98", "%99", "%9A", "%9B", "%9C", "%9D", "%9E", "%9F", "%A0", "%A1", "%A2", "%A3", "%A4", "%A5", "%A6", "%A7", "%A8", "%A9", "%AA", "%AB", "%AC", "%AD", "%AE", "%AF", "%B0", "%B1", "%B2", "%B3", "%B4", "%B5", "%B6", "%B7", "%B8", "%B9", "%BA", "%BB", "%BC", "%BD", "%BE", "%BF", "%C0", "%C1", "%C2", "%C3", "%C4", "%C5", "%C6", "%C7", "%C8", "%C9", "%CA", "%CB", "%CC", "%CD", "%CE", "%CF", "%D0", "%D1", "%D2", "%D3", "%D4", "%D5", "%D6", "%D7", "%D8", "%D9", "%DA", "%DB", "%DC", "%DD", "%DE", "%DF", "%E0", "%E1", "%E2", "%E3", "%E4", "%E5", "%E6", "%E7", "%E8", "%E9", "%EA", "%EB", "%EC", "%ED", "%EE", "%EF", "%F0", "%F1", "%F2", "%F3", "%F4", "%F5", "%F6", "%F7", "%F8", "%F9", "%FA", "%FB", "%FC", "%FD", "%FE", "%FF"};
    private static final char[] hexArray = "0123456789abcdef".toCharArray();
    private static ExecutorService mediaExecutor = null;
    private static ExecutorService messageImageCachingExecutor = null;
    private static ExecutorService messagesExecutor = null;
    private static String operatingSystem = null;
    private static ExecutorService piiExecutor = null;
    private static SharedPreferences prefs = null;
    private static boolean pushEnabled = false;
    private static String pushIdentifier = null;
    private static String resolution = null;
    private static Context sharedContext = null;
    private static ExecutorService sharedExecutor = null;
    private static ExecutorService thirdPartyCallbacksExecutor = null;
    private static ExecutorService timedActionsExecutor = null;
    private static String timestamp = null;
    private static String userAgent = null;
    private static final boolean[] utf8Mask = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, true, true, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
    private static int versionCode = -1;
    private static String versionName = null;
    private static String visitorID = null;

    StaticMethods() {
    }

    protected static class NullContextException extends Exception {
        public NullContextException(String str) {
            super(str);
        }
    }

    protected static class NullActivityException extends Exception {
        public NullActivityException(String str) {
            super(str);
        }
    }

    protected static SharedPreferences getSharedPreferences() throws NullContextException {
        SharedPreferences sharedPreferences;
        synchronized (_sharedPreferencesMutex) {
            if (prefs == null) {
                prefs = getSharedContext().getSharedPreferences("APP_MEASUREMENT_CACHE", 0);
                if (prefs == null) {
                    logWarningFormat("Config - No SharedPreferences available", new Object[0]);
                }
            }
            if (prefs != null) {
                sharedPreferences = prefs;
            } else {
                throw new NullContextException("Config - No SharedPreferences available");
            }
        }
        return sharedPreferences;
    }

    protected static String getApplicationID() {
        String str;
        String str2;
        synchronized (_applicationIDMutex) {
            if (appID == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(getApplicationName() != null ? getApplicationName() : "");
                if (getApplicationVersion() == null || getApplicationVersion().length() <= 0) {
                    str2 = "";
                } else {
                    str2 = vqvvqq.f907b042504250425 + getApplicationVersion() + vqvvqq.f907b042504250425;
                }
                sb.append(str2);
                sb.append(getApplicationVersionCode() != 0 ? String.format(Locale.US, "(%d)", new Object[]{Integer.valueOf(getApplicationVersionCode())}) : "");
                appID = sb.toString();
                try {
                    SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferencesEditor();
                    sharedPreferencesEditor.putString("ADOBEMOBILE_STOREDDEFAULTS_APPID", appID);
                    sharedPreferencesEditor.commit();
                } catch (NullContextException e) {
                    logWarningFormat("Config - Unable to set Application ID in preferences (%s)", e.getLocalizedMessage());
                }
            }
            str = appID;
        }
        return str;
    }

    private static String getApplicationName() {
        String str;
        synchronized (_applicationNameMutex) {
            if (appName == null) {
                appName = "";
                try {
                    PackageManager packageManager = getSharedContext().getPackageManager();
                    if (packageManager != null) {
                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getSharedContext().getPackageName(), 0);
                        if (applicationInfo != null) {
                            appName = packageManager.getApplicationLabel(applicationInfo) != null ? (String) packageManager.getApplicationLabel(applicationInfo) : "";
                        } else {
                            logWarningFormat("Analytics - ApplicationInfo was null", new Object[0]);
                            appName = "";
                        }
                    } else {
                        logWarningFormat("Analytics - PackageManager was null", new Object[0]);
                        appName = "";
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    logWarningFormat("Analytics - PackageManager couldn't find application name (%s)", e.toString());
                    appName = "";
                } catch (NullContextException e2) {
                    logErrorFormat("Config - Unable to get package to pull application name. (%s)", e2.getMessage());
                    appName = "";
                }
            }
            str = appName;
        }
        return str;
    }

    protected static String getApplicationVersion() {
        String str;
        synchronized (_applicationVersionMutex) {
            if (versionName == null) {
                versionName = "";
                try {
                    PackageManager packageManager = getSharedContext().getPackageManager();
                    if (packageManager != null) {
                        PackageInfo packageInfo = packageManager.getPackageInfo(getSharedContext().getPackageName(), 0);
                        if (packageInfo != null) {
                            versionName = packageInfo.versionName != null ? packageInfo.versionName : "";
                        } else {
                            logWarningFormat("Analytics - PackageInfo was null", new Object[0]);
                            versionName = "";
                        }
                    } else {
                        logWarningFormat("Analytics - PackageManager was null", new Object[0]);
                        versionName = "";
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    logWarningFormat("Analytics - PackageManager couldn't find application version (%s)", e.toString());
                    versionName = "";
                } catch (NullContextException e2) {
                    logErrorFormat("Config - Unable to get package to pull application version. (%s)", e2.getMessage());
                    versionName = "";
                }
            }
            str = versionName;
        }
        return str;
    }

    protected static int getApplicationVersionCode() {
        int i;
        synchronized (_applicationVersionCodeMutex) {
            if (versionCode == -1) {
                try {
                    PackageManager packageManager = getSharedContext().getPackageManager();
                    if (packageManager != null) {
                        PackageInfo packageInfo = packageManager.getPackageInfo(getSharedContext().getPackageName(), 0);
                        if (packageInfo != null) {
                            versionCode = packageInfo.versionCode > 0 ? packageInfo.versionCode : 0;
                        } else {
                            logWarningFormat("Analytics - PackageInfo was null", new Object[0]);
                            versionCode = 0;
                        }
                    } else {
                        logWarningFormat("Analytics - PackageManager was null", new Object[0]);
                        versionCode = 0;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    logWarningFormat("Analytics - PackageManager couldn't find application version code (%s)", e.toString());
                    versionCode = 0;
                } catch (NullContextException e2) {
                    logErrorFormat("Config - Unable to get package to pull application version code. (%s)", e2.getMessage());
                    versionCode = 0;
                }
            }
            i = versionCode;
        }
        return i;
    }

    protected static String getVisitorID() {
        String str;
        synchronized (_visitorIDMutex) {
            if (visitorID == null) {
                try {
                    visitorID = getSharedPreferences().getString("APP_MEASUREMENT_VISITOR_ID", (String) null);
                } catch (NullContextException e) {
                    logErrorFormat("Config - Unable to pull visitorID from shared preferences. (%s)", e.getMessage());
                }
            }
            str = visitorID;
        }
        return str;
    }

    protected static String getPushIdentifier() {
        String str;
        synchronized (_pushIdentifierMutex) {
            str = pushIdentifier;
        }
        return str;
    }

    protected static String getAdvertisingIdentifier() {
        String str;
        synchronized (_advertisingIdentifierMutex) {
            str = advertisingIdentifier;
        }
        return str;
    }

    protected static String getDefaultUserAgent() {
        String str;
        synchronized (_userAgentMutex) {
            if (userAgent == null) {
                userAgent = "Mozilla/5.0 (Linux; U; Android " + getAndroidVersion() + "; " + getDefaultAcceptLanguage() + "; " + Build.MODEL + " Build/" + Build.ID + ")";
            }
            str = userAgent;
        }
        return str;
    }

    protected static HashMap<String, Object> getDefaultData() {
        HashMap<String, Object> hashMap;
        synchronized (_defaultDataMutex) {
            if (defaultData == null) {
                defaultData = new HashMap<>();
                defaultData.put("a.DeviceName", Build.MODEL);
                defaultData.put("a.Resolution", getResolution());
                defaultData.put("a.OSVersion", getOperatingSystem());
                defaultData.put("a.CarrierName", getCarrier());
                defaultData.put("a.AppID", getApplicationID());
                defaultData.put("a.RunMode", isWearableApp() ? "Extension" : "Application");
            }
            hashMap = defaultData;
        }
        return hashMap;
    }

    protected static String getResolution() {
        String str;
        synchronized (_resolutionMutex) {
            if (resolution == null) {
                try {
                    DisplayMetrics displayMetrics = getSharedContext().getResources().getDisplayMetrics();
                    resolution = displayMetrics.widthPixels + "x" + displayMetrics.heightPixels;
                } catch (NullContextException e) {
                    logErrorFormat("Config - Error getting device resolution. (%s)", e.getMessage());
                }
            }
            str = resolution;
        }
        return str;
    }

    protected static String getCarrier() {
        String str;
        synchronized (_carrierMutex) {
            if (carrier == null) {
                try {
                    carrier = ((TelephonyManager) getSharedContext().getSystemService("phone")).getNetworkOperatorName();
                } catch (NullContextException e) {
                    logErrorFormat("Config - Error getting device carrier. (%s)", e.getMessage());
                }
            }
            str = carrier;
        }
        return str;
    }

    protected static String getOperatingSystem() {
        String str;
        synchronized (_operatingSystemMutex) {
            if (operatingSystem == null) {
                operatingSystem = "Android " + getAndroidVersion();
                try {
                    SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferencesEditor();
                    sharedPreferencesEditor.putString("ADOBEMOBILE_STOREDDEFAULTS_OS", operatingSystem);
                    sharedPreferencesEditor.commit();
                } catch (NullContextException e) {
                    logWarningFormat("Config - Unable to set OS version in preferences (%s)", e.getLocalizedMessage());
                }
            }
            str = operatingSystem;
        }
        return str;
    }

    protected static String getTimestampString() {
        String str;
        synchronized (_timestampMutex) {
            if (timestamp == null) {
                Date date = new Date();
                Calendar instance = Calendar.getInstance();
                instance.setTime(date);
                timestamp = "00/00/0000 00:00:00 0 " + ((instance.getTimeZone().getOffset(1, instance.get(1), instance.get(2), instance.get(5), instance.get(7), (((((instance.get(11) * 60) + instance.get(12)) * 60) + instance.get(13)) * 1000) + instance.get(14)) / 60000) * -1);
            }
            str = timestamp;
        }
        return str;
    }

    protected static File getCacheDirectory() {
        try {
            return getSharedContext().getCacheDir();
        } catch (NullContextException e) {
            logErrorFormat("Config - Error getting cache directory. (%s)", e.getMessage());
            return null;
        }
    }

    protected static ExecutorService getAnalyticsExecutor() {
        ExecutorService executorService;
        synchronized (_analyticsExecutorMutex) {
            if (analyticsExecutor == null) {
                analyticsExecutor = Executors.newSingleThreadExecutor();
            }
            executorService = analyticsExecutor;
        }
        return executorService;
    }

    protected static ExecutorService getMessagesExecutor() {
        ExecutorService executorService;
        synchronized (_messagesExecutorMutex) {
            if (messagesExecutor == null) {
                messagesExecutor = Executors.newSingleThreadExecutor();
            }
            executorService = messagesExecutor;
        }
        return executorService;
    }

    protected static ExecutorService getThirdPartyCallbacksExecutor() {
        ExecutorService executorService;
        synchronized (_thirdPartyCallbacksExecutorMutex) {
            if (thirdPartyCallbacksExecutor == null) {
                thirdPartyCallbacksExecutor = Executors.newSingleThreadExecutor();
            }
            executorService = thirdPartyCallbacksExecutor;
        }
        return executorService;
    }

    protected static ExecutorService getMessageImageCachingExecutor() {
        ExecutorService executorService;
        synchronized (_messageImageCachingExecutorMutex) {
            if (messageImageCachingExecutor == null) {
                messageImageCachingExecutor = Executors.newSingleThreadExecutor();
            }
            executorService = messageImageCachingExecutor;
        }
        return executorService;
    }

    protected static ExecutorService getAudienceExecutor() {
        ExecutorService executorService;
        synchronized (_audienceExecutorMutex) {
            if (audienceExecutor == null) {
                audienceExecutor = Executors.newSingleThreadExecutor();
            }
            executorService = audienceExecutor;
        }
        return executorService;
    }

    protected static ExecutorService getPIIExecutor() {
        ExecutorService executorService;
        synchronized (_piiExecutorMutex) {
            if (piiExecutor == null) {
                piiExecutor = Executors.newSingleThreadExecutor();
            }
            executorService = piiExecutor;
        }
        return executorService;
    }

    protected static String getAID() {
        if (!MobileConfig.getInstance().mobileUsingAnalytics()) {
            return null;
        }
        synchronized (_aidMutex) {
            if (!_aidDone) {
                _aidDone = true;
                try {
                    boolean z = getSharedPreferences().getBoolean("ADOBEMOBILE_STOREDDEFAULTS_IGNORE_AID", false);
                    aid = getSharedPreferences().getString("ADOBEMOBILE_STOREDDEFAULTS_AID", (String) null);
                    if ((!z && aid == null) || (!MobileConfig.getInstance().getVisitorIdServiceEnabled() && z)) {
                        aid = retrieveAIDFromVisitorIDService();
                        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferencesEditor();
                        if (aid != null) {
                            sharedPreferencesEditor.putString("ADOBEMOBILE_STOREDDEFAULTS_AID", aid);
                            sharedPreferencesEditor.putBoolean("ADOBEMOBILE_STOREDDEFAULTS_IGNORE_AID", false);
                        } else {
                            sharedPreferencesEditor.putBoolean("ADOBEMOBILE_STOREDDEFAULTS_IGNORE_AID", true);
                        }
                        sharedPreferencesEditor.commit();
                        syncAIDIfNeeded(aid);
                    }
                } catch (NullContextException e) {
                    logErrorFormat("Config - Error getting AID. (%s)", e.getMessage());
                }
            }
        }
        return aid;
    }

    private static void syncAIDIfNeeded(String str) {
        boolean z;
        if (str != null && MobileConfig.getInstance().getVisitorIdServiceEnabled()) {
            try {
                z = getSharedPreferences().getBoolean("ADOBEMOBILE_STOREDDEFAULTS_AID_SYNCED", false);
            } catch (NullContextException e) {
                logWarningFormat("Visitor ID - Null context when attempting to determine visitor ID sync status (%s)", e.getLocalizedMessage());
                z = false;
            }
            if (!z) {
                HashMap hashMap = new HashMap();
                hashMap.put("AVID", str);
                VisitorIDService.sharedInstance().idSync(hashMap);
                try {
                    SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferencesEditor();
                    sharedPreferencesEditor.putBoolean("ADOBEMOBILE_STOREDDEFAULTS_AID_SYNCED", true);
                    sharedPreferencesEditor.commit();
                } catch (NullContextException e2) {
                    logWarningFormat("Visitor ID - Null context when attempting to persist visitor ID sync status (%s)", e2.getLocalizedMessage());
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x009a  */
    /* JADX WARNING: Removed duplicated region for block: B:24:? A[RETURN, SYNTHETIC] */
    private static String retrieveAIDFromVisitorIDService() {
        String str;
        StringBuilder sb = new StringBuilder(64);
        sb.append(MobileConfig.getInstance().getSSL() ? "https" : "http");
        sb.append("://");
        sb.append(MobileConfig.getInstance().getTrackingServer());
        sb.append("/id");
        boolean visitorIdServiceEnabled = MobileConfig.getInstance().getVisitorIdServiceEnabled();
        if (visitorIdServiceEnabled) {
            sb.append(VisitorIDService.sharedInstance().getAnalyticsIDRequestParameterString());
        }
        logDebugFormat("Analytics ID - Sending Analytics ID call(%s)", sb);
        byte[] retrieveData = RequestHandler.retrieveData(sb.toString(), (Map<String, String>) null, 500, "Analytics ID");
        if (retrieveData != null) {
            try {
                str = new JSONObject(new String(retrieveData, "UTF-8")).getString("id");
            } catch (UnsupportedEncodingException e) {
                logErrorFormat("Analytics ID - Unable to decode /id response(%s)", e.getLocalizedMessage());
            } catch (JSONException e2) {
                if (!visitorIdServiceEnabled) {
                    logErrorFormat("Analytics ID - Unable to parse /id response(%s)", e2.getLocalizedMessage());
                }
            }
            if (str != null && str.length() == 33) {
                return str;
            }
            if (visitorIdServiceEnabled) {
                return null;
            }
            return generateAID();
        }
        str = null;
        if (str != null || str.length() == 33) {
        }
    }

    private static String generateAID() {
        String upperCase = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.US);
        Pattern compile = Pattern.compile("^[89A-F]");
        Pattern compile2 = Pattern.compile("^[4-9A-F]");
        Matcher matcher = compile.matcher(upperCase.substring(0, 16));
        Matcher matcher2 = compile2.matcher(upperCase.substring(16, 32));
        SecureRandom secureRandom = new SecureRandom();
        String replaceAll = matcher.replaceAll(String.valueOf(secureRandom.nextInt(7)));
        String replaceAll2 = matcher2.replaceAll(String.valueOf(secureRandom.nextInt(3)));
        return replaceAll + "-" + replaceAll2;
    }

    protected static void setDebugLogging(boolean z) {
        _debugLogging = z;
    }

    protected static boolean getDebugLogging() {
        return _debugLogging;
    }

    protected static void setApplicationType(Config.ApplicationType applicationType) {
        _appType = applicationType;
        _isWearable = _appType == Config.ApplicationType.APPLICATION_TYPE_WEARABLE;
    }

    protected static boolean isWearableApp() {
        return _isWearable;
    }

    protected static void serializeToQueryString(Map<String, Object> map, StringBuilder sb) {
        if (map != null) {
            for (Map.Entry next : map.entrySet()) {
                String URLEncode = URLEncode((String) next.getKey());
                if (URLEncode != null) {
                    Object value = next.getValue();
                    if (value instanceof ContextData) {
                        ContextData contextData = (ContextData) value;
                        if (contextData.value != null) {
                            serializeKeyValuePair(URLEncode, contextData.value, sb);
                        }
                        if (contextData.contextData != null && contextData.contextData.size() > 0) {
                            sb.append("&");
                            sb.append(URLEncode);
                            sb.append(".");
                            serializeToQueryString(contextData.contextData, sb);
                            sb.append("&.");
                            sb.append(URLEncode);
                        }
                    } else {
                        serializeKeyValuePair(URLEncode, value, sb);
                    }
                }
            }
        }
    }

    private static void serializeKeyValuePair(String str, Object obj, StringBuilder sb) {
        if (str != null && obj != null && !(obj instanceof ContextData) && str.length() > 0) {
            if (!(obj instanceof String) || ((String) obj).length() > 0) {
                sb.append("&");
                sb.append(str);
                sb.append("=");
                if (obj instanceof List) {
                    sb.append(URLEncode(join((List) obj, ",")));
                } else {
                    sb.append(URLEncode(obj.toString()));
                }
            }
        }
    }

    protected static String join(Iterable<?> iterable, String str) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = iterable.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (!it.hasNext()) {
                break;
            }
            sb.append(str);
        }
        return sb.toString();
    }

    protected static String URLEncode(String str) {
        if (str == null) {
            return null;
        }
        try {
            byte[] bytes = str.getBytes("UTF-8");
            int length = bytes.length;
            int i = 0;
            while (i < length && utf8Mask[bytes[i] & 255]) {
                i++;
            }
            if (i == length) {
                return str;
            }
            StringBuilder sb = new StringBuilder(bytes.length << 1);
            if (i > 0) {
                sb.append(new String(bytes, 0, i, "UTF-8"));
            }
            while (i < length) {
                sb.append(encodedChars[bytes[i] & 255]);
                i++;
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            logWarningFormat("UnsupportedEncodingException : " + e.getMessage(), new Object[0]);
            return null;
        }
    }

    protected static void logErrorFormat(String str, Object... objArr) {
        if (objArr == null || objArr.length <= 0) {
            Log.e("ADBMobile", "ADBMobile Error : " + str);
            return;
        }
        Log.e("ADBMobile", String.format("ADBMobile Error : " + str, objArr));
    }

    protected static void logWarningFormat(String str, Object... objArr) {
        if (!getDebugLogging()) {
            return;
        }
        if (objArr == null || objArr.length <= 0) {
            Log.w("ADBMobile", "ADBMobile Warning : " + str);
            return;
        }
        Log.w("ADBMobile", String.format("ADBMobile Warning : " + str, objArr));
    }

    protected static void logDebugFormat(String str, Object... objArr) {
        if (!getDebugLogging()) {
            return;
        }
        if (objArr == null || objArr.length <= 0) {
            Log.d("ADBMobile", "ADBMobile Debug : " + str);
            return;
        }
        Log.d("ADBMobile", String.format("ADBMobile Debug : " + str, objArr));
    }

    protected static ContextData translateContextData(Map<String, Object> map) {
        ContextData contextData = new ContextData();
        for (Map.Entry next : cleanContextDataDictionary(map).entrySet()) {
            String str = (String) next.getKey();
            ArrayList arrayList = new ArrayList();
            int i = 0;
            while (true) {
                int indexOf = str.indexOf(46, i);
                if (indexOf < 0) {
                    break;
                }
                arrayList.add(str.substring(i, indexOf));
                i = indexOf + 1;
            }
            arrayList.add(str.substring(i, str.length()));
            addValueToHashMap(next.getValue(), contextData, arrayList, 0);
        }
        return contextData;
    }

    protected static Map<String, Object> cleanContextDataDictionary(Map<String, Object> map) {
        if (map == null) {
            return new HashMap();
        }
        HashMap hashMap = new HashMap();
        for (Map.Entry next : map.entrySet()) {
            String cleanContextDataKey = cleanContextDataKey((String) next.getKey());
            if (cleanContextDataKey != null) {
                hashMap.put(cleanContextDataKey, next.getValue());
            }
        }
        return hashMap;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r3 = r12.getBytes("UTF-8");
        r4 = new byte[r3.length];
        r5 = r3.length;
        r6 = 0;
        r7 = 0;
        r8 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0025, code lost:
        if (r6 >= r5) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0027, code lost:
        r10 = r3[r6];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0029, code lost:
        if (r10 != 46) goto L_0x002e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002b, code lost:
        if (r8 != 46) goto L_0x002e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0034, code lost:
        if (contextDataMask[r10 & 255] == false) goto L_0x003c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0036, code lost:
        r4[r7] = r10;
        r7 = r7 + 1;
        r8 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003c, code lost:
        r6 = r6 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x003f, code lost:
        if (r7 != 0) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0041, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0044, code lost:
        if (r4[0] != 46) goto L_0x0048;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0046, code lost:
        r3 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0048, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x004d, code lost:
        if (r4[r7 - 1] != 46) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x004f, code lost:
        r5 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0051, code lost:
        r5 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0052, code lost:
        r7 = (r7 - r5) - r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0054, code lost:
        if (r7 > 0) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0056, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0057, code lost:
        r5 = new java.lang.String(r4, r3, r7, "UTF-8");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x005e, code lost:
        r3 = _contextDataKeyWhiteList;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0060, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0065, code lost:
        if (_contextDataKeyWhiteListCount <= 250) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0067, code lost:
        _contextDataKeyWhiteList.clear();
        _contextDataKeyWhiteListCount = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x006e, code lost:
        _contextDataKeyWhiteList.put(r12, r5);
        _contextDataKeyWhiteListCount++;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0078, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0079, code lost:
        return r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x007d, code lost:
        r12 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x007e, code lost:
        logErrorFormat("Analytics - Unable to clean context data key (%s)", r12.getLocalizedMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x008b, code lost:
        return null;
     */
    protected static String cleanContextDataKey(String str) {
        if (str == null) {
            return null;
        }
        synchronized (_contextDataKeyWhiteList) {
            String str2 = _contextDataKeyWhiteList.get(str);
            if (str2 != null) {
                return str2;
            }
        }
    }

    private static void addValueToHashMap(Object obj, ContextData contextData, List<String> list, int i) {
        if (contextData != null && list != null) {
            int size = list.size();
            String str = i < size ? list.get(i) : null;
            if (str != null) {
                ContextData contextData2 = new ContextData();
                if (contextData.containsKey(str)) {
                    contextData2 = contextData.get(str);
                }
                if (size - 1 == i) {
                    contextData2.value = obj;
                    contextData.put(str, contextData2);
                    return;
                }
                contextData.put(str, contextData2);
                addValueToHashMap(obj, contextData2, list, i + 1);
            }
        }
    }

    protected static long getTimeSince1970() {
        return System.currentTimeMillis() / 1000;
    }

    protected static void updateLastKnownTimestamp(Long l) {
        MobileConfig instance = MobileConfig.getInstance();
        if (instance == null) {
            logErrorFormat("Config - Lost config instance", new Object[0]);
        } else if (instance.getOfflineTrackingEnabled()) {
            try {
                SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferencesEditor();
                sharedPreferencesEditor.putLong("ADBLastKnownTimestampKey", l.longValue());
                sharedPreferencesEditor.commit();
            } catch (NullContextException e) {
                logErrorFormat("Config - Error while updating last known timestamp. (%s)", e.getMessage());
            }
        }
    }

    protected static String getDefaultAcceptLanguage() {
        Configuration configuration;
        Locale locale;
        try {
            Resources resources = getSharedContext().getResources();
            if (resources == null || (configuration = resources.getConfiguration()) == null || (locale = configuration.locale) == null) {
                return null;
            }
            return locale.toString().replace('_', '-');
        } catch (NullContextException e) {
            logErrorFormat("Config - Error getting application resources for default accepted language. (%s)", e.getMessage());
            return null;
        }
    }

    private static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    protected static SharedPreferences.Editor getSharedPreferencesEditor() throws NullContextException {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        if (edit != null) {
            return edit;
        }
        throw new NullContextException("Config - Unable to create an instance of a SharedPreferences Editor");
    }

    protected static Context getSharedContext() throws NullContextException {
        Context context = sharedContext;
        if (context != null) {
            return context;
        }
        throw new NullContextException("Config - No Application Context (Application context must be set prior to calling any library functions.)");
    }

    protected static void setSharedContext(Context context) {
        if (context == null) {
            return;
        }
        if (context instanceof Activity) {
            sharedContext = context.getApplicationContext();
        } else {
            sharedContext = context;
        }
    }

    protected static long getTimeSinceLaunch() {
        long timeSince1970 = getTimeSince1970() - Lifecycle.sessionStartTime;
        if (timeSince1970 < 604800) {
            return timeSince1970;
        }
        return 0;
    }

    protected static HashMap<String, Object> mapFromJson(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        Iterator<String> keys = jSONObject.keys();
        HashMap<String, Object> hashMap = new HashMap<>();
        while (keys.hasNext()) {
            String next = keys.next();
            try {
                hashMap.put(next, jSONObject.getString(next));
            } catch (JSONException e) {
                logWarningFormat("Problem parsing json data (%s)", e.getLocalizedMessage());
            }
        }
        return hashMap;
    }

    protected static void setCurrentActivity(Activity activity) {
        synchronized (_currentActivityMutex) {
            _activity = new WeakReference<>(activity);
        }
    }

    protected static Activity getCurrentActivity() throws NullActivityException {
        Activity activity;
        synchronized (_currentActivityMutex) {
            if (_activity == null || _activity.get() == null) {
                throw new NullActivityException("Message - No Current Activity (Messages must have a reference to the current activity to work properly)");
            }
            activity = (Activity) _activity.get();
        }
        return activity;
    }

    protected static String expandTokens(String str, Map<String, String> map) {
        try {
            for (Map.Entry next : map.entrySet()) {
                str = str.replace((CharSequence) next.getKey(), (CharSequence) next.getValue());
            }
        } catch (Exception e) {
            logDebugFormat("Unable to expand tokens (%s)", e.toString());
        }
        return str;
    }

    protected static int getCurrentOrientation() {
        try {
            return getCurrentActivity().getResources().getConfiguration().orientation;
        } catch (NullActivityException unused) {
            return -1;
        }
    }

    protected static String getHexString(String str) {
        if (str == null || str.length() <= 0) {
            return null;
        }
        int i = 0;
        try {
            byte[] bytes = str.getBytes("UTF-8");
            int length = bytes.length;
            char[] cArr = new char[(length << 1)];
            int i2 = 0;
            while (i < length) {
                int i3 = i + 1;
                int i4 = (bytes[i] & 255) << 1;
                int i5 = i2 + 1;
                char[] cArr2 = BYTE_TO_HEX;
                cArr[i2] = cArr2[i4];
                i2 = i5 + 1;
                cArr[i5] = cArr2[i4 + 1];
                i = i3;
            }
            return new String(cArr);
        } catch (UnsupportedEncodingException e) {
            logDebugFormat("Failed to get hex from string (%s)", e.getMessage());
            return null;
        }
    }

    protected static String hexToString(String str) {
        if (str == null || str.length() <= 0 || str.length() % 2 != 0) {
            return null;
        }
        int length = str.length();
        byte[] bArr = new byte[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        try {
            return new String(bArr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logDebugFormat("Failed to get string from hex (%s)", e.getMessage());
            return null;
        }
    }

    protected static String appendContextData(Map<String, Object> map, String str) {
        String group;
        if (str == null || map == null || map.size() == 0) {
            return str;
        }
        Matcher matcher = Pattern.compile(".*(&c\\.(.*)&\\.c).*").matcher(str);
        if (!matcher.matches() || (group = matcher.group(2)) == null) {
            return str;
        }
        HashMap hashMap = new HashMap(64);
        ArrayList arrayList = new ArrayList(16);
        for (String str2 : group.split("&")) {
            if (str2.endsWith(".") && !str2.contains("=")) {
                arrayList.add(str2);
            } else if (!str2.startsWith(".")) {
                String[] split = str2.split("=");
                if (split.length == 2) {
                    try {
                        hashMap.put(contextDataStringPath(arrayList, split[0]), URLDecoder.decode(split[1], "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            } else if (arrayList.size() > 0) {
                arrayList.remove(arrayList.size() - 1);
            }
        }
        hashMap.putAll(map);
        StringBuilder sb = new StringBuilder(str.substring(0, matcher.start(1)));
        HashMap hashMap2 = new HashMap();
        hashMap2.put("c", translateContextData(hashMap));
        serializeToQueryString(hashMap2, sb);
        sb.append(str.substring(matcher.end(1)));
        return sb.toString();
    }

    protected static String contextDataStringPath(List<String> list, String str) {
        StringBuilder sb = new StringBuilder();
        for (String append : list) {
            sb.append(append);
        }
        sb.append(str);
        return sb.toString();
    }

    private static Locale getDeviceLocale() {
        try {
            Resources resources = getSharedContext().getResources();
            if (resources == null) {
                return Locale.US;
            }
            Configuration configuration = resources.getConfiguration();
            if (configuration == null) {
                return Locale.US;
            }
            return configuration.locale != null ? configuration.locale : Locale.US;
        } catch (NullContextException e) {
            logErrorFormat("Config - Error getting application resources for device locale. (%s)", e.getMessage());
            return Locale.US;
        }
    }

    protected static String getIso8601Date() {
        return getIso8601Date(new Date());
    }

    protected static String getIso8601Date(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", getDeviceLocale());
        if (date == null) {
            date = new Date();
        }
        return simpleDateFormat.format(date);
    }
}
