package com.adobe.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.adobe.mobile.Config;
import com.adobe.mobile.RemoteDownload;
import com.adobe.mobile.StaticMethods;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class MobileConfig {
    private static final MobilePrivacyStatus DEFAULT_PRIVACY_STATUS = MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_IN;
    private static MobileConfig _instance = null;
    private static final Object _instanceMutex = new Object();
    private static InputStream _userDefinedInputStream = null;
    private static final Object _userDefinedInputStreamMutex = new Object();
    private static final Object _usingAnalyticsMutex = new Object();
    private static final Object _usingAudienceManagerMutex = new Object();
    private static final Object _usingGooglePlayServicesMutex = new Object();
    private static final Object _usingTargetMutex = new Object();
    private boolean _aamAnalyticsForwardingEnabled = false;
    private String _aamServer = null;
    private int _aamTimeout = 2;
    private String _acquisitionAppIdentifier = null;
    private String _acquisitionServer = null;
    private Config.AdobeDataCallback _adobeDataCallback = null;
    private boolean _backdateSessionInfoEnabled = true;
    private int _batchLimit = 0;
    private ArrayList<Message> _callbackTemplates = null;
    private String _characterSet = "UTF-8";
    private String _clientCode = null;
    private int _defaultLocationTimeout = 2;
    private long _environmentId = 0;
    /* access modifiers changed from: private */
    public ArrayList<Message> _inAppMessages = null;
    private int _lifecycleTimeout = 300;
    private String _marketingCloudOrganizationId = null;
    private String _marketingCloudServer = null;
    /* access modifiers changed from: private */
    public String _messagesURL = null;
    /* access modifiers changed from: private */
    public boolean _networkConnectivity = false;
    private boolean _offlineTrackingEnabled = false;
    private ArrayList<Message> _piiRequests = null;
    private List<List<Object>> _pointsOfInterest = null;
    private String _pointsOfInterestURL = null;
    private MobilePrivacyStatus _privacyStatus = DEFAULT_PRIVACY_STATUS;
    private int _referrerTimeout = 0;
    private String _reportSuiteIds = null;
    private boolean _ssl = true;
    private String _trackingServer = null;
    private Boolean _usingAnalytics = null;
    private Boolean _usingAudienceManager = null;
    private Boolean _usingGooglePlayServices = null;
    private Boolean _usingTarget = null;

    protected static MobileConfig getInstance() {
        MobileConfig mobileConfig;
        synchronized (_instanceMutex) {
            if (_instance == null) {
                _instance = new MobileConfig();
            }
            mobileConfig = _instance;
        }
        return mobileConfig;
    }

    private MobileConfig() {
        JSONObject jSONObject;
        JSONObject jSONObject2;
        JSONObject jSONObject3;
        JSONObject jSONObject4;
        JSONObject jSONObject5;
        JSONArray jSONArray;
        JSONObject jSONObject6;
        String str;
        startNotifier();
        JSONObject loadBundleConfig = loadBundleConfig();
        if (loadBundleConfig != null) {
            try {
                jSONObject = loadBundleConfig.getJSONObject("analytics");
            } catch (JSONException unused) {
                StaticMethods.logDebugFormat("Analytics - Not configured.", new Object[0]);
                jSONObject = null;
            }
            if (jSONObject != null) {
                try {
                    this._trackingServer = jSONObject.getString("server");
                    this._reportSuiteIds = jSONObject.getString("rsids");
                } catch (JSONException unused2) {
                    this._trackingServer = null;
                    this._reportSuiteIds = null;
                    StaticMethods.logDebugFormat("Analytics - Not Configured.", new Object[0]);
                }
                try {
                    this._characterSet = jSONObject.getString("charset");
                } catch (JSONException unused3) {
                    this._characterSet = "UTF-8";
                }
                try {
                    this._ssl = jSONObject.getBoolean("ssl");
                } catch (JSONException unused4) {
                    this._ssl = true;
                }
                try {
                    this._offlineTrackingEnabled = jSONObject.getBoolean("offlineEnabled");
                } catch (JSONException unused5) {
                    this._offlineTrackingEnabled = false;
                }
                try {
                    this._backdateSessionInfoEnabled = jSONObject.getBoolean("backdateSessionInfo");
                } catch (JSONException unused6) {
                    this._backdateSessionInfoEnabled = true;
                }
                try {
                    this._lifecycleTimeout = jSONObject.getInt("lifecycleTimeout");
                } catch (JSONException unused7) {
                    this._lifecycleTimeout = 300;
                }
                try {
                    this._referrerTimeout = jSONObject.getInt("referrerTimeout");
                } catch (JSONException unused8) {
                    this._referrerTimeout = 0;
                }
                try {
                    this._batchLimit = jSONObject.getInt("batchLimit");
                } catch (JSONException unused9) {
                    this._batchLimit = 0;
                }
                try {
                    if (StaticMethods.getSharedPreferences().contains("PrivacyStatus")) {
                        this._privacyStatus = MobilePrivacyStatus.values()[StaticMethods.getSharedPreferences().getInt("PrivacyStatus", 0)];
                    } else {
                        try {
                            str = jSONObject.getString("privacyDefault");
                        } catch (JSONException unused10) {
                            str = null;
                        }
                        this._privacyStatus = str != null ? privacyStatusFromString(str) : DEFAULT_PRIVACY_STATUS;
                    }
                    try {
                        loadPoiFromJsonArray(jSONObject.getJSONArray("poi"));
                    } catch (JSONException e) {
                        StaticMethods.logErrorFormat("Analytics - Malformed POI List(%s)", e.getLocalizedMessage());
                    }
                } catch (StaticMethods.NullContextException e2) {
                    StaticMethods.logErrorFormat("Config - Error pulling privacy from shared preferences. (%s)", e2.getMessage());
                    return;
                }
            }
            try {
                jSONObject2 = loadBundleConfig.getJSONObject("target");
            } catch (JSONException unused11) {
                StaticMethods.logDebugFormat("Target - Not Configured.", new Object[0]);
                jSONObject2 = null;
            }
            if (jSONObject2 != null) {
                try {
                    this._clientCode = jSONObject2.getString("clientCode");
                } catch (JSONException unused12) {
                    this._clientCode = null;
                    StaticMethods.logDebugFormat("Target - Not Configured.", new Object[0]);
                }
                try {
                    this._defaultLocationTimeout = jSONObject2.getInt("timeout");
                } catch (JSONException unused13) {
                    this._defaultLocationTimeout = 2;
                }
                try {
                    this._environmentId = jSONObject2.getLong("environmentId");
                } catch (JSONException unused14) {
                    this._environmentId = 0;
                }
            }
            try {
                jSONObject3 = loadBundleConfig.getJSONObject("audienceManager");
            } catch (JSONException unused15) {
                StaticMethods.logDebugFormat("Audience Manager - Not Configured.", new Object[0]);
                jSONObject3 = null;
            }
            if (jSONObject3 != null) {
                try {
                    this._aamServer = jSONObject3.getString("server");
                } catch (JSONException unused16) {
                    this._aamServer = null;
                    StaticMethods.logDebugFormat("Audience Manager - Not Configured.", new Object[0]);
                }
                try {
                    this._aamAnalyticsForwardingEnabled = jSONObject3.getBoolean("analyticsForwardingEnabled");
                } catch (JSONException unused17) {
                    this._aamAnalyticsForwardingEnabled = false;
                }
                if (this._aamAnalyticsForwardingEnabled) {
                    StaticMethods.logDebugFormat("Audience Manager - Analytics Server-Side Forwarding Is Enabled.", new Object[0]);
                }
                try {
                    this._aamTimeout = jSONObject3.getInt("timeout");
                } catch (JSONException unused18) {
                    this._aamTimeout = 2;
                }
            }
            try {
                jSONObject4 = loadBundleConfig.getJSONObject("acquisition");
            } catch (JSONException unused19) {
                StaticMethods.logDebugFormat("Acquisition - Not Configured.", new Object[0]);
                jSONObject4 = null;
            }
            if (jSONObject4 != null) {
                try {
                    this._acquisitionAppIdentifier = jSONObject4.getString("appid");
                    this._acquisitionServer = jSONObject4.getString("server");
                } catch (JSONException unused20) {
                    this._acquisitionAppIdentifier = null;
                    this._acquisitionServer = null;
                    StaticMethods.logDebugFormat("Acquisition - Not configured correctly (missing server or app identifier).", new Object[0]);
                }
            }
            try {
                jSONObject5 = loadBundleConfig.getJSONObject("remotes");
            } catch (JSONException unused21) {
                StaticMethods.logDebugFormat("Remotes - Not Configured.", new Object[0]);
                jSONObject5 = null;
            }
            if (jSONObject5 != null) {
                try {
                    this._messagesURL = jSONObject5.getString("messages");
                } catch (JSONException e3) {
                    StaticMethods.logDebugFormat("Config - No in-app messages remote url loaded (%s)", e3.getLocalizedMessage());
                }
                try {
                    this._pointsOfInterestURL = jSONObject5.getString("analytics.poi");
                } catch (JSONException e4) {
                    StaticMethods.logDebugFormat("Config - No points of interest remote url loaded (%s)", e4.getLocalizedMessage());
                }
            }
            try {
                jSONArray = loadBundleConfig.getJSONArray("messages");
            } catch (JSONException unused22) {
                StaticMethods.logDebugFormat("Messages - Not configured locally.", new Object[0]);
                jSONArray = null;
            }
            if (jSONArray != null && jSONArray.length() > 0) {
                loadMessagesFromJsonArray(jSONArray);
            }
            try {
                jSONObject6 = loadBundleConfig.getJSONObject("marketingCloud");
            } catch (JSONException unused23) {
                StaticMethods.logDebugFormat("Marketing Cloud - Not configured locally.", new Object[0]);
                jSONObject6 = null;
            }
            if (jSONObject6 != null) {
                try {
                    this._marketingCloudOrganizationId = jSONObject6.getString("org");
                } catch (JSONException unused24) {
                    this._marketingCloudOrganizationId = null;
                    StaticMethods.logDebugFormat("Visitor - ID Service Not Configured.", new Object[0]);
                }
                try {
                    this._marketingCloudServer = jSONObject6.getString("server");
                } catch (JSONException unused25) {
                    this._marketingCloudServer = null;
                    StaticMethods.logDebugFormat("Visitor ID Service - Custom endpoint not found in configuration, using default endpoint.", new Object[0]);
                }
            }
            loadCachedRemotes();
            updateBlacklist();
        }
    }

    /* access modifiers changed from: protected */
    public boolean mobileUsingAnalytics() {
        boolean booleanValue;
        synchronized (_usingAnalyticsMutex) {
            if (this._usingAnalytics == null) {
                this._usingAnalytics = Boolean.valueOf(getReportSuiteIds() != null && getReportSuiteIds().length() > 0 && getTrackingServer() != null && getTrackingServer().length() > 0);
                if (!this._usingAnalytics.booleanValue()) {
                    StaticMethods.logDebugFormat("Analytics - Your config file is not set up to use Analytics(missing report suite id(s) or tracking server information)", new Object[0]);
                }
            }
            booleanValue = this._usingAnalytics.booleanValue();
        }
        return booleanValue;
    }

    /* access modifiers changed from: protected */
    public boolean mobileUsingGooglePlayServices() {
        boolean booleanValue;
        synchronized (_usingGooglePlayServicesMutex) {
            if (this._usingGooglePlayServices == null) {
                this._usingGooglePlayServices = Boolean.valueOf(WearableFunctionBridge.isGooglePlayServicesEnabled());
            }
            booleanValue = this._usingGooglePlayServices.booleanValue();
        }
        return booleanValue;
    }

    /* access modifiers changed from: protected */
    public boolean mobileUsingAudienceManager() {
        boolean booleanValue;
        if (StaticMethods.isWearableApp()) {
            return false;
        }
        synchronized (_usingAudienceManagerMutex) {
            if (this._usingAudienceManager == null) {
                this._usingAudienceManager = Boolean.valueOf(getAamServer() != null && getAamServer().length() > 0);
                if (!this._usingAudienceManager.booleanValue()) {
                    StaticMethods.logDebugFormat("Audience Manager - Your config file is not set up to use Audience Manager(missing audience manager server information)", new Object[0]);
                }
            }
            booleanValue = this._usingAudienceManager.booleanValue();
        }
        return booleanValue;
    }

    /* access modifiers changed from: protected */
    public boolean mobileUsingTarget() {
        boolean booleanValue;
        if (StaticMethods.isWearableApp()) {
            return false;
        }
        synchronized (_usingTargetMutex) {
            if (this._usingTarget == null) {
                this._usingTarget = Boolean.valueOf(getClientCode() != null && getClientCode().length() > 0);
                if (!this._usingTarget.booleanValue()) {
                    StaticMethods.logDebugFormat("Target Worker - Your config file is not set up to use Target(missing client code information)", new Object[0]);
                }
            }
            booleanValue = this._usingTarget.booleanValue();
        }
        return booleanValue;
    }

    /* access modifiers changed from: protected */
    public boolean mobileReferrerConfigured() {
        String str = this._acquisitionServer;
        return str != null && this._acquisitionAppIdentifier != null && str.length() > 0 && this._acquisitionAppIdentifier.length() > 0;
    }

    /* access modifiers changed from: protected */
    public void invokeAdobeDataCallback(Config.MobileDataEvent mobileDataEvent, Map<String, Object> map) {
        Config.AdobeDataCallback adobeDataCallback = this._adobeDataCallback;
        if (adobeDataCallback == null) {
            StaticMethods.logDebugFormat("Config - A callback has not been registered for Adobe events.", new Object[0]);
        } else if (map != null) {
            adobeDataCallback.call(mobileDataEvent, new HashMap(map));
        } else {
            adobeDataCallback.call(mobileDataEvent, (Map<String, Object>) null);
        }
    }

    /* access modifiers changed from: protected */
    public String getReportSuiteIds() {
        return this._reportSuiteIds;
    }

    /* access modifiers changed from: protected */
    public String getTrackingServer() {
        return this._trackingServer;
    }

    /* access modifiers changed from: protected */
    public String getCharacterSet() {
        return this._characterSet;
    }

    /* access modifiers changed from: protected */
    public boolean getSSL() {
        return this._ssl;
    }

    /* access modifiers changed from: protected */
    public boolean getOfflineTrackingEnabled() {
        return this._offlineTrackingEnabled;
    }

    /* access modifiers changed from: protected */
    public boolean getBackdateSessionInfoEnabled() {
        return this._backdateSessionInfoEnabled;
    }

    /* access modifiers changed from: protected */
    public int getLifecycleTimeout() {
        return this._lifecycleTimeout;
    }

    /* access modifiers changed from: protected */
    public int getBatchLimit() {
        return this._batchLimit;
    }

    /* access modifiers changed from: protected */
    public void setPrivacyStatus(MobilePrivacyStatus mobilePrivacyStatus) {
        if (mobilePrivacyStatus != null) {
            if (mobilePrivacyStatus != MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_UNKNOWN || this._offlineTrackingEnabled) {
                if (mobilePrivacyStatus == MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_IN) {
                    StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
                        public void run() {
                            StaticMethods.logDebugFormat("Analytics - Privacy status set to opt in, attempting to send all hits in queue.", new Object[0]);
                            AnalyticsWorker.sharedInstance().kick(false);
                        }
                    });
                    StaticMethods.getThirdPartyCallbacksExecutor().execute(new Runnable() {
                        public void run() {
                            StaticMethods.logDebugFormat("Data Callback - Privacy status set to opt in, attempting to send all requests in queue", new Object[0]);
                            ThirdPartyQueue.sharedInstance().kick(false);
                        }
                    });
                    StaticMethods.getPIIExecutor().execute(new Runnable() {
                        public void run() {
                            StaticMethods.logDebugFormat("Pii Callback - Privacy status set to opt in, attempting to send all requests in queue", new Object[0]);
                            PiiQueue.sharedInstance().kick(false);
                        }
                    });
                }
                if (mobilePrivacyStatus == MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_OUT) {
                    StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
                        public void run() {
                            StaticMethods.logDebugFormat("Analytics - Privacy status set to opt out, attempting to clear Analytics queue of all hits.", new Object[0]);
                            AnalyticsWorker.sharedInstance().clearTrackingQueue();
                            StaticMethods.logDebugFormat("Target - Privacy status set to opt out, clearing stored Target values.", new Object[0]);
                            Target.clearCookies();
                        }
                    });
                    StaticMethods.getThirdPartyCallbacksExecutor().execute(new Runnable() {
                        public void run() {
                            StaticMethods.logDebugFormat("Data Callback - Privacy status set to opt out, attempting to clear queue of all requests", new Object[0]);
                            ThirdPartyQueue.sharedInstance().clearTrackingQueue();
                        }
                    });
                    StaticMethods.getPIIExecutor().execute(new Runnable() {
                        public void run() {
                            StaticMethods.logDebugFormat("PII - Privacy status set to opt out, attempting to clear queue of all requests.", new Object[0]);
                            PiiQueue.sharedInstance().clearTrackingQueue();
                        }
                    });
                    StaticMethods.getAudienceExecutor().execute(new Runnable() {
                        public void run() {
                            StaticMethods.logDebugFormat("Audience Manager - Privacy status set to opt out, clearing Audience Manager information.", new Object[0]);
                            AudienceManagerWorker.Reset();
                        }
                    });
                }
                this._privacyStatus = mobilePrivacyStatus;
                WearableFunctionBridge.syncPrivacyStatusToWearable(mobilePrivacyStatus.getValue());
                try {
                    SharedPreferences.Editor sharedPreferencesEditor = StaticMethods.getSharedPreferencesEditor();
                    sharedPreferencesEditor.putInt("PrivacyStatus", mobilePrivacyStatus.getValue());
                    sharedPreferencesEditor.commit();
                } catch (StaticMethods.NullContextException e) {
                    StaticMethods.logErrorFormat("Config - Error persisting privacy status (%s).", e.getMessage());
                }
            } else {
                StaticMethods.logWarningFormat("Analytics - Cannot set privacy status to unknown when offline tracking is disabled", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: protected */
    public MobilePrivacyStatus getPrivacyStatus() {
        return this._privacyStatus;
    }

    /* access modifiers changed from: protected */
    public int getReferrerTimeout() {
        return this._referrerTimeout * 1000;
    }

    /* access modifiers changed from: protected */
    public int getAnalyticsResponseType() {
        return this._aamAnalyticsForwardingEnabled ? 10 : 0;
    }

    /* access modifiers changed from: protected */
    public String getClientCode() {
        return this._clientCode;
    }

    /* access modifiers changed from: protected */
    public int getDefaultLocationTimeout() {
        return this._defaultLocationTimeout;
    }

    /* access modifiers changed from: protected */
    public String getAamServer() {
        return this._aamServer;
    }

    /* access modifiers changed from: protected */
    public boolean getAamAnalyticsForwardingEnabled() {
        return this._aamAnalyticsForwardingEnabled;
    }

    /* access modifiers changed from: protected */
    public int getAamTimeout() {
        return this._aamTimeout;
    }

    /* access modifiers changed from: protected */
    public String getAcquisitionAppId() {
        return this._acquisitionAppIdentifier;
    }

    /* access modifiers changed from: protected */
    public String getAcquisitionServer() {
        return this._acquisitionServer;
    }

    /* access modifiers changed from: protected */
    public void downloadRemoteConfigs() {
        StaticMethods.getMessagesExecutor().execute(new Runnable() {
            public void run() {
                if (MobileConfig.this._messagesURL == null || MobileConfig.this._messagesURL.length() <= 0) {
                    MobileConfig.this.loadMessageImages();
                } else {
                    RemoteDownload.remoteDownloadSync(MobileConfig.this._messagesURL, new RemoteDownload.RemoteDownloadBlock() {
                        public void call(boolean z, File file) {
                            MobileConfig.this.updateMessagesData(file);
                            MobileConfig.this.loadMessageImages();
                            MobileConfig.this.updateBlacklist();
                        }
                    });
                }
            }
        });
        StaticMethods.getThirdPartyCallbacksExecutor().execute(new Runnable() {
            public void run() {
                FutureTask futureTask = new FutureTask(new Callable<Void>() {
                    public Void call() throws Exception {
                        return null;
                    }
                });
                StaticMethods.getMessagesExecutor().execute(futureTask);
                try {
                    futureTask.get();
                } catch (Exception e) {
                    StaticMethods.logErrorFormat("Data Callback - Error waiting for callbacks being loaded (%s)", e.getMessage());
                }
            }
        });
        StaticMethods.getPIIExecutor().execute(new Runnable() {
            public void run() {
                FutureTask futureTask = new FutureTask(new Callable<Void>() {
                    public Void call() throws Exception {
                        return null;
                    }
                });
                StaticMethods.getMessagesExecutor().execute(futureTask);
                try {
                    futureTask.get();
                } catch (Exception e) {
                    StaticMethods.logErrorFormat("Pii Callback - Error waiting for callbacks being loaded (%s)", e.getMessage());
                }
            }
        });
        String str = this._pointsOfInterestURL;
        if (str != null && str.length() > 0) {
            RemoteDownload.remoteDownloadAsync(this._pointsOfInterestURL, new RemoteDownload.RemoteDownloadBlock() {
                public void call(boolean z, final File file) {
                    StaticMethods.getAnalyticsExecutor().execute(new Runnable() {
                        public void run() {
                            if (file != null) {
                                StaticMethods.logDebugFormat("Config - Using remote definition for points of interest", new Object[0]);
                                MobileConfig.this.updatePOIData(file);
                            }
                        }
                    });
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0040 A[SYNTHETIC, Splitter:B:24:0x0040] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0060 A[SYNTHETIC, Splitter:B:33:0x0060] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0073 A[SYNTHETIC, Splitter:B:38:0x0073] */
    /* JADX WARNING: Removed duplicated region for block: B:44:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A[RETURN, SYNTHETIC] */
    public void updateMessagesData(File file) {
        String str;
        Object[] objArr;
        if (file != null) {
            FileInputStream fileInputStream = null;
            try {
                FileInputStream fileInputStream2 = new FileInputStream(file);
                try {
                    loadMessagesDataFromRemote(loadConfigFromStream(fileInputStream2));
                    try {
                        fileInputStream2.close();
                        return;
                    } catch (IOException e) {
                        str = "Messages - Unable to close file stream (%s)";
                        objArr = new Object[]{e.getLocalizedMessage()};
                    }
                } catch (JSONException e2) {
                    e = e2;
                    fileInputStream = fileInputStream2;
                    StaticMethods.logErrorFormat("Messages - Unable to read messages remote config file, falling back to bundled messages (%s)", e.getLocalizedMessage());
                    if (fileInputStream == null) {
                        try {
                            fileInputStream.close();
                            return;
                        } catch (IOException e3) {
                            str = "Messages - Unable to close file stream (%s)";
                            objArr = new Object[]{e3.getLocalizedMessage()};
                        }
                    } else {
                        return;
                    }
                } catch (IOException e4) {
                    e = e4;
                    fileInputStream = fileInputStream2;
                    try {
                        StaticMethods.logWarningFormat("Messages - Unable to open messages config file, falling back to bundled messages (%s)", e.getLocalizedMessage());
                        if (fileInputStream == null) {
                            try {
                                fileInputStream.close();
                                return;
                            } catch (IOException e5) {
                                str = "Messages - Unable to close file stream (%s)";
                                objArr = new Object[]{e5.getLocalizedMessage()};
                            }
                        } else {
                            return;
                        }
                    } catch (Throwable th) {
                        th = th;
                        if (fileInputStream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = fileInputStream2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e6) {
                            StaticMethods.logErrorFormat("Messages - Unable to close file stream (%s)", e6.getLocalizedMessage());
                        }
                    }
                    throw th;
                }
            } catch (JSONException e7) {
                e = e7;
                StaticMethods.logErrorFormat("Messages - Unable to read messages remote config file, falling back to bundled messages (%s)", e.getLocalizedMessage());
                if (fileInputStream == null) {
                }
            } catch (IOException e8) {
                e = e8;
                StaticMethods.logWarningFormat("Messages - Unable to open messages config file, falling back to bundled messages (%s)", e.getLocalizedMessage());
                if (fileInputStream == null) {
                }
            }
        } else {
            return;
        }
        StaticMethods.logErrorFormat(str, objArr);
    }

    /* access modifiers changed from: protected */
    public void enableTargetPreviewMessage() {
        if (this._inAppMessages == null) {
            this._inAppMessages = new ArrayList<>();
        }
        MessageTargetExperienceUIFullScreen messageTargetExperienceUIFullscreen = TargetPreviewManager.getInstance().getMessageTargetExperienceUIFullscreen();
        if (Messages.getFullScreenMessageById(messageTargetExperienceUIFullscreen.messageId) == null) {
            this._inAppMessages.add(messageTargetExperienceUIFullscreen);
        }
    }

    /* access modifiers changed from: protected */
    public void disableTargetPreviewMessage() {
        if (this._inAppMessages != null) {
            MessageTargetExperienceUIFullScreen messageTargetExperienceUIFullscreen = TargetPreviewManager.getInstance().getMessageTargetExperienceUIFullscreen();
            Iterator<Message> it = this._inAppMessages.iterator();
            while (it.hasNext()) {
                if (it.next().messageId.equalsIgnoreCase(messageTargetExperienceUIFullscreen.messageId)) {
                    it.remove();
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x004e A[SYNTHETIC, Splitter:B:26:0x004e] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x006e A[SYNTHETIC, Splitter:B:35:0x006e] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0081 A[SYNTHETIC, Splitter:B:40:0x0081] */
    /* JADX WARNING: Removed duplicated region for block: B:46:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:47:? A[RETURN, SYNTHETIC] */
    public void updatePOIData(File file) {
        String str;
        Object[] objArr;
        if (file != null) {
            FileInputStream fileInputStream = null;
            try {
                FileInputStream fileInputStream2 = new FileInputStream(file);
                try {
                    JSONObject loadConfigFromStream = loadConfigFromStream(fileInputStream2);
                    if (loadConfigFromStream != null) {
                        loadPoiFromJsonArray(loadConfigFromStream.getJSONObject("analytics").getJSONArray("poi"));
                    }
                    try {
                        fileInputStream2.close();
                        return;
                    } catch (IOException e) {
                        str = "Config - Unable to close file stream (%s)";
                        objArr = new Object[]{e.getLocalizedMessage()};
                    }
                } catch (JSONException e2) {
                    e = e2;
                    fileInputStream = fileInputStream2;
                    StaticMethods.logErrorFormat("Config - Unable to read points of interest remote config file, falling back to bundled poi (%s)", e.getLocalizedMessage());
                    if (fileInputStream == null) {
                        try {
                            fileInputStream.close();
                            return;
                        } catch (IOException e3) {
                            str = "Config - Unable to close file stream (%s)";
                            objArr = new Object[]{e3.getLocalizedMessage()};
                        }
                    } else {
                        return;
                    }
                } catch (IOException e4) {
                    e = e4;
                    fileInputStream = fileInputStream2;
                    try {
                        StaticMethods.logWarningFormat("Config - Unable to open points of interest config file, falling back to bundled poi (%s)", e.getLocalizedMessage());
                        if (fileInputStream == null) {
                            try {
                                fileInputStream.close();
                                return;
                            } catch (IOException e5) {
                                str = "Config - Unable to close file stream (%s)";
                                objArr = new Object[]{e5.getLocalizedMessage()};
                            }
                        } else {
                            return;
                        }
                    } catch (Throwable th) {
                        th = th;
                        if (fileInputStream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = fileInputStream2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e6) {
                            StaticMethods.logErrorFormat("Config - Unable to close file stream (%s)", e6.getLocalizedMessage());
                        }
                    }
                    throw th;
                }
            } catch (JSONException e7) {
                e = e7;
                StaticMethods.logErrorFormat("Config - Unable to read points of interest remote config file, falling back to bundled poi (%s)", e.getLocalizedMessage());
                if (fileInputStream == null) {
                }
            } catch (IOException e8) {
                e = e8;
                StaticMethods.logWarningFormat("Config - Unable to open points of interest config file, falling back to bundled poi (%s)", e.getLocalizedMessage());
                if (fileInputStream == null) {
                }
            }
        } else {
            return;
        }
        StaticMethods.logErrorFormat(str, objArr);
    }

    /* access modifiers changed from: protected */
    public ArrayList<Message> getInAppMessages() {
        return this._inAppMessages;
    }

    /* access modifiers changed from: protected */
    public ArrayList<Message> getCallbackTemplates() {
        return this._callbackTemplates;
    }

    /* access modifiers changed from: protected */
    public String getMarketingCloudOrganizationId() {
        return this._marketingCloudOrganizationId;
    }

    /* access modifiers changed from: protected */
    public String getMarketingCloudCustomServer() {
        return this._marketingCloudServer;
    }

    /* access modifiers changed from: protected */
    public boolean getVisitorIdServiceEnabled() {
        String str = this._marketingCloudOrganizationId;
        return str != null && str.length() > 0;
    }

    private JSONObject loadBundleConfig() {
        InputStream inputStream;
        synchronized (_userDefinedInputStreamMutex) {
            inputStream = _userDefinedInputStream;
        }
        JSONObject jSONObject = null;
        if (inputStream != null) {
            try {
                StaticMethods.logDebugFormat("Config - Attempting to load config file from override stream", new Object[0]);
                jSONObject = loadConfigFromStream(inputStream);
            } catch (IOException e) {
                StaticMethods.logDebugFormat("Config - Error loading user defined config (%s)", e.getMessage());
            } catch (JSONException e2) {
                StaticMethods.logDebugFormat("Config - Error parsing user defined config (%s)", e2.getMessage());
            }
        }
        if (jSONObject != null) {
            return jSONObject;
        }
        if (inputStream != null) {
            StaticMethods.logDebugFormat("Config - Failed attempting to load custom config, will fall back to standard config location.", new Object[0]);
        }
        StaticMethods.logDebugFormat("Config - Attempting to load config file from default location", new Object[0]);
        JSONObject loadConfigFile = loadConfigFile("ADBMobileConfig.json");
        if (loadConfigFile != null) {
            return loadConfigFile;
        }
        StaticMethods.logDebugFormat("Config - Could not find config file at expected location.  Attempting to load from www folder", new Object[0]);
        return loadConfigFile("www" + File.separator + "ADBMobileConfig.json");
    }

    private JSONObject loadConfigFile(String str) {
        AssetManager assets;
        try {
            Resources resources = StaticMethods.getSharedContext().getResources();
            if (resources == null || (assets = resources.getAssets()) == null) {
                return null;
            }
            return loadConfigFromStream(assets.open(str));
        } catch (IOException e) {
            StaticMethods.logErrorFormat("Config - Exception loading config file (%s)", e.getMessage());
            return null;
        } catch (JSONException e2) {
            StaticMethods.logErrorFormat("Config - Exception parsing config file (%s)", e2.getMessage());
            return null;
        } catch (StaticMethods.NullContextException e3) {
            StaticMethods.logErrorFormat("Config - Null context when attempting to read config file (%s)", e3.getMessage());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void loadCachedRemotes() {
        String str = this._messagesURL;
        if (str != null && str.length() > 0) {
            updateMessagesData(RemoteDownload.getFileForCachedURL(this._messagesURL));
        }
        String str2 = this._pointsOfInterestURL;
        if (str2 != null && str2.length() > 0) {
            updatePOIData(RemoteDownload.getFileForCachedURL(this._pointsOfInterestURL));
        }
    }

    private JSONObject loadConfigFromStream(InputStream inputStream) throws JSONException, IOException {
        String str;
        Object[] objArr;
        if (inputStream == null) {
            return null;
        }
        try {
            byte[] bArr = new byte[inputStream.available()];
            inputStream.read(bArr);
            JSONObject jSONObject = new JSONObject(new String(bArr, "UTF-8"));
            try {
                inputStream.close();
            } catch (IOException e) {
                StaticMethods.logErrorFormat("Config - Unable to close stream (%s)", e.getMessage());
            }
            return jSONObject;
        } catch (IOException e2) {
            StaticMethods.logErrorFormat("Config - Exception when reading config (%s)", e2.getMessage());
            try {
                inputStream.close();
            } catch (IOException e3) {
                str = "Config - Unable to close stream (%s)";
                objArr = new Object[]{e3.getMessage()};
            }
            return new JSONObject();
        } catch (NullPointerException e4) {
            StaticMethods.logErrorFormat("Config - Stream closed when attempting to load config (%s)", e4.getMessage());
            try {
                inputStream.close();
            } catch (IOException e5) {
                str = "Config - Unable to close stream (%s)";
                objArr = new Object[]{e5.getMessage()};
            }
            return new JSONObject();
        } catch (Throwable th) {
            try {
                inputStream.close();
            } catch (IOException e6) {
                StaticMethods.logErrorFormat("Config - Unable to close stream (%s)", e6.getMessage());
            }
            throw th;
        }
        StaticMethods.logErrorFormat(str, objArr);
        return new JSONObject();
    }

    private MobilePrivacyStatus privacyStatusFromString(String str) {
        if (str != null) {
            if (str.equalsIgnoreCase("optedin")) {
                return MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_IN;
            }
            if (str.equalsIgnoreCase("optedout")) {
                return MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_OUT;
            }
            if (str.equalsIgnoreCase("optunknown")) {
                return MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_UNKNOWN;
            }
        }
        return DEFAULT_PRIVACY_STATUS;
    }

    private void loadPoiFromJsonArray(JSONArray jSONArray) {
        if (jSONArray != null) {
            try {
                this._pointsOfInterest = new ArrayList();
                int length = jSONArray.length();
                for (int i = 0; i < length; i++) {
                    JSONArray jSONArray2 = jSONArray.getJSONArray(i);
                    ArrayList arrayList = new ArrayList(4);
                    arrayList.add(jSONArray2.getString(0));
                    arrayList.add(Double.valueOf(jSONArray2.getDouble(1)));
                    arrayList.add(Double.valueOf(jSONArray2.getDouble(2)));
                    arrayList.add(Double.valueOf(jSONArray2.getDouble(3)));
                    this._pointsOfInterest.add(arrayList);
                }
            } catch (JSONException e) {
                StaticMethods.logErrorFormat("Messages - Unable to parse remote points of interest JSON (%s)", e.getMessage());
            }
        }
    }

    private void loadMessagesDataFromRemote(JSONObject jSONObject) {
        JSONArray jSONArray;
        if (jSONObject == null) {
            StaticMethods.logDebugFormat("Messages - Remote messages config was null, falling back to bundled messages", new Object[0]);
            RemoteDownload.deleteFilesInDirectory("messageImages");
            return;
        }
        try {
            jSONArray = jSONObject.getJSONArray("messages");
        } catch (JSONException unused) {
            StaticMethods.logDebugFormat("Messages - Remote messages not configured, falling back to bundled messages", new Object[0]);
            jSONArray = null;
        }
        StaticMethods.logDebugFormat("Messages - Using remote definition for messages", new Object[0]);
        if (jSONArray == null || jSONArray.length() <= 0) {
            RemoteDownload.deleteFilesInDirectory("messageImages");
            this._inAppMessages = null;
            this._callbackTemplates = null;
            this._piiRequests = null;
            return;
        }
        loadMessagesFromJsonArray(jSONArray);
    }

    private void loadMessagesFromJsonArray(JSONArray jSONArray) {
        try {
            ArrayList<Message> arrayList = new ArrayList<>();
            ArrayList<Message> arrayList2 = new ArrayList<>();
            ArrayList<Message> arrayList3 = new ArrayList<>();
            int length = jSONArray.length();
            for (int i = 0; i < length; i++) {
                Message messageWithJsonObject = Message.messageWithJsonObject(jSONArray.getJSONObject(i));
                if (messageWithJsonObject != null) {
                    StaticMethods.logDebugFormat("Messages - loaded message - %s", messageWithJsonObject.description());
                    if (messageWithJsonObject.getClass() == MessageTemplatePii.class) {
                        arrayList3.add(messageWithJsonObject);
                    } else {
                        if (messageWithJsonObject.getClass() != MessageTemplateCallback.class) {
                            if (messageWithJsonObject.getClass() != MessageOpenURL.class) {
                                arrayList.add(messageWithJsonObject);
                            }
                        }
                        arrayList2.add(messageWithJsonObject);
                    }
                }
            }
            this._inAppMessages = arrayList;
            this._callbackTemplates = arrayList2;
            this._piiRequests = arrayList3;
        } catch (JSONException e) {
            StaticMethods.logErrorFormat("Messages - Unable to parse messages JSON (%s)", e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public void updateBlacklist() {
        ArrayList<Message> arrayList = this._inAppMessages;
        if (arrayList != null) {
            Iterator<Message> it = arrayList.iterator();
            while (it.hasNext()) {
                Message next = it.next();
                HashMap<String, Integer> loadBlacklist = next.loadBlacklist();
                if (next.isBlacklisted() && next.showRule.getValue() != loadBlacklist.get(next.messageId).intValue()) {
                    next.removeFromBlacklist();
                }
            }
        }
        ArrayList<Message> arrayList2 = this._callbackTemplates;
        if (arrayList2 != null) {
            Iterator<Message> it2 = arrayList2.iterator();
            while (it2.hasNext()) {
                Message next2 = it2.next();
                HashMap<String, Integer> loadBlacklist2 = next2.loadBlacklist();
                if (next2.isBlacklisted() && next2.showRule.getValue() != loadBlacklist2.get(next2.messageId).intValue()) {
                    next2.removeFromBlacklist();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void loadMessageImages() {
        StaticMethods.getMessageImageCachingExecutor().execute(new Runnable() {
            public void run() {
                if (MobileConfig.this._inAppMessages == null || MobileConfig.this._inAppMessages.size() <= 0) {
                    RemoteDownload.deleteFilesInDirectory("messageImages");
                    return;
                }
                ArrayList arrayList = new ArrayList();
                Iterator it = MobileConfig.this._inAppMessages.iterator();
                while (it.hasNext()) {
                    Message message = (Message) it.next();
                    if (message.assets != null && message.assets.size() > 0) {
                        Iterator<ArrayList<String>> it2 = message.assets.iterator();
                        while (it2.hasNext()) {
                            ArrayList next = it2.next();
                            if (next.size() > 0) {
                                Iterator it3 = next.iterator();
                                while (it3.hasNext()) {
                                    String str = (String) it3.next();
                                    arrayList.add(str);
                                    RemoteDownload.remoteDownloadSync(str, 10000, 10000, (RemoteDownload.RemoteDownloadBlock) null, "messageImages");
                                }
                            }
                        }
                    }
                }
                if (arrayList.size() > 0) {
                    RemoteDownload.deleteFilesForDirectoryNotInList("messageImages", arrayList);
                } else {
                    RemoteDownload.deleteFilesInDirectory("messageImages");
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public boolean networkConnectivity() {
        return StaticMethods.isWearableApp() || this._networkConnectivity;
    }

    /* access modifiers changed from: protected */
    public void startNotifier() {
        Context context;
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        try {
            context = StaticMethods.getSharedContext().getApplicationContext();
        } catch (StaticMethods.NullContextException e) {
            StaticMethods.logErrorFormat("Analytics - Error registering network receiver (%s)", e.getMessage());
            context = null;
        }
        if (context != null) {
            context.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    MobileConfig mobileConfig = MobileConfig.this;
                    boolean unused = mobileConfig._networkConnectivity = mobileConfig.getNetworkConnectivity(context);
                    if (MobileConfig.this._networkConnectivity) {
                        StaticMethods.logDebugFormat("Analytics - Network status changed (reachable)", new Object[0]);
                        AnalyticsWorker.sharedInstance().kick(false);
                        return;
                    }
                    StaticMethods.logDebugFormat("Analytics - Network status changed (unreachable)", new Object[0]);
                }
            }, intentFilter);
        }
    }

    /* access modifiers changed from: protected */
    public boolean getNetworkConnectivity(Context context) {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4 = true;
        if (context != null) {
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
                if (connectivityManager != null) {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo == null) {
                        try {
                            StaticMethods.logWarningFormat("Analytics - Unable to determine connectivity status due to there being no default network currently active", new Object[0]);
                            z4 = false;
                        } catch (NullPointerException e) {
                            e = e;
                            z = false;
                            StaticMethods.logWarningFormat("Analytics - Unable to determine connectivity status due to an unexpected error (%s)", e.getLocalizedMessage());
                            return z;
                        } catch (SecurityException e2) {
                            e = e2;
                            z2 = false;
                            StaticMethods.logErrorFormat("Analytics - Unable to access connectivity status due to a security error (%s)", e.getLocalizedMessage());
                            return z2;
                        } catch (Exception e3) {
                            e = e3;
                            z3 = false;
                            StaticMethods.logWarningFormat("Analytics - Unable to access connectivity status due to an unexpected error (%s)", e.getLocalizedMessage());
                            return z3;
                        }
                    } else if (!activeNetworkInfo.isAvailable() || !activeNetworkInfo.isConnected()) {
                        z4 = false;
                    }
                    return z4;
                }
                StaticMethods.logWarningFormat("Analytics - Unable to determine connectivity status due to the system service requested being unrecognized", new Object[0]);
            } catch (NullPointerException e4) {
                e = e4;
                z = true;
            } catch (SecurityException e5) {
                e = e5;
                z2 = true;
                StaticMethods.logErrorFormat("Analytics - Unable to access connectivity status due to a security error (%s)", e.getLocalizedMessage());
                return z2;
            } catch (Exception e6) {
                e = e6;
                z3 = true;
                StaticMethods.logWarningFormat("Analytics - Unable to access connectivity status due to an unexpected error (%s)", e.getLocalizedMessage());
                return z3;
            }
        }
        return true;
    }
}
