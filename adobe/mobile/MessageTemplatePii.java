package com.adobe.mobile;

import org.json.JSONObject;

final class MessageTemplatePii extends MessageTemplateCallback {
    /* access modifiers changed from: protected */
    public String logPrefix() {
        return "PII";
    }

    MessageTemplatePii() {
    }

    /* access modifiers changed from: protected */
    public boolean initWithPayloadObject(JSONObject jSONObject) {
        if (!super.initWithPayloadObject(jSONObject)) {
            return false;
        }
        if (this.templateUrl.length() > 0 && this.templateUrl.toLowerCase().trim().startsWith("https")) {
            return true;
        }
        StaticMethods.logDebugFormat("Data Callback - Unable to create data callback %s, templateurl is empty or does not use https for request", this.messageId);
        return false;
    }

    /* access modifiers changed from: protected */
    public ThirdPartyQueue getQueue() {
        return PiiQueue.sharedInstance();
    }
}
