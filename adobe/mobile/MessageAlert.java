package com.adobe.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.adobe.mobile.StaticMethods;
import com.mycheck.mychecksdk.fragments.WebViewFragment;
import com.wyndham.rewards.ui.cityguide.CityGuidePlaceDetailActivity;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

final class MessageAlert extends Message {
    protected AlertDialog alertDialog;
    protected String cancelButtonText;
    protected String confirmButtonText;
    protected String content;
    protected String title;
    protected String url;

    MessageAlert() {
    }

    /* access modifiers changed from: protected */
    public boolean initWithPayloadObject(JSONObject jSONObject) {
        if (jSONObject == null || jSONObject.length() <= 0 || !super.initWithPayloadObject(jSONObject)) {
            return false;
        }
        try {
            JSONObject jSONObject2 = jSONObject.getJSONObject("payload");
            if (jSONObject2.length() <= 0) {
                StaticMethods.logWarningFormat("Messages - Unable to create alert message \"%s\", payload is empty", this.messageId);
                return false;
            }
            try {
                this.title = jSONObject2.getString(CityGuidePlaceDetailActivity.TITLE);
                if (this.title.length() <= 0) {
                    StaticMethods.logWarningFormat("Messages - Unable to create alert message \"%s\", title is empty", this.messageId);
                    return false;
                }
                try {
                    this.content = jSONObject2.getString("content");
                    if (this.content.length() <= 0) {
                        StaticMethods.logWarningFormat("Messages - Unable to create alert message \"%s\", content is empty", this.messageId);
                        return false;
                    }
                    try {
                        this.cancelButtonText = jSONObject2.getString("cancel");
                        if (this.cancelButtonText.length() <= 0) {
                            StaticMethods.logWarningFormat("Messages - Unable to create alert message \"%s\", cancel is empty", this.messageId);
                            return false;
                        }
                        try {
                            this.confirmButtonText = jSONObject2.getString("confirm");
                        } catch (JSONException unused) {
                            StaticMethods.logDebugFormat("Messages - Tried to read \"confirm\" for alert message but found none. This is not a required field", new Object[0]);
                        }
                        try {
                            this.url = jSONObject2.getString(WebViewFragment.ARG_URL);
                        } catch (JSONException unused2) {
                            StaticMethods.logDebugFormat("Messages - Tried to read url for alert message but found none. This is not a required field", new Object[0]);
                        }
                        return true;
                    } catch (JSONException unused3) {
                        StaticMethods.logWarningFormat("Messages - Unable to create alert message \"%s\", cancel is required", this.messageId);
                        return false;
                    }
                } catch (JSONException unused4) {
                    StaticMethods.logWarningFormat("Messages - Unable to create alert message \"%s\", content is required", this.messageId);
                    return false;
                }
            } catch (JSONException unused5) {
                StaticMethods.logWarningFormat("Messages - Unable to create alert message \"%s\", title is required", this.messageId);
                return false;
            }
        } catch (JSONException unused6) {
            StaticMethods.logWarningFormat("Messages - Unable to create alert message \"%s\", payload is required", this.messageId);
            return false;
        }
    }

    private static final class MessageShower implements Runnable {
        private final MessageAlert message;

        public MessageShower(MessageAlert messageAlert) {
            this.message = messageAlert;
        }

        private static final class PositiveClickHandler implements DialogInterface.OnClickListener {
            private final MessageAlert message;

            public PositiveClickHandler(MessageAlert messageAlert) {
                this.message = messageAlert;
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                this.message.clickedThrough();
                MessageAlert messageAlert = this.message;
                messageAlert.isVisible = false;
                if (messageAlert.url != null && this.message.url.length() > 0) {
                    HashMap hashMap = new HashMap();
                    hashMap.put("{userId}", StaticMethods.getVisitorID() == null ? "" : StaticMethods.getVisitorID());
                    hashMap.put("{trackingId}", StaticMethods.getAID() == null ? "" : StaticMethods.getAID());
                    hashMap.put("{messageId}", this.message.messageId);
                    hashMap.put("{lifetimeValue}", AnalyticsTrackLifetimeValueIncrease.getLifetimeValue().toString());
                    MessageAlert messageAlert2 = this.message;
                    messageAlert2.url = StaticMethods.expandTokens(messageAlert2.url, hashMap);
                    try {
                        Activity currentActivity = StaticMethods.getCurrentActivity();
                        try {
                            Intent intent = new Intent("android.intent.action.VIEW");
                            intent.setData(Uri.parse(this.message.url));
                            currentActivity.startActivity(intent);
                        } catch (Exception e) {
                            StaticMethods.logDebugFormat("Messages - Could not load click-through intent for message (%s)", e.toString());
                        }
                    } catch (StaticMethods.NullActivityException e2) {
                        StaticMethods.logErrorFormat(e2.getMessage(), new Object[0]);
                    }
                }
            }
        }

        private static final class NegativeClickHandler implements DialogInterface.OnClickListener {
            private final MessageAlert message;

            public NegativeClickHandler(MessageAlert messageAlert) {
                this.message = messageAlert;
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                this.message.viewed();
                this.message.isVisible = false;
            }
        }

        private static final class CancelClickHandler implements DialogInterface.OnCancelListener {
            private final MessageAlert message;

            public CancelClickHandler(MessageAlert messageAlert) {
                this.message = messageAlert;
            }

            public void onCancel(DialogInterface dialogInterface) {
                this.message.viewed();
                this.message.isVisible = false;
            }
        }

        public void run() {
            try {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(StaticMethods.getCurrentActivity());
                    builder.setTitle(this.message.title);
                    builder.setMessage(this.message.content);
                    if (this.message.confirmButtonText != null && !this.message.confirmButtonText.isEmpty()) {
                        builder.setPositiveButton(this.message.confirmButtonText, new PositiveClickHandler(this.message));
                    }
                    builder.setNegativeButton(this.message.cancelButtonText, new NegativeClickHandler(this.message));
                    builder.setOnCancelListener(new CancelClickHandler(this.message));
                    this.message.alertDialog = builder.create();
                    this.message.alertDialog.setCanceledOnTouchOutside(false);
                    this.message.alertDialog.show();
                    this.message.isVisible = true;
                } catch (Exception e) {
                    StaticMethods.logDebugFormat("Messages - Could not show alert message (%s)", e.toString());
                }
            } catch (StaticMethods.NullActivityException e2) {
                StaticMethods.logErrorFormat(e2.getMessage(), new Object[0]);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void show() {
        String str;
        String str2 = this.cancelButtonText;
        if ((str2 != null && str2.length() >= 1) || ((str = this.confirmButtonText) != null && str.length() >= 1)) {
            super.show();
            messageTriggered();
            new Handler(Looper.getMainLooper()).post(new MessageShower(this));
        }
    }

    protected static void clearCurrentDialog() {
        Message currentMessage = Messages.getCurrentMessage();
        if (currentMessage != null && (currentMessage instanceof MessageAlert) && currentMessage.orientationWhenShown != StaticMethods.getCurrentOrientation()) {
            MessageAlert messageAlert = (MessageAlert) currentMessage;
            AlertDialog alertDialog2 = messageAlert.alertDialog;
            if (alertDialog2 != null && alertDialog2.isShowing()) {
                messageAlert.alertDialog.dismiss();
            }
            messageAlert.alertDialog = null;
        }
    }
}
