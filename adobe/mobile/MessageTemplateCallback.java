package com.adobe.mobile;

import android.text.TextUtils;
import android.util.Base64;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

class MessageTemplateCallback extends Message {
    private static final boolean[] tokenDataMask = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, true, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
    private HashMap<String, Object> _combinedVariablesCopy;
    protected String contentType;
    private final SecureRandom randomGen = new SecureRandom();
    protected String templateBody;
    protected String templateUrl;
    protected int timeout;

    /* access modifiers changed from: protected */
    public String logPrefix() {
        return "Postbacks";
    }

    MessageTemplateCallback() {
    }

    /* access modifiers changed from: protected */
    public boolean initWithPayloadObject(JSONObject jSONObject) {
        byte[] decode;
        if (jSONObject == null || jSONObject.length() <= 0 || !super.initWithPayloadObject(jSONObject)) {
            return false;
        }
        String logPrefix = logPrefix();
        try {
            JSONObject jSONObject2 = jSONObject.getJSONObject("payload");
            if (jSONObject2.length() <= 0) {
                StaticMethods.logDebugFormat("%s - Unable to create data callback %s, \"payload\" is empty", logPrefix, this.messageId);
                return false;
            }
            try {
                this.templateUrl = jSONObject2.getString("templateurl");
                if (this.templateUrl.length() <= 0) {
                    StaticMethods.logDebugFormat("%s - Unable to create data callback %s, \"templateurl\" is empty", logPrefix, this.messageId);
                    return false;
                }
                try {
                    this.timeout = jSONObject2.getInt("timeout");
                } catch (JSONException unused) {
                    StaticMethods.logDebugFormat("%s - Tried to read \"timeout\" for data callback, but found none.  Using default value of two (2) seconds", logPrefix);
                    this.timeout = 2;
                }
                try {
                    String string = jSONObject2.getString("templatebody");
                    if (!(string == null || string.length() <= 0 || (decode = Base64.decode(string, 0)) == null)) {
                        String str = new String(decode, "UTF-8");
                        if (str.length() > 0) {
                            this.templateBody = str;
                        }
                    }
                } catch (JSONException unused2) {
                    StaticMethods.logDebugFormat("%s - Tried to read \"templatebody\" for data callback, but found none.  This is not a required field", logPrefix);
                } catch (UnsupportedEncodingException e) {
                    StaticMethods.logDebugFormat("%s - Failed to decode \"templatebody\" for data callback (%s).  This is not a required field", logPrefix, e.getLocalizedMessage());
                } catch (IllegalArgumentException e2) {
                    StaticMethods.logDebugFormat("%s - Failed to decode \"templatebody\" for data callback (%s).  This is not a required field", logPrefix, e2.getLocalizedMessage());
                }
                String str2 = this.templateBody;
                if (str2 != null && str2.length() > 0) {
                    try {
                        this.contentType = jSONObject2.getString("contenttype");
                    } catch (JSONException unused3) {
                        StaticMethods.logDebugFormat("%s - Tried to read \"contenttype\" for data callback, but found none.  This is not a required field", logPrefix);
                    }
                }
                return true;
            } catch (JSONException unused4) {
                StaticMethods.logDebugFormat("%s - Unable to create data callback %s, \"templateurl\" is required", logPrefix, this.messageId);
                return false;
            }
        } catch (JSONException unused5) {
            StaticMethods.logDebugFormat("%s - Unable to create create data callback %s, \"payload\" is required", logPrefix, this.messageId);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean shouldShowForVariables(Map<String, Object> map, Map<String, Object> map2, Map<String, Object> map3) {
        HashMap hashMap = map2 != null ? new HashMap(map2) : new HashMap();
        if (map != null) {
            hashMap.putAll(map);
        }
        hashMap.putAll(getExpansionTokensForVariables(hashMap));
        if (map3 != null) {
            hashMap.putAll(map3);
        }
        this._combinedVariablesCopy = new HashMap<>(hashMap);
        return super.shouldShowForVariables(map, map2, map3);
    }

    /* access modifiers changed from: protected */
    public void show() {
        String expandedUrl = getExpandedUrl();
        String expandedBody = getExpandedBody();
        StaticMethods.logDebugFormat("%s - Request Queued (url:%s body:%s contentType:%s)", logPrefix(), expandedUrl, expandedBody, this.contentType);
        getQueue().queue(expandedUrl, expandedBody, this.contentType, StaticMethods.getTimeSince1970(), (long) this.timeout);
    }

    /* access modifiers changed from: protected */
    public String getExpandedUrl() {
        String str = this.templateUrl;
        if (str == null || str.length() <= 0) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add("{%all_url%}");
        HashMap<String, String> buildExpansionsForTokens = buildExpansionsForTokens(findTokensForExpansion(this.templateUrl), true);
        buildExpansionsForTokens.putAll(buildExpansionsForTokens(arrayList, false));
        return StaticMethods.expandTokens(this.templateUrl, buildExpansionsForTokens);
    }

    private String getExpandedBody() {
        String str = this.templateBody;
        if (str == null || str.length() <= 0) {
            return null;
        }
        String str2 = this.contentType;
        HashMap<String, String> buildExpansionsForTokens = buildExpansionsForTokens(findTokensForExpansion(this.templateBody), !(str2 == null ? false : str2.toLowerCase().contains("application/json")));
        ArrayList arrayList = new ArrayList();
        arrayList.add("{%all_url%}");
        arrayList.add("{%all_json%}");
        buildExpansionsForTokens.putAll(buildExpansionsForTokens(arrayList, false));
        return StaticMethods.expandTokens(this.templateBody, buildExpansionsForTokens);
    }

    /* access modifiers changed from: protected */
    public ThirdPartyQueue getQueue() {
        return ThirdPartyQueue.sharedInstance();
    }

    private HashMap<String, Object> getExpansionTokensForVariables(HashMap<String, Object> hashMap) {
        String str;
        HashMap<String, Object> hashMap2 = new HashMap<>(5);
        hashMap2.put("%sdkver%", "4.14.0-AN");
        hashMap2.put("%cachebust%", String.valueOf(this.randomGen.nextInt(100000000)));
        hashMap2.put("%adid%", StaticMethods.getAdvertisingIdentifier());
        hashMap2.put("%timestampu%", String.valueOf(StaticMethods.getTimeSince1970()));
        hashMap2.put("%timestampz%", StaticMethods.getIso8601Date());
        hashMap2.put("%pushid%", StaticMethods.getPushIdentifier());
        hashMap2.put("%mcid%", VisitorIDService.sharedInstance().getMarketingCloudID() != null ? VisitorIDService.sharedInstance().getMarketingCloudID() : "");
        ArrayList arrayList = new ArrayList();
        HashMap hashMap3 = new HashMap();
        for (String next : hashMap.keySet()) {
            if (next != null) {
                Object obj = hashMap.get(next);
                if (obj == null) {
                    str = "";
                } else {
                    str = obj.toString();
                }
                arrayList.add(StaticMethods.URLEncode(next) + "=" + StaticMethods.URLEncode(str));
                hashMap3.put(next, str);
            }
        }
        hashMap2.put("%all_url%", TextUtils.join("&", arrayList));
        try {
            String jSONObject = new JSONObject(hashMap3).toString();
            if (jSONObject.length() > 0) {
                hashMap2.put("%all_json%", jSONObject);
            }
        } catch (NullPointerException e) {
            StaticMethods.logDebugFormat("Data Callback - unable to create json string for vars:  (%s)", e.getLocalizedMessage());
        }
        return hashMap2;
    }

    private HashMap<String, String> buildExpansionsForTokens(ArrayList<String> arrayList, boolean z) {
        String str;
        HashMap<String, String> hashMap = new HashMap<>(arrayList.size());
        Iterator<String> it = arrayList.iterator();
        while (it.hasNext()) {
            String next = it.next();
            Object obj = this._combinedVariablesCopy.get(next.substring(1, next.length() - 1).toLowerCase());
            if (obj == null) {
                str = "";
            } else {
                str = obj.toString();
            }
            if (z) {
                str = StaticMethods.URLEncode(str);
            }
            hashMap.put(next, str);
        }
        return hashMap;
    }

    private ArrayList<String> findTokensForExpansion(String str) {
        if (str == null || str.length() <= 0) {
            return null;
        }
        ArrayList<String> arrayList = new ArrayList<>(32);
        int length = str.length();
        int i = 0;
        while (i < length) {
            if (str.charAt(i) == '{') {
                int i2 = i + 1;
                while (i2 < length && str.charAt(i2) != '}') {
                    i2++;
                }
                if (i2 == length) {
                    break;
                }
                String substring = str.substring(i, i2 + 1);
                if (tokenIsValid(substring.substring(1, substring.length() - 1))) {
                    arrayList.add(substring);
                    i = i2;
                }
            }
            i++;
        }
        return arrayList;
    }

    private boolean tokenIsValid(String str) {
        try {
            for (byte b : str.getBytes("UTF-8")) {
                if (!tokenDataMask[b & 255]) {
                    return false;
                }
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            StaticMethods.logErrorFormat("Data Callback - Unable to validate token (%s)", e.getLocalizedMessage());
            return false;
        }
    }
}
