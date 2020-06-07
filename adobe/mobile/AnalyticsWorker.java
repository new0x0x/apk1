package com.adobe.mobile;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Process;
import com.adobe.mobile.AbstractDatabaseBacking;
import com.adobe.mobile.AbstractHitDatabase;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

final class AnalyticsWorker extends AbstractHitDatabase {
    private static AnalyticsWorker _instance = null;
    private static final Object _instanceMutex = new Object();
    private static volatile boolean analyticsGetBaseURL_pred = true;
    private static String baseURL;
    /* access modifiers changed from: private */
    public static final SecureRandom randomGen = new SecureRandom();
    protected SQLiteStatement _preparedInsertStatement = null;

    public static AnalyticsWorker sharedInstance() {
        AnalyticsWorker analyticsWorker;
        synchronized (_instanceMutex) {
            if (_instance == null) {
                _instance = new AnalyticsWorker();
            }
            analyticsWorker = _instance;
        }
        return analyticsWorker;
    }

    protected AnalyticsWorker() {
        this.fileName = "ADBMobileDataCache.sqlite";
        this.logPrefix = "Analytics";
        this.dbCreateStatement = "CREATE TABLE IF NOT EXISTS HITS (ID INTEGER PRIMARY KEY AUTOINCREMENT, URL TEXT, TIMESTAMP INTEGER)";
        this.lastHitTimestamp = 0;
        initDatabaseBacking(new File(StaticMethods.getCacheDirectory(), this.fileName));
        this.numberOfUnsentHits = getTrackingQueueSize();
    }

