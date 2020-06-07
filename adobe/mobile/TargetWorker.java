package com.adobe.mobile;

import android.content.SharedPreferences;
import com.adobe.mobile.StaticMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

final class TargetWorker {
    private static final Object _checkOldCookiesMutex = new Object();
    private static String _edgeHost = null;
    private static boolean _oldCookiesHaveBeenChecked = false;
    private static HashMap<String, Object> _persistentParameters = null;
    private static final Object _persistentParametersMutex = new Object();
    private static String _sessionId = null;
    private static String _thirdPartyId = null;
    private static final Object _thirdPartyIdMutex = new Object();
    private static String _tntId = null;
    private static final Object _tntIdMutex = new Object();
    private static final List<String> cacheMboxAcceptedKeys = Arrays.asList(new String[]{"mbox", "parameters", "product", "order", "content", "eventTokens", "clientSideAnalyticsLoggingPayload", "errorType", "profileScriptToken"});
    private static Map<String, JSONObject> cachedMboxes = new HashMap();
    private static List<JSONObject> notifications = new ArrayList();

    TargetWorker() {
    }

    protected static void setThirdPartyId(String str) {
        synchronized (_thirdPartyIdMutex) {
            if (_thirdPartyId == null || str == null || !_thirdPartyId.equals(str)) {
                _thirdPartyId = str;
                try {
                    SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                    if (isNullOrEmpty(_thirdPartyId)) {
                        sharedPreferencesEditor.remove("ADBMOBILE_TARGET_3RD_PARTY_ID");
                    } else {
                        sharedPreferencesEditor.putString("ADBMOBILE_TARGET_3RD_PARTY_ID", _thirdPartyId);
                    }
                    sharedPreferencesEditor.commit();
                } catch (StaticMethods.NullContextException unused) {
                    StaticMethods.logErrorFormat("Target - Error retrieving shared preferences - application context is null", new Object[0]);
                }
            }
        }
    }

    protected static void setTntId(String str) {
        synchronized (_tntIdMutex) {
            if (!tntIdValuesAreEqual(_tntId, str)) {
                _tntId = str;
                try {
                    SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                    if (isNullOrEmpty(_tntId)) {
                        sharedPreferencesEditor.remove("ADBMOBILE_TARGET_TNT_ID");
                    } else {
                        sharedPreferencesEditor.putString("ADBMOBILE_TARGET_TNT_ID", _tntId);
                    }
                    sharedPreferencesEditor.commit();
                } catch (StaticMethods.NullContextException unused) {
                    StaticMethods.logErrorFormat("Target - Error retrieving shared preferences - application context is null", new Object[0]);
                }
            }
        }
    }

    protected static void setTntIdFromOldCookieValues() {
        synchronized (_checkOldCookiesMutex) {
            if (!_oldCookiesHaveBeenChecked) {
                String checkForOldCookieValue = checkForOldCookieValue("mboxPC");
                if (checkForOldCookieValue != null) {
                    setTntId(checkForOldCookieValue);
                }
                _oldCookiesHaveBeenChecked = true;
            }
        }
    }

    private static String checkForOldCookieValue(String str) {
        String str2 = null;
        if (str == null || str.length() == 0) {
            return null;
        }
        try {
            SharedPreferences sharedPreferences = StaticMethods.getSharedPreferences();
            if (sharedPreferences.contains(str + "_Expires")) {
                if (sharedPreferences.getLong(str + "_Expires", 0) > System.currentTimeMillis()) {
                    String string = sharedPreferences.getString(str + "_Value", "");
                    if (string.length() > 0) {
                        str2 = string;
                    }
                }
                SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                sharedPreferencesEditor.remove(str + "_Value");
                sharedPreferencesEditor.remove(str + "_Expires");
                sharedPreferencesEditor.commit();
            }
        } catch (StaticMethods.NullContextException unused) {
            StaticMethods.logErrorFormat("Target - Error retrieving shared preferences - application context is null", new Object[0]);
        }
        return str2;
    }

    private static boolean tntIdValuesAreEqual(String str, String str2) {
        if (str == null && str2 == null) {
            return true;
        }
        if (str == null || str2 == null) {
            return false;
        }
        if (str.equals(str2)) {
            return true;
        }
        int indexOf = str.indexOf(46);
        if (indexOf != -1) {
            str = str.substring(0, indexOf);
        }
        int indexOf2 = str2.indexOf(46);
        if (indexOf2 != -1) {
            str2 = str2.substring(0, indexOf2);
        }
        return str.equals(str2);
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
