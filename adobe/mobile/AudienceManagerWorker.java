package com.adobe.mobile;

import android.content.SharedPreferences;
import com.adobe.mobile.AudienceManager;
import com.adobe.mobile.StaticMethods;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import oooooo.vvqqvq;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class AudienceManagerWorker {
    private static volatile boolean UrlPrefix_pred = true;
    private static volatile boolean VisitorProfile_pred = true;
    private static String _dpid;
    private static String _dpuuid;
    private static String _urlPrefix;
    private static HashMap<String, Object> _visitorProfile;

    AudienceManagerWorker() {
    }

    public static class SubmitSignalTask implements Runnable {
        public final AudienceManager.AudienceManagerCallback<Map<String, Object>> callback;
        public final Map<String, Object> data;

        public SubmitSignalTask(Map<String, Object> map, AudienceManager.AudienceManagerCallback<Map<String, Object>> audienceManagerCallback) {
            this.data = map;
            this.callback = audienceManagerCallback;
        }

        public void run() {
            Thread thread;
            final HashMap hashMap = new HashMap();
            try {
                if (!MobileConfig.getInstance().mobileUsingAudienceManager()) {
                    if (this.callback != null) {
                        new Thread(new Runnable() {
                            public void run() {
                                SubmitSignalTask.this.callback.call(hashMap);
                            }
                        }).start();
                    }
                } else if (MobileConfig.getInstance().getPrivacyStatus() == MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_OUT) {
                    StaticMethods.logDebugFormat("Audience Manager - Privacy status is set to opt out, no signals will be submitted.", new Object[0]);
                    if (this.callback != null) {
                        new Thread(new Runnable() {
                            public void run() {
                                SubmitSignalTask.this.callback.call(hashMap);
                            }
                        }).start();
                    }
                } else {
                    String access$400 = AudienceManagerWorker.BuildSchemaRequest(this.data);
                    if (access$400.length() <= 1) {
                        StaticMethods.logWarningFormat("Audience Manager - Unable to create URL object", new Object[0]);
                        if (this.callback != null) {
                            new Thread(new Runnable() {
                                public void run() {
                                    SubmitSignalTask.this.callback.call(hashMap);
                                }
                            }).start();
                            return;
                        }
                        return;
                    }
                    StaticMethods.logDebugFormat("Audience Manager - request (%s)", access$400);
                    byte[] retrieveData = RequestHandler.retrieveData(access$400, (Map<String, String>) null, MobileConfig.getInstance().getAamTimeout() * 1000, "Audience Manager");
                    String str = "";
                    if (retrieveData != null && retrieveData.length > 0) {
                        str = new String(retrieveData, "UTF-8");
                    }
                    hashMap.putAll(AudienceManagerWorker.processJsonResponse(new JSONObject(str)));
                    if (this.callback != null) {
                        thread = new Thread(new Runnable() {
                            public void run() {
                                SubmitSignalTask.this.callback.call(hashMap);
                            }
                        });
                        thread.start();
                    }
                }
            } catch (UnsupportedEncodingException e) {
                StaticMethods.logWarningFormat("Audience Manager - Unable to decode server response (%s)", e.getLocalizedMessage());
                if (this.callback != null) {
                    thread = new Thread(new Runnable() {
                        public void run() {
                            SubmitSignalTask.this.callback.call(hashMap);
                        }
                    });
                }
            } catch (JSONException e2) {
                StaticMethods.logWarningFormat("Audience Manager - Unable to parse JSON data (%s)", e2.getLocalizedMessage());
                if (this.callback != null) {
                    thread = new Thread(new Runnable() {
                        public void run() {
                            SubmitSignalTask.this.callback.call(hashMap);
                        }
                    });
                }
            } catch (Exception e3) {
                StaticMethods.logWarningFormat("Audience Manager - Unexpected error parsing result (%s)", e3.getLocalizedMessage());
                if (this.callback != null) {
                    thread = new Thread(new Runnable() {
                        public void run() {
                            SubmitSignalTask.this.callback.call(hashMap);
                        }
                    });
                }
            } catch (Throwable th) {
                if (this.callback != null) {
                    new Thread(new Runnable() {
                        public void run() {
                            SubmitSignalTask.this.callback.call(hashMap);
                        }
                    }).start();
                }
                throw th;
            }
        }
    }

    public static void SubmitSignal(Map<String, Object> map, AudienceManager.AudienceManagerCallback<Map<String, Object>> audienceManagerCallback) {
        if (MobileConfig.getInstance().getPrivacyStatus() == MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_OUT) {
            StaticMethods.logDebugFormat("Audience Manager - Ignoring signal due to privacy status being opted out", new Object[0]);
            if (audienceManagerCallback != null) {
                audienceManagerCallback.call(null);
                return;
            }
            return;
        }
        StaticMethods.getAudienceExecutor().execute(new SubmitSignalTask(map, audienceManagerCallback));
    }

    public static void Reset() {
        StaticMethods.getAudienceExecutor().execute(new Runnable() {
            public void run() {
                AudienceManagerWorker.SetUUID((String) null);
                AudienceManagerWorker.SetVisitorProfile((Map<String, Object>) null);
            }
        });
    }

    protected static HashMap<String, Object> processJsonResponse(JSONObject jSONObject) {
        ProcessDestsArray(jSONObject);
        try {
            SetUUID(jSONObject.getString("uuid"));
        } catch (JSONException e) {
            StaticMethods.logWarningFormat("Audience Manager - Unable to parse JSON data (%s)", e.getLocalizedMessage());
        }
        HashMap<String, Object> ProcessStuffArray = ProcessStuffArray(jSONObject);
        if (ProcessStuffArray.size() > 0) {
            StaticMethods.logDebugFormat("Audience Manager - response (%s)", ProcessStuffArray);
        } else {
            StaticMethods.logWarningFormat("Audience Manager - response was empty", new Object[0]);
        }
        SetVisitorProfile(ProcessStuffArray);
        return ProcessStuffArray;
    }

    /* access modifiers changed from: private */
    public static String BuildSchemaRequest(Map<String, Object> map) {
        if (GetUrlPrefix() == null) {
            return null;
        }
        return (GetUrlPrefix() + GetCustomUrlVariables(map) + GetDataProviderUrlVariables() + "&d_ptfm=android&d_dst=1&d_rtbd=json").replace("?&", "?");
    }

    private static String GetCustomUrlVariables(Map<String, Object> map) {
        if (map == null || map.size() <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(1024);
        for (Map.Entry next : map.entrySet()) {
            String str = (String) next.getKey();
            Object value = next.getValue();
            if (str != null && str.length() > 0 && value != null && value.toString().length() > 0 && next.getValue().getClass() == String.class) {
                sb.append("&");
                sb.append("c_");
                sb.append(StaticMethods.URLEncode(SanitizeUrlVariableName(str)));
                sb.append("=");
                sb.append(StaticMethods.URLEncode(value.toString()));
            }
        }
        return sb.toString();
    }

    private static String SanitizeUrlVariableName(String str) {
        return str.replace(".", "_");
    }

    private static String GetDataProviderUrlVariables() {
        String str;
        StringBuilder sb = new StringBuilder();
        if (MobileConfig.getInstance().getVisitorIdServiceEnabled()) {
            sb.append(VisitorIDService.sharedInstance().getAAMParameterString());
        }
        if (GetUUID() != null) {
            sb.append("&");
            sb.append("d_uuid");
            sb.append("=");
            sb.append(GetUUID());
        }
        String str2 = _dpid;
        if (str2 != null && str2.length() > 0 && (str = _dpuuid) != null && str.length() > 0) {
            String str3 = _dpuuid;
            try {
                str3 = StaticMethods.URLEncode(URLDecoder.decode(str3.replace(vvqqvq.f936b043204320432, "%2B"), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                StaticMethods.logDebugFormat("Audience Manager", "Unable to properly encode dpuuid (%s).  Sending original value.", e);
            }
            sb.append("&");
            sb.append("d_dpid");
            sb.append("=");
            sb.append(_dpid);
            sb.append("&");
            sb.append("d_dpuuid");
            sb.append("=");
            sb.append(str3);
        }
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public static void SetUUID(String str) {
        try {
            SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
            if (str == null) {
                sharedPreferencesEditor.remove("AAMUserId");
            } else {
                sharedPreferencesEditor.putString("AAMUserId", str);
            }
            sharedPreferencesEditor.commit();
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logErrorFormat("Audience Manager - Error updating uuid in shared preferences (%s)", e.getMessage());
        }
    }

    private static String GetUUID() {
        try {
            return StaticMethods.getSharedPreferences().getString("AAMUserId", (String) null);
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logErrorFormat("Audience Manager - Error getting uuid from shared preferences (%s).", e.getMessage());
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static void SetVisitorProfile(Map<String, Object> map) {
        VisitorProfile_pred = false;
        try {
            SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
            if (map == null || map.size() <= 0) {
                sharedPreferencesEditor.remove("AAMUserProfile");
                _visitorProfile = null;
            } else {
                sharedPreferencesEditor.putString("AAMUserProfile", new JSONObject(map).toString());
                _visitorProfile = new HashMap<>(map);
            }
            sharedPreferencesEditor.commit();
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logErrorFormat("Audience Manager - Error updating visitor profile (%s)", e.getMessage());
        }
    }

    private static void ProcessDestsArray(JSONObject jSONObject) {
        try {
            JSONArray jSONArray = jSONObject.getJSONArray("dests");
            for (int i = 0; i < jSONArray.length(); i++) {
                String string = jSONArray.getJSONObject(i).getString("c");
                if (string != null && string.length() > 0) {
                    RequestHandler.sendGenericRequest(string, (Map<String, String>) null, 5000, "Audience Manager");
                }
            }
        } catch (JSONException e) {
            StaticMethods.logDebugFormat("Audience Manager - No destination in response (%s)", e.getLocalizedMessage());
        }
    }

    private static HashMap<String, Object> ProcessStuffArray(JSONObject jSONObject) {
        HashMap<String, Object> hashMap = new HashMap<>();
        try {
            JSONArray jSONArray = jSONObject.getJSONArray("stuff");
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject2 = jSONArray.getJSONObject(i);
                if (jSONObject2 != null) {
                    hashMap.put(jSONObject2.getString("cn"), jSONObject2.getString("cv"));
                }
            }
        } catch (JSONException e) {
            StaticMethods.logDebugFormat("Audience Manager - No 'stuff' array in response (%s)", e.getLocalizedMessage());
        }
        return hashMap;
    }

    private static String GetUrlPrefix() {
        if (UrlPrefix_pred && MobileConfig.getInstance().mobileUsingAudienceManager()) {
            UrlPrefix_pred = false;
            Object[] objArr = new Object[2];
            objArr[0] = MobileConfig.getInstance().getSSL() ? "https" : "http";
            objArr[1] = MobileConfig.getInstance().getAamServer();
            _urlPrefix = String.format("%s://%s/event?", objArr);
        }
        return _urlPrefix;
    }
}
