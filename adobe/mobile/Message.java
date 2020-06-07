package com.adobe.mobile;

import android.content.SharedPreferences;
import com.adobe.mobile.Messages;
import com.adobe.mobile.StaticMethods;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

abstract class Message {
    private static final Long JSON_DEFAULT_START_DATE = 0L;
    private static HashMap<String, Integer> _blacklist;
    private static final Object _blacklistMutex = new Object();
    private static final Map<String, Class> _messageTypeDictionary = new HashMap<String, Class>() {
        {
            put("local", MessageLocalNotification.class);
            put("alert", MessageAlert.class);
            put("fullscreen", MessageFullScreen.class);
            put("callback", MessageTemplateCallback.class);
            put("pii", MessageTemplatePii.class);
            put("openUrl", MessageOpenURL.class);
        }
    };
    private static final Map<String, Messages.MessageShowRule> _showRuleEnumDictionary = new HashMap<String, Messages.MessageShowRule>() {
        {
            put("unknown", Messages.MessageShowRule.MESSAGE_SHOW_RULE_UNKNOWN);
            put("always", Messages.MessageShowRule.MESSAGE_SHOW_RULE_ALWAYS);
            put("once", Messages.MessageShowRule.MESSAGE_SHOW_RULE_ONCE);
            put("untilClick", Messages.MessageShowRule.MESSAGE_SHOW_RULE_UNTIL_CLICK);
        }
    };
    protected ArrayList<ArrayList<String>> assets;
    protected ArrayList<MessageMatcher> audiences;
    protected Date endDate;
    protected boolean isVisible;
    protected String messageId;
    protected int orientationWhenShown;
    protected boolean showOffline;
    protected Messages.MessageShowRule showRule;
    protected Date startDate;
    protected ArrayList<MessageMatcher> triggers;

    Message() {
    }

