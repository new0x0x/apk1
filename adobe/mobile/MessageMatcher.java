package com.adobe.mobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

abstract class MessageMatcher {
    private static final Map<String, Class> _messageMatcherDictionary = new HashMap<String, Class>() {
        {
            put("eq", MessageMatcherEquals.class);
            put("ne", MessageMatcherNotEquals.class);
            put("gt", MessageMatcherGreaterThan.class);
            put("ge", MessageMatcherGreaterThanOrEqual.class);
            put("lt", MessageMatcherLessThan.class);
            put("le", MessageMatcherLessThanOrEqual.class);
            put("co", MessageMatcherContains.class);
            put("nc", MessageMatcherNotContains.class);
            put("sw", MessageMatcherStartsWith.class);
            put("ew", MessageMatcherEndsWith.class);
            put("ex", MessageMatcherExists.class);
            put("nx", MessageMatcherNotExists.class);
        }
    };
    protected String key;
    protected ArrayList<Object> values;

    /* access modifiers changed from: protected */
    public boolean matches(Object obj) {
        return false;
    }

    MessageMatcher() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x005d  */
    protected static MessageMatcher messageMatcherWithJsonObject(JSONObject jSONObject) {
        String str;
        MessageMatcher messageMatcher;
        try {
            str = jSONObject.getString("matches");
            if (str != null && str.length() <= 0) {
                StaticMethods.logWarningFormat("Messages - message matcher type is empty", new Object[0]);
            }
        } catch (JSONException unused) {
            str = "UNKNOWN";
            StaticMethods.logWarningFormat("Messages - message matcher type is required", new Object[0]);
        }
        Class<MessageMatcherUnknown> cls = _messageMatcherDictionary.get(str);
        if (cls == null) {
            cls = MessageMatcherUnknown.class;
            StaticMethods.logWarningFormat("Messages - message matcher type \"%s\" is invalid", str);
        }
        try {
            messageMatcher = cls.newInstance();
        } catch (InstantiationException e) {
            StaticMethods.logErrorFormat("Messages - Error creating matcher (%s)", e.getMessage());
        } catch (IllegalAccessException e2) {
            StaticMethods.logErrorFormat("Messages - Error creating matcher (%s)", e2.getMessage());
        }
        if (messageMatcher != null) {
            try {
                messageMatcher.key = jSONObject.getString("key").toLowerCase();
                if (messageMatcher.key != null && messageMatcher.key.length() <= 0) {
                    StaticMethods.logWarningFormat("Messages - error creating matcher, key is empty", new Object[0]);
                }
            } catch (JSONException unused2) {
                StaticMethods.logWarningFormat("Messages - error creating matcher, key is required", new Object[0]);
            } catch (NullPointerException unused3) {
                StaticMethods.logWarningFormat("Messages - error creating matcher, key is required", new Object[0]);
            }
            try {
                messageMatcher.values = new ArrayList<>();
                if (messageMatcher instanceof MessageMatcherExists) {
                    return messageMatcher;
                }
                JSONArray jSONArray = jSONObject.getJSONArray("values");
                int length = jSONArray.length();
                for (int i = 0; i < length; i++) {
                    messageMatcher.values.add(jSONArray.get(i));
                }
                if (messageMatcher.values.size() == 0) {
                    StaticMethods.logWarningFormat("Messages - error creating matcher, values is empty", new Object[0]);
                }
            } catch (JSONException unused4) {
                StaticMethods.logWarningFormat("Messages - error creating matcher, values is required", new Object[0]);
            }
        }
        return messageMatcher;
        messageMatcher = null;
        if (messageMatcher != null) {
        }
        return messageMatcher;
    }

    /* access modifiers changed from: protected */
    public boolean matchesInMaps(Map<String, Object>... mapArr) {
        if (mapArr == null || mapArr.length <= 0) {
            return false;
        }
        Object obj = null;
        int length = mapArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            Map<String, Object> map = mapArr[i];
            if (map != null && map.containsKey(this.key)) {
                obj = map.get(this.key);
                break;
            }
            i++;
        }
        if (obj == null || !matches(obj)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public Double tryParseDouble(Object obj) {
        try {
            return Double.valueOf(obj.toString());
        } catch (Exception unused) {
            return null;
        }
    }
}
