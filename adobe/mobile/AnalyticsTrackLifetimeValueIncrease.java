package com.adobe.mobile;

import com.adobe.mobile.StaticMethods;
import java.math.BigDecimal;

final class AnalyticsTrackLifetimeValueIncrease {
    private static final Object _lifetimeValueMutex = new Object();

    AnalyticsTrackLifetimeValueIncrease() {
    }

    protected static BigDecimal getLifetimeValue() {
        BigDecimal bigDecimal;
        synchronized (_lifetimeValueMutex) {
            try {
                bigDecimal = new BigDecimal(StaticMethods.getSharedPreferences().getString("ADB_LIFETIME_VALUE", "0"));
            } catch (NumberFormatException unused) {
                bigDecimal = new BigDecimal("0");
            } catch (StaticMethods.NullContextException e) {
                StaticMethods.logErrorFormat("Analytics - Error getting current lifetime value:(%s).", e.getMessage());
                bigDecimal = null;
            }
        }
        return bigDecimal;
    }
}
