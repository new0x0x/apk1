package com.adobe.mobile;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;
import com.adobe.mobile.FloatingButton;
import com.adobe.mobile.Messages;
import com.adobe.mobile.StaticMethods;
import com.appdynamics.eumagent.runtime.InstrumentationCallbacks;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

class TargetPreviewManager implements FloatingButton.OnButtonDetachedFromWindowListener, FloatingButton.OnPositionChangedListener {
    private static final Object _instanceMutex = new Object();
    private static final Object _targetPreviewTokenMutex = new Object();
    private static TargetPreviewManager targetPreviewManager;
    private final Object _targetPreviewParamsMutex = new Object();
    private float lastFloatingButtonX = -1.0f;
    private float lastFloatingButtonY = -1.0f;
    private String restartDeeplink = null;
    private String targetPreviewApiUiFetchUrlBaseOverride = null;
    private MessageTargetExperienceUIFullScreen targetPreviewExperienceUI = null;
    private String targetPreviewExperienceUIHtml = null;
    private String targetPreviewParams = null;
    private String targetPreviewToken = null;

    /* access modifiers changed from: protected */
    public void setToken(String str) {
        synchronized (_targetPreviewTokenMutex) {
            this.targetPreviewToken = str;
        }
    }

    /* access modifiers changed from: protected */
    public String getToken() {
        String str;
        synchronized (_targetPreviewTokenMutex) {
            str = this.targetPreviewToken;
        }
        return str;
    }

    /* access modifiers changed from: protected */
    public void setPreviewParams(String str) {
        synchronized (this._targetPreviewParamsMutex) {
            this.targetPreviewParams = str;
        }
    }

    private TargetPreviewManager() {
    }

    /* access modifiers changed from: protected */
    public MessageTargetExperienceUIFullScreen getMessageTargetExperienceUIFullscreen() {
        if (this.targetPreviewExperienceUI == null) {
            this.targetPreviewExperienceUI = createMessageTargetExperienceUIFullscreen();
        }
        return this.targetPreviewExperienceUI;
    }

    /* access modifiers changed from: protected */
    public MessageTargetExperienceUIFullScreen createMessageTargetExperienceUIFullscreen() {
        MessageTargetExperienceUIFullScreen messageTargetExperienceUIFullScreen = new MessageTargetExperienceUIFullScreen();
        messageTargetExperienceUIFullScreen.messageId = "TargetPreview-" + UUID.randomUUID();
        messageTargetExperienceUIFullScreen.startDate = new Date(StaticMethods.getTimeSince1970() * 1000);
        messageTargetExperienceUIFullScreen.html = getTargetPreviewExperienceUIHtml();
        messageTargetExperienceUIFullScreen.showRule = Messages.MessageShowRule.MESSAGE_SHOW_RULE_ALWAYS;
        messageTargetExperienceUIFullScreen.triggers = new ArrayList();
        MessageMatcherEquals messageMatcherEquals = new MessageMatcherEquals();
        messageMatcherEquals.key = "a.targetpreview.show";
        messageMatcherEquals.values = new ArrayList<>();
        messageMatcherEquals.values.add("true");
        messageTargetExperienceUIFullScreen.triggers.add(messageMatcherEquals);
        messageTargetExperienceUIFullScreen.audiences = new ArrayList();
        return messageTargetExperienceUIFullScreen;
    }

