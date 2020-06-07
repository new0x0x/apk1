package com.adobe.mobile;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteStatement;
import android.os.Process;
import com.adobe.mobile.AbstractDatabaseBacking;
import com.adobe.mobile.AbstractHitDatabase;
import java.io.File;
import java.util.HashMap;

class ThirdPartyQueue extends AbstractHitDatabase {
    private static final String[] _hitsSelectedColumns = {"ID", "URL", "POSTBODY", "POSTTYPE", "TIMESTAMP", "TIMEOUT"};
    private static ThirdPartyQueue _instance = null;
    private static final Object _instanceMutex = new Object();
    private SQLiteStatement _preparedInsertStatement = null;

    /* access modifiers changed from: protected */
    public String fileName() {
        return "ADBMobile3rdPartyDataCache.sqlite";
    }

    /* access modifiers changed from: protected */
    public String logPrefix() {
        return "External Callback";
    }

    protected static ThirdPartyQueue sharedInstance() {
        ThirdPartyQueue thirdPartyQueue;
        synchronized (_instanceMutex) {
            if (_instance == null) {
                _instance = new ThirdPartyQueue();
            }
            thirdPartyQueue = _instance;
        }
        return thirdPartyQueue;
    }

    protected ThirdPartyQueue() {
        this.fileName = fileName();
        this.logPrefix = logPrefix();
        this.dbCreateStatement = "CREATE TABLE IF NOT EXISTS HITS (ID INTEGER PRIMARY KEY AUTOINCREMENT, URL TEXT, POSTBODY TEXT, POSTTYPE TEXT, TIMESTAMP INTEGER, TIMEOUT INTEGER)";
        this.lastHitTimestamp = 0;
        initDatabaseBacking(new File(StaticMethods.getCacheDirectory(), this.fileName));
        this.numberOfUnsentHits = getTrackingQueueSize();
    }

    /* access modifiers changed from: protected */
    public void queue(String str, String str2, String str3, long j, long j2) {
        MobileConfig instance = MobileConfig.getInstance();
        if (instance == null) {
            StaticMethods.logErrorFormat("%s - Cannot send hit, MobileConfig is null (this really shouldn't happen)", this.logPrefix);
        } else if (instance.getPrivacyStatus() == MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_OUT) {
            StaticMethods.logDebugFormat("%s - Ignoring hit due to privacy status being opted out", this.logPrefix);
        } else {
            synchronized (this.dbMutex) {
                try {
                    this._preparedInsertStatement.bindString(1, str);
                    if (str2 == null || str2.length() <= 0) {
                        this._preparedInsertStatement.bindNull(2);
                    } else {
                        this._preparedInsertStatement.bindString(2, str2);
                    }
                    if (str3 == null || str3.length() <= 0) {
                        this._preparedInsertStatement.bindNull(3);
                    } else {
                        this._preparedInsertStatement.bindString(3, str3);
                    }
                    this._preparedInsertStatement.bindLong(4, j);
                    this._preparedInsertStatement.bindLong(5, j2);
                    this._preparedInsertStatement.execute();
                    this.numberOfUnsentHits++;
                    this._preparedInsertStatement.clearBindings();
                } catch (SQLException e) {
                    StaticMethods.logErrorFormat("%s - Unable to insert url (%s)", this.logPrefix, str);
                    resetDatabase(e);
                } catch (Exception e2) {
                    StaticMethods.logErrorFormat("%s - Unknown error while inserting url (%s)", this.logPrefix, str);
                    resetDatabase(e2);
                }
            }
            kick(false);
        }
    }