    /* access modifiers changed from: protected */
    public void queue(String str, long j) {
        MobileConfig instance = MobileConfig.getInstance();
        if (instance == null) {
            StaticMethods.logErrorFormat("Analytics - Cannot send hit, MobileConfig is null (this really shouldn't happen)", new Object[0]);
        } else if (MobileConfig.getInstance().mobileUsingAnalytics()) {
            if (instance.getPrivacyStatus() == MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_OUT) {
                StaticMethods.logDebugFormat("Analytics - Ignoring hit due to privacy status being opted out", new Object[0]);
            } else if (this.databaseStatus == AbstractDatabaseBacking.DatabaseStatus.FATALERROR) {
                StaticMethods.logErrorFormat("Analytics - Ignoring hit due to database error", new Object[0]);
            } else {
                synchronized (this.dbMutex) {
                    try {
                        this._preparedInsertStatement.bindString(1, str);
                        this._preparedInsertStatement.bindLong(2, j);
                        this._preparedInsertStatement.execute();
                        StaticMethods.updateLastKnownTimestamp(Long.valueOf(j));
                        this.numberOfUnsentHits++;
                        this._preparedInsertStatement.clearBindings();
                    } catch (SQLException e) {
                        StaticMethods.logErrorFormat("Analytics - Unable to insert url (%s)", str);
                        resetDatabase(e);
                    } catch (Exception e2) {
                        StaticMethods.logErrorFormat("Analytics - Unknown error while inserting url (%s)", str);
                        resetDatabase(e2);
                    }
                }
                kick(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public static String getBaseURL() {
        if (analyticsGetBaseURL_pred) {
            analyticsGetBaseURL_pred = false;
            StringBuilder sb = new StringBuilder();
            sb.append(MobileConfig.getInstance().getSSL() ? "https://" : "http://");
            sb.append(MobileConfig.getInstance().getTrackingServer());
            sb.append("/b/ss/");
            sb.append(StaticMethods.URLEncode(MobileConfig.getInstance().getReportSuiteIds()));
            sb.append("/");
            sb.append(MobileConfig.getInstance().getAnalyticsResponseType());
            sb.append("/JAVA-");
            sb.append("4.14.0-AN");
            sb.append("/s");
            baseURL = sb.toString();
            StaticMethods.logDebugFormat("Analytics - Setting base request URL(%s)", baseURL);
        }
        return baseURL;
    }

    /* access modifiers changed from: protected */
    public void preMigrate() {
        File file = new File(StaticMethods.getCacheDirectory() + this.fileName);
        File file2 = new File(StaticMethods.getCacheDirectory(), this.fileName);
        if (file.exists() && !file2.exists()) {
            try {
                if (!file.renameTo(file2)) {
                    StaticMethods.logWarningFormat("Analytics - Unable to migrate old hits db, creating new hits db (move file returned false)", new Object[0]);
                }
            } catch (Exception e) {
                StaticMethods.logWarningFormat("Analytics - Unable to migrate old hits db, creating new hits db (%s)", e.getLocalizedMessage());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void prepareStatements() {
        try {
            this._preparedInsertStatement = this.database.compileStatement("INSERT INTO HITS (URL, TIMESTAMP) VALUES (?, ?)");
        } catch (NullPointerException e) {
            StaticMethods.logErrorFormat("Analytics - Unable to create database due to an invalid path (%s)", e.getLocalizedMessage());
        } catch (SQLException e2) {
            StaticMethods.logErrorFormat("Analytics - Unable to create database due to a sql error (%s)", e2.getLocalizedMessage());
        } catch (Exception e3) {
            StaticMethods.logErrorFormat("Analytics - Unable to create database due to an unexpected error (%s)", e3.getLocalizedMessage());
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v0, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: com.adobe.mobile.AbstractHitDatabase$Hit} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v3, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v8, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v7, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v10, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v8, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v11, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v12, resolved type: com.adobe.mobile.AbstractHitDatabase$Hit} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v13, resolved type: com.adobe.mobile.AbstractHitDatabase$Hit} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v13, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v15, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v18, resolved type: android.database.Cursor} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v14, resolved type: com.adobe.mobile.AbstractHitDatabase$Hit} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v15, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v16, resolved type: com.adobe.mobile.AbstractHitDatabase$Hit} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v17, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v18, resolved type: com.adobe.mobile.AbstractHitDatabase$Hit} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v19, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v20, resolved type: java.lang.Object} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0045, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0046, code lost:
        r14 = r4;
        r4 = r1;
        r1 = r14;
        r5 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004a, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004b, code lost:
        r14 = r4;
        r4 = r1;
        r1 = r14;
        r5 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0056, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0057, code lost:
        r2 = r1;
        r1 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0077, code lost:
        if (r1 == null) goto L_0x008f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        r1.close();
        r5 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x008c, code lost:
        if (r1 == null) goto L_0x008f;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0056 A[ExcHandler: all (r1v9 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:5:0x0026] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0093  */
    public AbstractHitDatabase.Hit selectOldestHit() {
        AbstractHitDatabase.Hit hit;
        Object obj;
        Object obj2;
        Object obj3;
        synchronized (this.dbMutex) {
            Cursor cursor = null;
            try {
                Cursor query = this.database.query("HITS", new String[]{"ID", "URL", "TIMESTAMP"}, (String) null, (String[]) null, (String) null, (String) null, "ID ASC", "1");
                try {
                    if (query.moveToFirst()) {
                        AbstractHitDatabase.Hit hit2 = new AbstractHitDatabase.Hit();
                        hit2.identifier = query.getString(0);
                        hit2.urlFragment = query.getString(1);
                        hit2.timestamp = query.getLong(2);
                        cursor = hit2;
                    }
                    if (query != null) {
                        query.close();
                    }
                    hit = cursor;
                } catch (SQLException e) {
                    SQLException sQLException = e;
                    obj = null;
                    cursor = query;
                    e = sQLException;
                    StaticMethods.logErrorFormat("Analytics - Unable to read from database (%s)", e.getMessage());
                    hit = obj;
                    obj2 = obj;
                } catch (Exception e2) {
                    Exception exc = e2;
                    obj3 = null;
                    cursor = query;
                    e = exc;
                    try {
                        StaticMethods.logErrorFormat("Analytics - Unknown error reading from database (%s)", e.getMessage());
                        obj2 = obj3;
                        hit = obj3;
                    } catch (Throwable th) {
                        th = th;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                }
            } catch (SQLException e3) {
                e = e3;
                obj = null;
                StaticMethods.logErrorFormat("Analytics - Unable to read from database (%s)", e.getMessage());
                hit = obj;
                obj2 = obj;
            } catch (Exception e4) {
                e = e4;
                obj3 = null;
                StaticMethods.logErrorFormat("Analytics - Unknown error reading from database (%s)", e.getMessage());
                obj2 = obj3;
                hit = obj3;
            }
        }
        return hit;
    }

    /* access modifiers changed from: protected */
    public final Runnable workerThread() {
        return new Runnable() {
            public void run() {
                AbstractHitDatabase.Hit selectOldestHit;
                AnalyticsWorker sharedInstance = AnalyticsWorker.sharedInstance();
                Process.setThreadPriority(10);
                HashMap hashMap = new HashMap();
                hashMap.put("Accept-Language", StaticMethods.getDefaultAcceptLanguage());
                hashMap.put("User-Agent", StaticMethods.getDefaultUserAgent());
                while (MobileConfig.getInstance().getPrivacyStatus() == MobilePrivacyStatus.MOBILE_PRIVACY_STATUS_OPT_IN && MobileConfig.getInstance().networkConnectivity() && sharedInstance.databaseStatus == AbstractDatabaseBacking.DatabaseStatus.OK && (selectOldestHit = sharedInstance.selectOldestHit()) != null) {
                    if (MobileConfig.getInstance().getOfflineTrackingEnabled()) {
                        if (selectOldestHit.timestamp - sharedInstance.lastHitTimestamp < 0) {
                            long j = sharedInstance.lastHitTimestamp + 1;
                            selectOldestHit.urlFragment = selectOldestHit.urlFragment.replaceFirst("&ts=" + Long.toString(selectOldestHit.timestamp), "&ts=" + Long.toString(j));
                            StaticMethods.logDebugFormat("Analytics - Adjusting out of order hit timestamp(%d->%d)", Long.valueOf(selectOldestHit.timestamp), Long.valueOf(j));
                            selectOldestHit.timestamp = j;
                        }
                    } else if (selectOldestHit.timestamp < StaticMethods.getTimeSince1970() - 60) {
                        try {
                            sharedInstance.deleteHit(selectOldestHit.identifier);
                        } catch (AbstractDatabaseBacking.CorruptedDatabaseException e) {
                            AnalyticsWorker.sharedInstance().resetDatabase(e);
                        }
                    }
                    byte[] retrieveAnalyticsRequestData = RequestHandler.retrieveAnalyticsRequestData(AnalyticsWorker.getBaseURL() + AnalyticsWorker.randomGen.nextInt(100000000), selectOldestHit.urlFragment.startsWith("ndh") ? selectOldestHit.urlFragment : selectOldestHit.urlFragment.substring(selectOldestHit.urlFragment.indexOf(63) + 1), hashMap, 5000, AnalyticsWorker.this.logPrefix);
                    if (retrieveAnalyticsRequestData == null) {
                        int i = 0;
                        while (((long) i) < 30) {
                            try {
                                if (!MobileConfig.getInstance().networkConnectivity()) {
                                    break;
                                }
                                Thread.sleep(1000);
                                i++;
                            } catch (Exception e2) {
                                StaticMethods.logWarningFormat("Analytics - Background Thread Interrupted(%s)", e2.getMessage());
                            }
                        }
                    } else if (retrieveAnalyticsRequestData.length > 1) {
                        try {
                            sharedInstance.deleteHit(selectOldestHit.identifier);
                            sharedInstance.lastHitTimestamp = selectOldestHit.timestamp;
                            final JSONObject jSONObject = new JSONObject(new String(retrieveAnalyticsRequestData, "UTF-8"));
                            StaticMethods.getAudienceExecutor().execute(new Runnable() {
                                public void run() {
                                    AudienceManagerWorker.processJsonResponse(jSONObject);
                                }
                            });
                        } catch (AbstractDatabaseBacking.CorruptedDatabaseException e3) {
                            AnalyticsWorker.sharedInstance().resetDatabase(e3);
                        } catch (UnsupportedEncodingException e4) {
                            StaticMethods.logWarningFormat("Audience Manager - Unable to decode server response (%s)", e4.getLocalizedMessage());
                        } catch (JSONException e5) {
                            StaticMethods.logWarningFormat("Audience Manager - Unable to parse JSON data (%s)", e5.getLocalizedMessage());
                        }
                    } else {
                        try {
                            sharedInstance.deleteHit(selectOldestHit.identifier);
                            sharedInstance.lastHitTimestamp = selectOldestHit.timestamp;
                        } catch (AbstractDatabaseBacking.CorruptedDatabaseException e6) {
                            AnalyticsWorker.sharedInstance().resetDatabase(e6);
                        }
                    }
                }
                sharedInstance.bgThreadActive = false;
            }
        };
    }

    /* access modifiers changed from: protected */
    public void kickWithReferrerData(Map<String, Object> map) {
        if (map == null || map.size() <= 0) {
            ReferrerHandler.setReferrerProcessed(true);
            kick(false);
            return;
        }
        AbstractHitDatabase.Hit selectOldestHit = selectOldestHit();
        if (!(selectOldestHit == null || selectOldestHit.urlFragment == null)) {
            selectOldestHit.urlFragment = StaticMethods.appendContextData(map, selectOldestHit.urlFragment);
            updateHitInDatabase(selectOldestHit);
            ReferrerHandler.setReferrerProcessed(true);
        }
        kick(false);
    }

    /* access modifiers changed from: protected */
    public void updateHitInDatabase(AbstractHitDatabase.Hit hit) {
        synchronized (this.dbMutex) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("URL", hit.urlFragment);
                SQLiteDatabase sQLiteDatabase = this.database;
                sQLiteDatabase.update("HITS", contentValues, "id=" + hit.identifier, (String[]) null);
            } catch (SQLException e) {
                StaticMethods.logErrorFormat("Analytics - Unable to update url in database (%s)", e.getMessage());
            } catch (Exception e2) {
                StaticMethods.logErrorFormat("Analytics - Unknown error updating url in database (%s)", e2.getMessage());
            }
        }
    }
}
