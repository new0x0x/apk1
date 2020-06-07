package com.adobe.mobile;

import android.content.SharedPreferences;
import com.adobe.mobile.StaticMethods;
import com.adobe.mobile.VisitorID;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

final class VisitorIDService {
    static String DEFAULT_SERVER = "dpm.demdex.net";
    private static VisitorIDService _instance;
    private static final Object _instanceMutex = new Object();
    /* access modifiers changed from: private */
    public String _aamIdString;
    /* access modifiers changed from: private */
    public String _analyticsIdString;
    /* access modifiers changed from: private */
    public String _blob;
    /* access modifiers changed from: private */
    public List<VisitorID> _customerIds;
    /* access modifiers changed from: private */
    public long _lastSync;
    /* access modifiers changed from: private */
    public String _locationHint;
    /* access modifiers changed from: private */
    public String _marketingCloudID;
    /* access modifiers changed from: private */
    public String _marketingCloudServer = MobileConfig.getInstance().getMarketingCloudCustomServer();
    /* access modifiers changed from: private */
    public long _ttl;
    private final ExecutorService _visitorIDExector = Executors.newSingleThreadExecutor();

    public static VisitorIDService sharedInstance() {
        VisitorIDService visitorIDService;
        synchronized (_instanceMutex) {
            if (_instance == null) {
                _instance = new VisitorIDService();
            }
            visitorIDService = _instance;
        }
        return visitorIDService;
    }

    protected VisitorIDService() {
        String str = this._marketingCloudServer;
        if (str == null || str.isEmpty()) {
            this._marketingCloudServer = DEFAULT_SERVER;
        }
        resetVariablesFromSharedPreferences();
        idSync((Map<String, String>) null);
    }

    /* access modifiers changed from: protected */
    public void resetVariablesFromSharedPreferences() {
        FutureTask futureTask = new FutureTask(new Callable<Void>() {
            public Void call() throws Exception {
                try {
                    VisitorIDService.this.updateCustomerIds(VisitorIDService.this._parseIdString(StaticMethods.getSharedPreferences().getString("ADBMOBILE_VISITORID_IDS", (String) null)));
                    String unused = VisitorIDService.this._marketingCloudID = StaticMethods.getSharedPreferences().getString("ADBMOBILE_PERSISTED_MID", (String) null);
                    String unused2 = VisitorIDService.this._locationHint = StaticMethods.getSharedPreferences().getString("ADBMOBILE_PERSISTED_MID_HINT", (String) null);
                    String unused3 = VisitorIDService.this._blob = StaticMethods.getSharedPreferences().getString("ADBMOBILE_PERSISTED_MID_BLOB", (String) null);
                    long unused4 = VisitorIDService.this._ttl = StaticMethods.getSharedPreferences().getLong("ADBMOBILE_VISITORID_TTL", 0);
                    long unused5 = VisitorIDService.this._lastSync = StaticMethods.getSharedPreferences().getLong("ADBMOBILE_VISITORID_SYNC", 0);
                } catch (StaticMethods.NullContextException e) {
                    String unused6 = VisitorIDService.this._marketingCloudID = null;
                    String unused7 = VisitorIDService.this._locationHint = null;
                    String unused8 = VisitorIDService.this._blob = null;
                    StaticMethods.logErrorFormat("Visitor - Unable to check for stored visitor ID due to context error (%s)", e.getMessage());
                }
                return null;
            }
        });
        this._visitorIDExector.execute(futureTask);
        try {
            futureTask.get();
        } catch (Exception e) {
            StaticMethods.logErrorFormat("ID Service - Unable to initialize visitor ID variables(%s)", e.getLocalizedMessage());
        }
    }

    /* access modifiers changed from: private */
    public void updateCustomerIds(List<VisitorID> list) {
        this._customerIds = list;
        this._analyticsIdString = _generateAnalyticsCustomerIdString(this._customerIds);
        this._aamIdString = _generateCustomerIdString(this._customerIds);
    }

