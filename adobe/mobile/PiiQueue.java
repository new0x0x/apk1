package com.adobe.mobile;

final class PiiQueue extends ThirdPartyQueue {
    private static PiiQueue _instance;
    private static final Object _instanceMutex = new Object();

    /* access modifiers changed from: protected */
    public String fileName() {
        return "ADBMobilePIICache.sqlite";
    }

    /* access modifiers changed from: protected */
    public String logPrefix() {
        return "PII";
    }

    protected PiiQueue() {
    }

    protected static PiiQueue sharedInstance() {
        PiiQueue piiQueue;
        synchronized (_instanceMutex) {
            if (_instance == null) {
                _instance = new PiiQueue();
            }
            piiQueue = _instance;
        }
        return piiQueue;
    }

    /* access modifiers changed from: protected */
    public ThirdPartyQueue getWorker() {
        return sharedInstance();
    }
}
