package com.adobe.mobile;

import android.database.SQLException;
import android.database.sqlite.SQLiteStatement;
import java.io.File;

final class AnalyticsTrackTimedAction extends AbstractDatabaseBacking {
    private static AnalyticsTrackTimedAction _instance;
    private static final Object _instanceMutex = new Object();
    private SQLiteStatement sqlDeleteAction;
    private SQLiteStatement sqlDeleteContextDataForAction;
    private String sqlExistsAction;
    private String sqlExistsContextDataByActionAndKey;
    private SQLiteStatement sqlInsertAction;
    private SQLiteStatement sqlInsertContextData;
    private String sqlSelectAction;
    private String sqlSelectContextDataForAction;
    private SQLiteStatement sqlUpdateAction;
    private SQLiteStatement sqlUpdateActionsClearAdjustedTime;
    private SQLiteStatement sqlUpdateContextData;

    /* access modifiers changed from: protected */
    public void postMigrate() {
    }

    /* access modifiers changed from: protected */
    public void postReset() {
    }

    public static AnalyticsTrackTimedAction sharedInstance() {
        AnalyticsTrackTimedAction analyticsTrackTimedAction;
        synchronized (_instanceMutex) {
            if (_instance == null) {
                _instance = new AnalyticsTrackTimedAction();
            }
            analyticsTrackTimedAction = _instance;
        }
        return analyticsTrackTimedAction;
    }

    private AnalyticsTrackTimedAction() {
        this.fileName = "ADBMobileTimedActionsCache.sqlite";
        this.logPrefix = "Analytics";
        initDatabaseBacking(new File(StaticMethods.getCacheDirectory(), this.fileName));
    }

    /* access modifiers changed from: protected */
    public void trackTimedActionUpdateAdjustedStartTime(long j) {
        synchronized (this.dbMutex) {
            try {
                this.sqlUpdateAction.bindLong(1, j);
                this.sqlUpdateAction.execute();
                this.sqlUpdateAction.clearBindings();
            } catch (SQLException e) {
                StaticMethods.logErrorFormat("%s - Unable to bind prepared statement values for updating the adjusted start time for timed action (%s)", this.logPrefix, e.getLocalizedMessage());
                resetDatabase(e);
            } catch (Exception e2) {
                StaticMethods.logErrorFormat("%s - Unable to adjust start times for timed actions (%s)", this.logPrefix, e2.getMessage());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void trackTimedActionUpdateActionsClearAdjustedStartTime() {
        synchronized (this.dbMutex) {
            try {
                this.sqlUpdateActionsClearAdjustedTime.execute();
                this.sqlUpdateActionsClearAdjustedTime.clearBindings();
            } catch (SQLException e) {
                StaticMethods.logErrorFormat("%s - Unable to update adjusted time for timed actions after crash (%s)", this.logPrefix, e.getMessage());
                resetDatabase(e);
            } catch (Exception e2) {
                StaticMethods.logErrorFormat("%s - Unknown error clearing adjusted start times for timed actions (%s)", this.logPrefix, e2.getMessage());
                resetDatabase(e2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void preMigrate() {
        File file = new File(StaticMethods.getCacheDirectory() + "ADBMobileDataCache.sqlite" + this.fileName);
        File file2 = new File(StaticMethods.getCacheDirectory(), this.fileName);
        if (file.exists() && !file2.exists()) {
            try {
                if (!file.renameTo(file2)) {
                    StaticMethods.logWarningFormat("%s - Unable to migrate old Timed Actions db, creating new Timed Actions db (move file returned false)", this.logPrefix);
                }
            } catch (Exception e) {
                StaticMethods.logWarningFormat("%s - Unable to migrate old Timed Actions db, creating new Timed Actions db (%s)", this.logPrefix, e.getLocalizedMessage());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initializeDatabase() {
        try {
            this.database.execSQL("CREATE TABLE IF NOT EXISTS TIMEDACTIONS (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, STARTTIME INTEGER, ADJSTARTTIME INTEGER)");
            this.database.execSQL("CREATE TABLE IF NOT EXISTS CONTEXTDATA (ID INTEGER PRIMARY KEY AUTOINCREMENT, ACTIONID INTEGER, KEY TEXT, VALUE TEXT, FOREIGN KEY(ACTIONID) REFERENCES TIMEDACTIONS(ID))");
        } catch (SQLException e) {
            StaticMethods.logErrorFormat("%s - Unable to open or create timed actions database (%s)", this.logPrefix, e.getMessage());
        } catch (Exception e2) {
            StaticMethods.logErrorFormat("%s - Uknown error creating timed actions database (%s)", this.logPrefix, e2.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void prepareStatements() {
        this.sqlSelectAction = "SELECT ID, STARTTIME, ADJSTARTTIME FROM TIMEDACTIONS WHERE NAME=?";
        this.sqlExistsAction = "SELECT COUNT(*) FROM TIMEDACTIONS WHERE NAME=?";
        this.sqlSelectContextDataForAction = "SELECT KEY, VALUE FROM CONTEXTDATA WHERE ACTIONID=?";
        this.sqlExistsContextDataByActionAndKey = "SELECT COUNT(*) FROM CONTEXTDATA WHERE ACTIONID=? AND KEY=?";
        try {
            this.sqlInsertAction = this.database.compileStatement("INSERT INTO TIMEDACTIONS (NAME, STARTTIME, ADJSTARTTIME) VALUES (@NAME, @START, @START)");
            this.sqlUpdateAction = this.database.compileStatement("UPDATE TIMEDACTIONS SET ADJSTARTTIME=ADJSTARTTIME+@DELTA WHERE ADJSTARTTIME!=0");
            this.sqlUpdateActionsClearAdjustedTime = this.database.compileStatement("UPDATE TIMEDACTIONS SET ADJSTARTTIME=0");
            this.sqlDeleteAction = this.database.compileStatement("DELETE FROM TIMEDACTIONS WHERE ID=@ID");
            this.sqlInsertContextData = this.database.compileStatement("INSERT INTO CONTEXTDATA(ACTIONID, KEY, VALUE) VALUES (@ACTIONID, @KEY, @VALUE)");
            this.sqlUpdateContextData = this.database.compileStatement("UPDATE CONTEXTDATA SET VALUE=@VALUE WHERE ACTIONID=@ACTIONID AND KEY=@KEY");
            this.sqlDeleteContextDataForAction = this.database.compileStatement("DELETE FROM CONTEXTDATA WHERE ACTIONID=@ACTIONID");
        } catch (SQLException e) {
            StaticMethods.logErrorFormat("Analytics - unable to prepare the needed SQL statements for interacting with the timed actions database (%s)", e.getMessage());
        } catch (Exception e2) {
            StaticMethods.logErrorFormat("Analytics - Unknown error preparing sql statements (%s)", e2.getMessage());
        }
    }
}