    /* access modifiers changed from: protected */
    public void idSync(Map<String, String> map) {
        idSync(map, (Map<String, String>) null, VisitorID.VisitorIDAuthenticationState.VISITOR_ID_AUTHENTICATION_STATE_UNKNOWN, false);
    }

    /* access modifiers changed from: protected */
    public void idSync(Map<String, String> map, Map<String, String> map2, VisitorID.VisitorIDAuthenticationState visitorIDAuthenticationState, boolean z) {
        HashMap hashMap = null;
        final HashMap hashMap2 = map != null ? new HashMap(map) : null;
        if (map2 != null) {
            hashMap = new HashMap(map2);
        }
        final HashMap hashMap3 = hashMap;
        final boolean z2 = z;
        final VisitorID.VisitorIDAuthenticationState visitorIDAuthenticationState2 = visitorIDAuthenticationState;
        this._visitorIDExector.execute(new Runnable() {
            public void run() {
                if (MobileConfig.getInstance().getVisitorIdServiceEnabled()) {
                    if (MobileConfig.getInstance().getPrivacyStatus() == MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_OUT) {
                        StaticMethods.logDebugFormat("ID Service - Ignoring ID Sync due to privacy status being opted out", new Object[0]);
                        return;
                    }
                    String marketingCloudOrganizationId = MobileConfig.getInstance().getMarketingCloudOrganizationId();
                    boolean z = StaticMethods.getTimeSince1970() - VisitorIDService.this._lastSync > VisitorIDService.this._ttl || z2;
                    boolean z2 = hashMap2 != null;
                    boolean z3 = hashMap3 != null;
                    if (VisitorIDService.this._marketingCloudID == null || z2 || z3 || z) {
                        StringBuilder sb = new StringBuilder(MobileConfig.getInstance().getSSL() ? "https" : "http");
                        sb.append("://");
                        sb.append(VisitorIDService.this._marketingCloudServer);
                        sb.append("/id?d_ver=2&d_rtbd=json&d_orgid=");
                        sb.append(marketingCloudOrganizationId);
                        if (VisitorIDService.this._marketingCloudID != null) {
                            sb.append("&");
                            sb.append("d_mid");
                            sb.append("=");
                            sb.append(VisitorIDService.this._marketingCloudID);
                        }
                        if (VisitorIDService.this._blob != null) {
                            sb.append("&");
                            sb.append("d_blob");
                            sb.append("=");
                            sb.append(VisitorIDService.this._blob);
                        }
                        if (VisitorIDService.this._locationHint != null) {
                            sb.append("&");
                            sb.append("dcs_region");
                            sb.append("=");
                            sb.append(VisitorIDService.this._locationHint);
                        }
                        List access$800 = VisitorIDService.this._generateCustomerIds(hashMap2, visitorIDAuthenticationState2);
                        String access$900 = VisitorIDService.this._generateCustomerIdString(access$800);
                        if (access$900 != null) {
                            sb.append(access$900);
                        }
                        String access$1000 = VisitorIDService.this._generateInternalIdString(hashMap3);
                        if (access$1000 != null) {
                            sb.append(access$1000);
                        }
                        String sb2 = sb.toString();
                        StaticMethods.logDebugFormat("ID Service - Sending id sync call (%s)", sb2);
                        JSONObject parseResponse = VisitorIDService.this.parseResponse(RequestHandler.retrieveData(sb2, (Map<String, String>) null, 2000, "ID Service"));
                        if (parseResponse == null || !parseResponse.has("d_mid") || parseResponse.has("error_msg")) {
                            if (parseResponse != null && parseResponse.has("error_msg")) {
                                try {
                                    StaticMethods.logErrorFormat("ID Service - Service returned error (%s)", parseResponse.getString("error_msg"));
                                } catch (JSONException e) {
                                    StaticMethods.logErrorFormat("ID Service - Unable to read error condition(%s)", e.getLocalizedMessage());
                                }
                            }
                            if (VisitorIDService.this._marketingCloudID == null) {
                                VisitorIDService visitorIDService = VisitorIDService.this;
                                String unused = visitorIDService._marketingCloudID = visitorIDService._generateMID();
                                String unused2 = VisitorIDService.this._blob = null;
                                String unused3 = VisitorIDService.this._locationHint = null;
                                long unused4 = VisitorIDService.this._ttl = 600;
                                StaticMethods.logDebugFormat("ID Service - Did not return an ID, generating one locally (mid: %s, ttl: %d)", VisitorIDService.this._marketingCloudID, Long.valueOf(VisitorIDService.this._ttl));
                            }
                        } else {
                            try {
                                String unused5 = VisitorIDService.this._marketingCloudID = parseResponse.getString("d_mid");
                                if (parseResponse.has("d_blob")) {
                                    String unused6 = VisitorIDService.this._blob = parseResponse.getString("d_blob");
                                }
                                if (parseResponse.has("dcs_region")) {
                                    String unused7 = VisitorIDService.this._locationHint = parseResponse.getString("dcs_region");
                                }
                                if (parseResponse.has("id_sync_ttl")) {
                                    long unused8 = VisitorIDService.this._ttl = (long) parseResponse.getInt("id_sync_ttl");
                                }
                                String str = "";
                                if (parseResponse.has("d_optout") && parseResponse.getJSONArray("d_optout").length() > 0) {
                                    MobileConfig.getInstance().setPrivacyStatus(MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_OUT);
                                    str = ", global privacy status: opted out";
                                }
                                StaticMethods.logDebugFormat("ID Service - Got ID Response (mid: %s, blob: %s, hint: %s, ttl: %d%s)", VisitorIDService.this._marketingCloudID, VisitorIDService.this._blob, VisitorIDService.this._locationHint, Long.valueOf(VisitorIDService.this._ttl), str);
                            } catch (JSONException e2) {
                                StaticMethods.logDebugFormat("ID Service - Error parsing response (%s)", e2.getLocalizedMessage());
                            }
                        }
                        long unused9 = VisitorIDService.this._lastSync = StaticMethods.getTimeSince1970();
                        VisitorIDService visitorIDService2 = VisitorIDService.this;
                        visitorIDService2.updateCustomerIds(visitorIDService2._mergeCustomerIds(access$800));
                        VisitorIDService visitorIDService3 = VisitorIDService.this;
                        String access$1400 = visitorIDService3._generateStoredCustomerIdString(visitorIDService3._customerIds);
                        WearableFunctionBridge.syncVidServiceToWearable(VisitorIDService.this._marketingCloudID, VisitorIDService.this._locationHint, VisitorIDService.this._blob, VisitorIDService.this._ttl, VisitorIDService.this._lastSync, access$1400);
                        try {
                            SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                            sharedPreferencesEditor.putString("ADBMOBILE_VISITORID_IDS", access$1400);
                            sharedPreferencesEditor.putString("ADBMOBILE_PERSISTED_MID", VisitorIDService.this._marketingCloudID);
                            sharedPreferencesEditor.putString("ADBMOBILE_PERSISTED_MID_HINT", VisitorIDService.this._locationHint);
                            sharedPreferencesEditor.putString("ADBMOBILE_PERSISTED_MID_BLOB", VisitorIDService.this._blob);
                            sharedPreferencesEditor.putLong("ADBMOBILE_VISITORID_TTL", VisitorIDService.this._ttl);
                            sharedPreferencesEditor.putLong("ADBMOBILE_VISITORID_SYNC", VisitorIDService.this._lastSync);
                            sharedPreferencesEditor.commit();
                        } catch (StaticMethods.NullContextException e3) {
                            StaticMethods.logErrorFormat("ID Service - Unable to persist identifiers to shared preferences(%s)", e3.getLocalizedMessage());
                        }
                    }
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public final JSONObject parseResponse(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        try {
            return new JSONObject(new String(bArr, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            StaticMethods.logErrorFormat("ID Service - Unable to decode response(%s)", e.getLocalizedMessage());
            return null;
        } catch (JSONException e2) {
            StaticMethods.logDebugFormat("ID Service - Unable to parse response(%s)", e2.getLocalizedMessage());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public final String getMarketingCloudID() {
        FutureTask futureTask = new FutureTask(new Callable<String>() {
            public String call() throws Exception {
                return VisitorIDService.this._marketingCloudID;
            }
        });
        this._visitorIDExector.execute(futureTask);
        try {
            return (String) futureTask.get();
        } catch (Exception e) {
            StaticMethods.logErrorFormat("ID Service - Unable to retrieve marketing cloud id from queue(%s)", e.getLocalizedMessage());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public final String getAnalyticsIDRequestParameterString() {
        final StringBuilder sb = new StringBuilder();
        FutureTask futureTask = new FutureTask(new Callable<Void>() {
            public Void call() throws Exception {
                if (VisitorIDService.this._marketingCloudID == null) {
                    return null;
                }
                sb.append("?");
                sb.append("mid");
                sb.append("=");
                sb.append(VisitorIDService.this._marketingCloudID);
                sb.append("&");
                sb.append("mcorgid");
                sb.append("=");
                sb.append(MobileConfig.getInstance().getMarketingCloudOrganizationId());
                return null;
            }
        });
        this._visitorIDExector.execute(futureTask);
        try {
            futureTask.get();
        } catch (Exception e) {
            StaticMethods.logErrorFormat("ID Service - Unable to retrieve analytics id parameters from queue(%s)", e.getLocalizedMessage());
        }
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    public final String getAnalyticsIdString() {
        FutureTask futureTask = new FutureTask(new Callable<String>() {
            public String call() throws Exception {
                return VisitorIDService.this._analyticsIdString != null ? VisitorIDService.this._analyticsIdString : "";
            }
        });
        this._visitorIDExector.execute(futureTask);
        try {
            return (String) futureTask.get();
        } catch (Exception e) {
            StaticMethods.logErrorFormat("ID Service - Unable to retrieve analytics id string from queue(%s)", e.getLocalizedMessage());
            return "";
        }
    }

    /* access modifiers changed from: protected */
    public final Map<String, Object> getAnalyticsIdVisitorParameters() {
        final HashMap hashMap = new HashMap();
        FutureTask futureTask = new FutureTask(new Callable<Void>() {
            public Void call() throws Exception {
                if (VisitorIDService.this._marketingCloudID == null) {
                    return null;
                }
                hashMap.put("mid", VisitorIDService.this._marketingCloudID);
                if (VisitorIDService.this._blob != null) {
                    hashMap.put("aamb", VisitorIDService.this._blob);
                }
                if (VisitorIDService.this._locationHint != null) {
                    hashMap.put("aamlh", VisitorIDService.this._locationHint);
                }
                return null;
            }
        });
        this._visitorIDExector.execute(futureTask);
        try {
            futureTask.get();
        } catch (Exception e) {
            StaticMethods.logErrorFormat("ID Service - Unable to retrieve analytics parameters from queue(%s)", e.getLocalizedMessage());
        }
        return hashMap;
    }

    /* access modifiers changed from: protected */
    public final String getAAMParameterString() {
        final StringBuilder sb = new StringBuilder();
        FutureTask futureTask = new FutureTask(new Callable<Void>() {
            public Void call() throws Exception {
                if (VisitorIDService.this._marketingCloudID == null) {
                    return null;
                }
                sb.append("&");
                sb.append("d_mid");
                sb.append("=");
                sb.append(VisitorIDService.this._marketingCloudID);
                if (VisitorIDService.this._blob != null) {
                    sb.append("&");
                    sb.append("d_blob");
                    sb.append("=");
                    sb.append(VisitorIDService.this._blob);
                }
                if (VisitorIDService.this._locationHint != null) {
                    sb.append("&");
                    sb.append("dcs_region");
                    sb.append("=");
                    sb.append(VisitorIDService.this._locationHint);
                }
                if (VisitorIDService.this._aamIdString != null) {
                    sb.append(VisitorIDService.this._aamIdString);
                }
                return null;
            }
        });
        this._visitorIDExector.execute(futureTask);
        try {
            futureTask.get();
        } catch (Exception e) {
            StaticMethods.logErrorFormat("ID Service - Unable to retrieve audience manager parameters from queue(%s)", e.getLocalizedMessage());
        }
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    public final String appendVisitorInfoForURL(String str) {
        StringBuilder _generateVisitorIDURLPayload;
        if (str == null || str.length() == 0 || (_generateVisitorIDURLPayload = _generateVisitorIDURLPayload()) == null || _generateVisitorIDURLPayload.length() == 0) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        int indexOf = sb.indexOf("?");
        if (indexOf <= 0) {
            _generateVisitorIDURLPayload.insert(0, "?");
        } else if (indexOf != sb.length() - 1) {
            _generateVisitorIDURLPayload.insert(0, "&");
        }
        int indexOf2 = sb.indexOf("#");
        if (indexOf2 <= 0) {
            indexOf2 = sb.length();
        }
        return sb.insert(indexOf2, _generateVisitorIDURLPayload.toString()).toString();
    }

    /* access modifiers changed from: private */
    public String _generateMID() {
        UUID randomUUID = UUID.randomUUID();
        long mostSignificantBits = randomUUID.getMostSignificantBits();
        long leastSignificantBits = randomUUID.getLeastSignificantBits();
        Locale locale = Locale.US;
        Object[] objArr = new Object[2];
        if (mostSignificantBits < 0) {
            mostSignificantBits = -mostSignificantBits;
        }
        objArr[0] = Long.valueOf(mostSignificantBits);
        if (leastSignificantBits < 0) {
            leastSignificantBits = -leastSignificantBits;
        }
        objArr[1] = Long.valueOf(leastSignificantBits);
        return String.format(locale, "%019d%019d", objArr);
    }

    private String _timedCall(Callable<String> callable, long j, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        if (callable == null) {
            return null;
        }
        FutureTask futureTask = new FutureTask(callable);
        new Thread(futureTask).start();
        return (String) futureTask.get(j, timeUnit);
    }

    private String _getAnalyticsID(long j, TimeUnit timeUnit) {
        try {
            return _timedCall(new Callable<String>() {
                public String call() throws Exception {
                    return Analytics.getTrackingIdentifier();
                }
            }, j, timeUnit);
        } catch (InterruptedException e) {
            StaticMethods.logErrorFormat("ID Service - error retrieving AID (%s)", e.getLocalizedMessage());
            return null;
        } catch (ExecutionException e2) {
            StaticMethods.logErrorFormat("ID Service - error retrieving AID (%s)", e2.getLocalizedMessage());
            return null;
        } catch (TimeoutException e3) {
            StaticMethods.logErrorFormat("ID Service - Timeout exceeded when retrieving AID (%s)", e3.getLocalizedMessage());
            return null;
        }
    }

    private String _getAnalyticsVID(long j, TimeUnit timeUnit) {
        try {
            return _timedCall(new Callable<String>() {
                public String call() throws Exception {
                    return Config.getUserIdentifier();
                }
            }, j, timeUnit);
        } catch (InterruptedException e) {
            StaticMethods.logErrorFormat("ID Service - error retrieving VID (%s)", e.getLocalizedMessage());
            return null;
        } catch (ExecutionException e2) {
            StaticMethods.logErrorFormat("ID Service - error retrieving VID (%s)", e2.getLocalizedMessage());
            return null;
        } catch (TimeoutException e3) {
            StaticMethods.logErrorFormat("ID Service - Timeout exceeded when retrieving VID (%s)", e3.getLocalizedMessage());
            return null;
        }
    }

    private String _getMarketingCloudID(long j, TimeUnit timeUnit) {
        try {
            return _timedCall(new Callable<String>() {
                public String call() throws Exception {
                    return VisitorIDService.this.getMarketingCloudID();
                }
            }, j, timeUnit);
        } catch (InterruptedException e) {
            StaticMethods.logErrorFormat("ID Service - error retrieving MID (%s)", e.getLocalizedMessage());
            return null;
        } catch (ExecutionException e2) {
            StaticMethods.logErrorFormat("ID Service - error retrieving MID (%s)", e2.getLocalizedMessage());
            return null;
        } catch (TimeoutException e3) {
            StaticMethods.logErrorFormat("ID Service - Timeout exceeded when retrieving MID (%s)", e3.getLocalizedMessage());
            return null;
        }
    }

    private StringBuilder _generateVisitorIDURLPayload() {
        StringBuilder sb = new StringBuilder();
        String _getAnalyticsID = _getAnalyticsID(100, TimeUnit.MILLISECONDS);
        String _getAnalyticsVID = _getAnalyticsVID(100, TimeUnit.MILLISECONDS);
        String _getMarketingCloudID = _getMarketingCloudID(100, TimeUnit.MILLISECONDS);
        String _appendKVPToVisitorIdString = _appendKVPToVisitorIdString(_appendKVPToVisitorIdString(_appendKVPToVisitorIdString(_appendKVPToVisitorIdString((String) null, "TS", String.valueOf(StaticMethods.getTimeSince1970())), "MCMID", _getMarketingCloudID), "MCAID", _getAnalyticsID), "MCORGID", MobileConfig.getInstance().getMarketingCloudOrganizationId());
        sb.append("adobe_mc");
        sb.append("=");
        sb.append(StaticMethods.URLEncode(_appendKVPToVisitorIdString));
        if (_getAnalyticsVID != null && _getAnalyticsVID.length() > 0) {
            sb.append("&");
            sb.append("adobe_aa_vid");
            sb.append("=");
            sb.append(StaticMethods.URLEncode(_getAnalyticsVID));
        }
        if (sb.length() > 0) {
            return sb;
        }
        return null;
    }

    private String _appendKVPToVisitorIdString(String str, String str2, String str3) {
        if (str2 == null || str2.length() <= 0 || str3 == null || str3.length() <= 0) {
            return str;
        }
        String format = String.format("%s=%s", new Object[]{str2, StaticMethods.URLEncode(str3)});
        if (str == null || str.length() <= 0) {
            return format;
        }
        return String.format("%s|%s", new Object[]{str, format});
    }

    /* access modifiers changed from: private */
    public String _generateStoredCustomerIdString(List<VisitorID> list) {
        if (list == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (VisitorID next : list) {
            sb.append("&");
            sb.append("d_cid_ic");
            sb.append("=");
            sb.append(next.idType);
            sb.append("%01");
            if (next.id != null) {
                sb.append(next.id);
            }
            sb.append("%01");
            sb.append(next.authenticationState.getValue());
        }
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public String _generateCustomerIdString(List<VisitorID> list) {
        if (list == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (VisitorID next : list) {
            sb.append("&");
            sb.append("d_cid_ic");
            sb.append("=");
            sb.append(StaticMethods.URLEncode(next.idType));
            sb.append("%01");
            String URLEncode = StaticMethods.URLEncode(next.id);
            if (URLEncode != null) {
                sb.append(URLEncode);
            }
            sb.append("%01");
            sb.append(next.authenticationState.getValue());
        }
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public String _generateInternalIdString(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        HashMap hashMap = new HashMap(map);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry entry : hashMap.entrySet()) {
            sb.append("&d_cid=");
            sb.append(StaticMethods.URLEncode((String) entry.getKey()));
            sb.append("%01");
            sb.append(StaticMethods.URLEncode((String) entry.getValue()));
        }
        return sb.toString();
    }

    private String _generateAnalyticsCustomerIdString(List<VisitorID> list) {
        if (list == null) {
            return null;
        }
        HashMap hashMap = new HashMap();
        for (VisitorID next : list) {
            hashMap.put(next.serializeIdentifierKeyForAnalyticsID(), next.id);
            hashMap.put(next.serializeAuthenticationKeyForAnalyticsID(), Integer.valueOf(next.authenticationState.getValue()));
        }
        HashMap hashMap2 = new HashMap();
        hashMap2.put("cid", StaticMethods.translateContextData(hashMap));
        StringBuilder sb = new StringBuilder(2048);
        StaticMethods.serializeToQueryString(hashMap2, sb);
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public List<VisitorID> _generateCustomerIds(Map<String, String> map, VisitorID.VisitorIDAuthenticationState visitorIDAuthenticationState) {
        if (map == null) {
            return null;
        }
        HashMap hashMap = new HashMap(map);
        ArrayList arrayList = new ArrayList();
        for (Map.Entry entry : hashMap.entrySet()) {
            try {
                arrayList.add(new VisitorID("d_cid_ic", (String) entry.getKey(), (String) entry.getValue(), visitorIDAuthenticationState));
            } catch (IllegalStateException e) {
                StaticMethods.logWarningFormat("ID Service - Unable to create ID after encoding:(%s)", e.getLocalizedMessage());
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: private */
    public List<VisitorID> _mergeCustomerIds(List<VisitorID> list) {
        if (list == null) {
            return this._customerIds;
        }
        List<VisitorID> list2 = this._customerIds;
        ArrayList arrayList = list2 != null ? new ArrayList(list2) : new ArrayList();
        for (VisitorID next : list) {
            Iterator<VisitorID> it = arrayList.iterator();
            while (true) {
                if (it.hasNext()) {
                    VisitorID next2 = it.next();
                    if (next2.isVisitorID(next.idType)) {
                        next2.authenticationState = next.authenticationState;
                        next2.id = next.id;
                        break;
                    }
                } else {
                    try {
                        arrayList.add(next);
                        break;
                    } catch (IllegalStateException e) {
                        StaticMethods.logWarningFormat("ID Service - Unable to create ID after encoding:(%s)", e.getLocalizedMessage());
                    }
                }
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: private */
    public List<VisitorID> _parseIdString(String str) {
        if (str == null) {
            return null;
        }
        List<String> asList = Arrays.asList(str.split("&"));
        ArrayList arrayList = new ArrayList();
        for (String str2 : asList) {
            if (str2.length() > 0) {
                int indexOf = str2.indexOf("=");
                if (indexOf == -1) {
                    StaticMethods.logWarningFormat("ID Service - Unable to load Customer ID from Shared Preferences: %s", str2);
                } else {
                    try {
                        String substring = str2.substring(0, indexOf);
                        String substring2 = str2.substring(indexOf + 1, str2.length());
                        List asList2 = Arrays.asList(substring2.split("%01"));
                        if (asList2.size() != 3) {
                            StaticMethods.logWarningFormat("ID Service - Unable to load Customer ID from Shared Preferences, value was malformed: %s", substring2);
                        } else {
                            try {
                                arrayList.add(new VisitorID(substring, (String) asList2.get(0), (String) asList2.get(1), VisitorID.VisitorIDAuthenticationState.values()[Integer.parseInt((String) asList2.get(2))]));
                            } catch (NumberFormatException e) {
                                StaticMethods.logWarningFormat("ID Service - Unable to parse visitor ID: (%s) exception:(%s)", str2, e.getLocalizedMessage());
                            } catch (IllegalStateException e2) {
                                StaticMethods.logWarningFormat("ID Service - Unable to create ID after encoding:(%s)", e2.getLocalizedMessage());
                            }
                        }
                    } catch (IndexOutOfBoundsException e3) {
                        StaticMethods.logWarningFormat("ID Service - Unable to load Customer ID from Shared Preferences, name or value was malformed: %s (%s)", str2, e3.getLocalizedMessage());
                    }
                }
            }
        }
        return arrayList;
    }
}