    /* access modifiers changed from: protected */
    public void prepareStatements() {
        try {
            this._preparedInsertStatement = this.database.compileStatement("INSERT INTO HITS (URL, POSTBODY, POSTTYPE, TIMESTAMP, TIMEOUT) VALUES (?, ?, ?, ?, ?)");
        } catch (NullPointerException e) {
            StaticMethods.logErrorFormat("%s - Unable to create database due to an invalid path (%s)", this.logPrefix, e.getLocalizedMessage());
        } catch (SQLException e2) {
            StaticMethods.logErrorFormat("%s - Unable to create database due to a sql error (%s)", this.logPrefix, e2.getLocalizedMessage());
        } catch (Exception e3) {
            StaticMethods.logErrorFormat("%s - Unable to create database due to an unexpected error (%s)", this.logPrefix, e3.getLocalizedMessage());
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007b, code lost:
        if (r5 == null) goto L_0x0099;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0096, code lost:
        if (r5 == null) goto L_0x0099;
     */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x009e A[Catch:{ all -> 0x009b }] */
    public AbstractHitDatabase.Hit selectOldestHit() {
        Cursor cursor;
        AbstractHitDatabase.Hit hit;
        SQLException e;
        Exception e2;
        synchronized (this.dbMutex) {
            AbstractHitDatabase.Hit hit2 = null;
            try {
                cursor = this.database.query("HITS", _hitsSelectedColumns, (String) null, (String[]) null, (String) null, (String) null, "ID ASC", "1");
                try {
                    if (cursor.moveToFirst()) {
                        hit = new AbstractHitDatabase.Hit();
                        try {
                            hit.identifier = cursor.getString(0);
                            hit.urlFragment = cursor.getString(1);
                            hit.postBody = cursor.getString(2);
                            hit.postType = cursor.getString(3);
                            hit.timestamp = cursor.getLong(4);
                            hit.timeout = cursor.getInt(5);
                            hit2 = hit;
                        } catch (SQLException e3) {
                            e = e3;
                            StaticMethods.logErrorFormat("%s - Unable to read from database (%s)", this.logPrefix, e.getMessage());
                        } catch (Exception e4) {
                            e2 = e4;
                            try {
                                StaticMethods.logErrorFormat("%s - Unknown error reading from database (%s)", this.logPrefix, e2.getMessage());
                            } catch (Throwable th) {
                                th = th;
                                if (cursor != null) {
                                }
                                throw th;
                            }
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    hit = hit2;
                } catch (SQLException e5) {
                    SQLException sQLException = e5;
                    hit = null;
                    e = sQLException;
                    StaticMethods.logErrorFormat("%s - Unable to read from database (%s)", this.logPrefix, e.getMessage());
                } catch (Exception e6) {
                    Exception exc = e6;
                    hit = null;
                    e2 = exc;
                    StaticMethods.logErrorFormat("%s - Unknown error reading from database (%s)", this.logPrefix, e2.getMessage());
                }
            } catch (SQLException e7) {
                hit = null;
                e = e7;
                cursor = null;
                StaticMethods.logErrorFormat("%s - Unable to read from database (%s)", this.logPrefix, e.getMessage());
            } catch (Exception e8) {
                hit = null;
                e2 = e8;
                cursor = null;
                StaticMethods.logErrorFormat("%s - Unknown error reading from database (%s)", this.logPrefix, e2.getMessage());
            } catch (Throwable th2) {
                cursor = null;
                th = th2;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
        return hit;
    }

    /* access modifiers changed from: protected */
    public ThirdPartyQueue getWorker() {
        return sharedInstance();
    }

    /* access modifiers changed from: protected */
    public Runnable workerThread() {
        final ThirdPartyQueue worker = getWorker();
        return new Runnable() {
            public void run() {
                AbstractHitDatabase.Hit selectOldestHit;
                Process.setThreadPriority(10);
                boolean offlineTrackingEnabled = MobileConfig.getInstance().getOfflineTrackingEnabled();
                HashMap hashMap = new HashMap();
                hashMap.put("Accept-Language", StaticMethods.getDefaultAcceptLanguage());
                hashMap.put("User-Agent", StaticMethods.getDefaultUserAgent());
                while (MobileConfig.getInstance().getPrivacyStatus() == MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_IN && MobileConfig.getInstance().networkConnectivity() && (selectOldestHit = worker.selectOldestHit()) != null && selectOldestHit.urlFragment != null) {
                    if (offlineTrackingEnabled || selectOldestHit.timestamp >= StaticMethods.getTimeSince1970() - 60) {
                        selectOldestHit.postBody = selectOldestHit.postBody != null ? selectOldestHit.postBody : "";
                        selectOldestHit.postType = selectOldestHit.postType != null ? selectOldestHit.postType : "";
                        selectOldestHit.timeout = selectOldestHit.timeout < 2 ? 2000 : selectOldestHit.timeout * 1000;
                        if (RequestHandler.sendThirdPartyRequest(selectOldestHit.urlFragment, selectOldestHit.postBody, hashMap, selectOldestHit.timeout, selectOldestHit.postType, ThirdPartyQueue.this.logPrefix)) {
                            try {
                                worker.deleteHit(selectOldestHit.identifier);
                                worker.lastHitTimestamp = selectOldestHit.timestamp;
                            } catch (AbstractDatabaseBacking.CorruptedDatabaseException e) {
                                worker.resetDatabase(e);
                            }
                        } else {
                            StaticMethods.logWarningFormat("%s - Unable to forward hit (%s)", ThirdPartyQueue.this.logPrefix, selectOldestHit.urlFragment);
                            if (MobileConfig.getInstance().getOfflineTrackingEnabled()) {
                                StaticMethods.logDebugFormat("%s - Network error, imposing internal cooldown (%d seconds)", ThirdPartyQueue.this.logPrefix, 30L);
                                int i = 0;
                                while (((long) i) < 30) {
                                    try {
                                        if (!MobileConfig.getInstance().networkConnectivity()) {
                                            break;
                                        }
                                        Thread.sleep(1000);
                                        i++;
                                    } catch (Exception e2) {
                                        StaticMethods.logWarningFormat("%s - Background Thread Interrupted (%s)", ThirdPartyQueue.this.logPrefix, e2.getMessage());
                                    }
                                }
                            } else {
                                try {
                                    worker.deleteHit(selectOldestHit.identifier);
                                } catch (AbstractDatabaseBacking.CorruptedDatabaseException e3) {
                                    worker.resetDatabase(e3);
                                }
                            }
                        }
                    } else {
                        try {
                            worker.deleteHit(selectOldestHit.identifier);
                        } catch (AbstractDatabaseBacking.CorruptedDatabaseException e4) {
                            worker.resetDatabase(e4);
                        }
                    }
                }
                worker.bgThreadActive = false;
            }
        };
    }
}