    protected static Message messageWithJsonObject(JSONObject jSONObject) {
        try {
            Message message = (Message) _messageTypeDictionary.get(jSONObject.getString("template")).newInstance();
            if (message.initWithPayloadObject(jSONObject)) {
                return message;
            }
            return null;
        } catch (JSONException unused) {
            StaticMethods.logWarningFormat("Messages - template is required for in-app message", new Object[0]);
            return null;
        } catch (NullPointerException unused2) {
            StaticMethods.logWarningFormat("Messages - invalid template specified for message (%s)", "");
            return null;
        } catch (IllegalAccessException e) {
            StaticMethods.logWarningFormat("Messages - unable to create instance of message (%s)", e.getMessage());
            return null;
        } catch (InstantiationException e2) {
            StaticMethods.logWarningFormat("Messages - unable to create instance of message (%s)", e2.getMessage());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean initWithPayloadObject(JSONObject jSONObject) {
        if (jSONObject == null || jSONObject.length() == 0) {
            return false;
        }
        try {
            this.messageId = jSONObject.getString("messageId");
            if (this.messageId.length() <= 0) {
                StaticMethods.logWarningFormat("Messages - Unable to create message, messageId is empty", new Object[0]);
                return false;
            }
            try {
                String string = jSONObject.getString("showRule");
                this.showRule = messageShowRuleFromString(string);
                if (this.showRule == null || this.showRule == Messages.MessageShowRule.MESSAGE_SHOW_RULE_UNKNOWN) {
                    StaticMethods.logWarningFormat("Messages - Unable to create message \"%s\", showRule not valid (%s)", this.messageId, string);
                    return false;
                }
                try {
                    this.startDate = new Date(jSONObject.getLong("startDate") * 1000);
                } catch (JSONException unused) {
                    StaticMethods.logDebugFormat("Messages - Tried to read startDate from message \"%s\" but none found. Using default value", this.messageId);
                    this.startDate = new Date(JSON_DEFAULT_START_DATE.longValue() * 1000);
                }
                try {
                    this.endDate = new Date(jSONObject.getLong("endDate") * 1000);
                } catch (JSONException unused2) {
                    StaticMethods.logDebugFormat("Messages - Tried to read endDate from message \"%s\" but none found. Using default value", this.messageId);
                }
                try {
                    this.showOffline = jSONObject.getBoolean("showOffline");
                } catch (JSONException unused3) {
                    StaticMethods.logDebugFormat("Messages - Tried to read showOffline from message \"%s\" but none found. Using default value", this.messageId);
                    this.showOffline = false;
                }
                this.audiences = new ArrayList<>();
                try {
                    JSONArray jSONArray = jSONObject.getJSONArray("audiences");
                    int length = jSONArray.length();
                    for (int i = 0; i < length; i++) {
                        this.audiences.add(MessageMatcher.messageMatcherWithJsonObject(jSONArray.getJSONObject(i)));
                    }
                } catch (JSONException e) {
                    StaticMethods.logDebugFormat("Messages - failed to read audience for message \"%s\", error: %s", this.messageId, e.getMessage());
                }
                this.triggers = new ArrayList<>();
                try {
                    JSONArray jSONArray2 = jSONObject.getJSONArray("triggers");
                    int length2 = jSONArray2.length();
                    for (int i2 = 0; i2 < length2; i2++) {
                        this.triggers.add(MessageMatcher.messageMatcherWithJsonObject(jSONArray2.getJSONObject(i2)));
                    }
                } catch (JSONException e2) {
                    StaticMethods.logDebugFormat("Messages - failed to read trigger for message \"%s\", error: %s", this.messageId, e2.getMessage());
                }
                if (this.triggers.size() <= 0) {
                    StaticMethods.logWarningFormat("Messages - Unable to load message \"%s\" - at least one valid trigger is required for a message", this.messageId);
                    return false;
                }
                this.isVisible = false;
                return true;
            } catch (JSONException unused4) {
                StaticMethods.logWarningFormat("Messages - Unable to create message \"%s\", showRule is required", this.messageId);
                return false;
            }
        } catch (JSONException unused5) {
            StaticMethods.logWarningFormat("Messages - Unable to create message, messageId is required", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void blacklist() {
        synchronized (_blacklistMutex) {
            if (_blacklist == null) {
                _blacklist = loadBlacklist();
            }
            _blacklist.put(this.messageId, Integer.valueOf(this.showRule.getValue()));
            StaticMethods.logDebugFormat("Messages - adding message \"%s\" to blacklist", this.messageId);
            try {
                SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                sharedPreferencesEditor.putString("messagesBlackList", stringFromMap(_blacklist));
                sharedPreferencesEditor.commit();
            } catch (StaticMethods.NullContextException e) {
                StaticMethods.logErrorFormat("Messages - Error persisting blacklist map (%s).", e.getMessage());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeFromBlacklist() {
        if (isBlacklisted()) {
            synchronized (_blacklistMutex) {
                _blacklist.remove(this.messageId);
                StaticMethods.logDebugFormat("Messages - removing message \"%s\" from blacklist", this.messageId);
                try {
                    SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                    sharedPreferencesEditor.putString("messagesBlackList", stringFromMap(_blacklist));
                    sharedPreferencesEditor.commit();
                } catch (StaticMethods.NullContextException e) {
                    StaticMethods.logErrorFormat("Messages - Error persisting blacklist map (%s).", e.getMessage());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isBlacklisted() {
        boolean z;
        synchronized (_blacklistMutex) {
            if (_blacklist == null) {
                _blacklist = loadBlacklist();
            }
            z = _blacklist.get(this.messageId) != null;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public HashMap<String, Integer> loadBlacklist() {
        try {
            String string = StaticMethods.getSharedPreferences().getString("messagesBlackList", (String) null);
            if (string == null) {
                return new HashMap<>();
            }
            return mapFromString(string);
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logDebugFormat("Messaging - Unable to get shared preferences while loading blacklist. (%s)", e.getMessage());
            return new HashMap<>();
        }
    }

    /* access modifiers changed from: protected */
    public boolean shouldShowForVariables(Map<String, Object> map, Map<String, Object> map2, Map<String, Object> map3) {
        if (this.isVisible && this.orientationWhenShown != StaticMethods.getCurrentOrientation() && (this instanceof MessageAlert)) {
            return true;
        }
        if (Messages.getCurrentMessage() != null && !(this instanceof MessageLocalNotification) && !(this instanceof MessageTemplateCallback)) {
            return false;
        }
        if (((map == null || map.size() <= 0) && (map2 == null || map2.size() <= 0)) || isBlacklisted()) {
            return false;
        }
        if (!MobileConfig.getInstance().networkConnectivity() && !this.showOffline) {
            return false;
        }
        Date date = new Date(StaticMethods.getTimeSince1970() * 1000);
        if (date.before(this.startDate)) {
            return false;
        }
        Date date2 = this.endDate;
        if (date2 != null && date.after(date2)) {
            return false;
        }
        Iterator<MessageMatcher> it = this.audiences.iterator();
        while (it.hasNext()) {
            if (!it.next().matchesInMaps(map3)) {
                return false;
            }
        }
        Map<String, Object> cleanContextDataDictionary = StaticMethods.cleanContextDataDictionary(map2);
        Iterator<MessageMatcher> it2 = this.triggers.iterator();
        while (it2.hasNext()) {
            if (!it2.next().matchesInMaps(map, cleanContextDataDictionary)) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void messageTriggered() {
        HashMap hashMap = new HashMap();
        hashMap.put("a.message.id", this.messageId);
        hashMap.put("a.message.triggered", 1);
        AnalyticsTrackInternal.trackInternal("In-App Message", hashMap, StaticMethods.getTimeSince1970());
    }

    /* access modifiers changed from: protected */
    public void show() {
        this.orientationWhenShown = StaticMethods.getCurrentOrientation();
        if (this.showRule == Messages.MessageShowRule.MESSAGE_SHOW_RULE_ONCE) {
            blacklist();
        }
        if ((this instanceof MessageAlert) || (this instanceof MessageFullScreen)) {
            Messages.setCurrentMessage(this);
        }
    }

    /* access modifiers changed from: protected */
    public void viewed() {
        HashMap hashMap = new HashMap();
        hashMap.put("a.message.id", this.messageId);
        hashMap.put("a.message.viewed", 1);
        AnalyticsTrackInternal.trackInternal("In-App Message", hashMap, StaticMethods.getTimeSince1970());
        Messages.setCurrentMessage((Message) null);
    }

    /* access modifiers changed from: protected */
    public void clickedThrough() {
        HashMap hashMap = new HashMap();
        hashMap.put("a.message.id", this.messageId);
        hashMap.put("a.message.clicked", 1);
        AnalyticsTrackInternal.trackInternal("In-App Message", hashMap, StaticMethods.getTimeSince1970());
        if (this.showRule == Messages.MessageShowRule.MESSAGE_SHOW_RULE_UNTIL_CLICK) {
            blacklist();
        }
        Messages.setCurrentMessage((Message) null);
    }

    /* access modifiers changed from: protected */
    public String description() {
        return "Message ID: " + this.messageId + "; Show Rule: " + this.showRule.toString() + "; Blacklisted: " + isBlacklisted();
    }

    private static Messages.MessageShowRule messageShowRuleFromString(String str) {
        return _showRuleEnumDictionary.get(str);
    }

    private HashMap<String, Integer> mapFromString(String str) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        try {
            JSONObject jSONObject = new JSONObject(str);
            Iterator<String> keys = jSONObject.keys();
            while (keys.hasNext()) {
                String next = keys.next();
                hashMap.put(next, Integer.valueOf(jSONObject.getInt(next)));
            }
        } catch (JSONException e) {
            StaticMethods.logErrorFormat("Messages- Unable to deserialize blacklist(%s)", e.getMessage());
        }
        return hashMap;
    }

    private String stringFromMap(Map<String, Integer> map) {
        return new JSONObject(map).toString();
    }
}