    private synchronized void showPreviewButton(Activity activity) {
        FloatingButton floatingButton = new FloatingButton((Context) activity, this.lastFloatingButtonX, this.lastFloatingButtonY);
        floatingButton.setTag("ADBFloatingButtonTag");
        InstrumentationCallbacks.setOnClickListenerCalled(floatingButton, new View.OnClickListener() {
            public void onClick(View view) {
                StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
                    public void run() {
                        TargetPreviewManager.this.downloadAndShowTargetPreviewUI();
                    }
                });
            }
        });
        floatingButton.showButton(activity, this, this);
    }

    private void setPreviewButtonLastKnownPosXY(float f, float f2) {
        this.lastFloatingButtonX = f;
        this.lastFloatingButtonY = f2;
    }

    /* access modifiers changed from: package-private */
    public float getLastFloatingButtonX() {
        return this.lastFloatingButtonX;
    }

    /* access modifiers changed from: package-private */
    public float getLastFloatingButtonY() {
        return this.lastFloatingButtonY;
    }

    static TargetPreviewManager getInstance() {
        TargetPreviewManager targetPreviewManager2;
        synchronized (_instanceMutex) {
            if (targetPreviewManager == null) {
                targetPreviewManager = new TargetPreviewManager();
            }
            targetPreviewManager2 = targetPreviewManager;
        }
        return targetPreviewManager2;
    }

    /* access modifiers changed from: package-private */
    public void setTargetPreviewToken(String str) {
        if (str != null && MobileConfig.getInstance().mobileUsingTarget()) {
            setToken(str);
        }
    }

    /* access modifiers changed from: private */
    public String getRequestUrl() {
        String str = "https://hal.testandtarget.omniture.com";
        String str2 = this.targetPreviewApiUiFetchUrlBaseOverride;
        if (str2 != null && !str2.isEmpty()) {
            str = this.targetPreviewApiUiFetchUrlBaseOverride;
        }
        return String.format(Locale.US, str + "/ui/admin/%s/preview/?token=%s", new Object[]{MobileConfig.getInstance().getClientCode(), StaticMethods.URLEncode(getToken())});
    }

    /* access modifiers changed from: package-private */
    public void downloadAndShowTargetPreviewUI() {
        if (getToken() == null || getToken().isEmpty()) {
            StaticMethods.logDebugFormat("No Target Preview token setup!", new Object[0]);
        } else {
            StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
                public void run() {
                    NetworkObject retrieveNetworkObject = RequestHandler.retrieveNetworkObject(TargetPreviewManager.getInstance().getRequestUrl(), "GET", "text/html", (String) null, MobileConfig.getInstance().getDefaultLocationTimeout(), (String) null, "Target Preview", (String) null);
                    if (retrieveNetworkObject == null || retrieveNetworkObject.responseCode != 200 || retrieveNetworkObject.response == null) {
                        try {
                            StaticMethods.getCurrentActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        Toast.makeText(StaticMethods.getCurrentActivity(), "Could not download Target Preview UI. Please try again!", 0).show();
                                    } catch (StaticMethods.NullActivityException e) {
                                        StaticMethods.logDebugFormat("Could not show error message!(%s) ", e);
                                    }
                                }
                            });
                        } catch (StaticMethods.NullActivityException e) {
                            StaticMethods.logDebugFormat("Could not show error message!(%s) ", e);
                        }
                    } else {
                        TargetPreviewManager.this.setTargetPreviewExperienceUIHtml(retrieveNetworkObject.response);
                        MobileConfig.getInstance().enableTargetPreviewMessage();
                        HashMap hashMap = new HashMap();
                        hashMap.put("a.targetpreview.show", "true");
                        Messages.checkForInAppMessage(hashMap, (Map<String, Object>) null, (Map<String, Object>) null);
                    }
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public String getTargetPreviewExperienceUIHtml() {
        return this.targetPreviewExperienceUIHtml;
    }

    /* access modifiers changed from: private */
    public void setTargetPreviewExperienceUIHtml(String str) {
        this.targetPreviewExperienceUIHtml = str;
    }

    private void removeTargetPreviewProperties() {
        setToken((String) null);
        setPreviewParams((String) null);
        setTargetPreviewExperienceUIHtml((String) null);
        setTargetPreviewApiUiFetchUrlBaseOverride((String) null);
        setPreviewButtonLastKnownPosXY(-1.0f, -1.0f);
    }

    /* access modifiers changed from: package-private */
    public void setupPreviewButton(Activity activity) {
        if (activity != null && !(activity instanceof AdobeMarketingActivity)) {
            if (getToken() != null) {
                showPreviewButton(activity);
            } else {
                FloatingButton.hideActiveButton(activity);
            }
        }
    }

    public void onButtonDetached(FloatingButton floatingButton) {
        if (floatingButton != null) {
            setPreviewButtonLastKnownPosXY(floatingButton.getXCompat(), floatingButton.getYCompat());
        }
    }

    public void onPositionChanged(float f, float f2) {
        setPreviewButtonLastKnownPosXY(f, f2);
    }

    /* access modifiers changed from: package-private */
    public String getPreviewRestartDeeplink() {
        return this.restartDeeplink;
    }

    /* access modifiers changed from: package-private */
    public void setTargetPreviewApiUiFetchUrlBaseOverride(String str) {
        this.targetPreviewApiUiFetchUrlBaseOverride = str;
    }

    /* access modifiers changed from: package-private */
    public void disableTargetPreviewMode() {
        MobileConfig.getInstance().disableTargetPreviewMessage();
        removeTargetPreviewProperties();
    }
}
