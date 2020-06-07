package com.adobe.mobile;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.adobe.mobile.StaticMethods;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;

public class MessageNotificationHandler extends BroadcastReceiver {
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v18, resolved type: android.app.Notification} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v20, resolved type: android.app.Notification} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v21, resolved type: android.app.Notification} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v24, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v27, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v31, resolved type: android.app.Notification} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r14v32, resolved type: android.app.Notification} */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onReceive(Context context, Intent intent) {
        Notification notification;
        Bundle extras = intent.getExtras();
        if (extras == null) {
            StaticMethods.logDebugFormat("Messages - unable to load extras from local notification intent", new Object[0]);
            return;
        }
        try {
            Object string = extras.getString("alarm_message");
            int i = extras.getInt("adbMessageCode");
            int i2 = extras.getInt("requestCode");
            String string2 = extras.getString("adb_m_l_id");
            String string3 = extras.getString("adb_deeplink");
            String string4 = extras.getString("userData");
            if (i == Messages.MESSAGE_LOCAL_IDENTIFIER.intValue()) {
                if (string == null) {
                    StaticMethods.logDebugFormat("Messages - local notification message was empty ", new Object[0]);
                    return;
                }
                try {
                    Activity currentActivity = StaticMethods.getCurrentActivity();
                    try {
                        Context sharedContext = StaticMethods.getSharedContext();
                        Activity activity = null;
                        try {
                            activity = StaticMethods.getCurrentActivity();
                        } catch (StaticMethods.NullActivityException unused) {
                            StaticMethods.logErrorFormat("Messages - unable to find activity for your notification, using default", new Object[0]);
                        }
                        if (string3 != null && !string3.isEmpty()) {
                            intent = new Intent("android.intent.action.VIEW");
                            intent.setData(Uri.parse(string3));
                        } else if (activity != null) {
                            intent = activity.getIntent();
                        }
                        intent.setFlags(603979776);
                        intent.putExtra("adb_m_l_id", string2);
                        intent.putExtra("userData", string4);
                        int i3 = Build.VERSION.SDK_INT;
                        try {
                            Object activity2 = PendingIntent.getActivity(sharedContext, i2, intent, 134217728);
                            if (activity2 == null) {
                                StaticMethods.logDebugFormat("Messages - could not retrieve sender from broadcast, unable to post notification", new Object[0]);
                                return;
                            }
                            if (i3 >= 11) {
                                Class<?> loadClass = BroadcastReceiver.class.getClassLoader().loadClass("android.app.Notification$Builder");
                                Constructor<?> constructor = loadClass.getConstructor(new Class[]{Context.class});
                                constructor.setAccessible(true);
                                Object newInstance = constructor.newInstance(new Object[]{StaticMethods.getSharedContext()});
                                loadClass.getDeclaredMethod("setSmallIcon", new Class[]{Integer.TYPE}).invoke(newInstance, new Object[]{Integer.valueOf(getSmallIcon())});
                                loadClass.getDeclaredMethod("setLargeIcon", new Class[]{Bitmap.class}).invoke(newInstance, new Object[]{getLargeIcon()});
                                loadClass.getDeclaredMethod("setContentTitle", new Class[]{CharSequence.class}).invoke(newInstance, new Object[]{getAppName()});
                                loadClass.getDeclaredMethod("setContentText", new Class[]{CharSequence.class}).invoke(newInstance, new Object[]{string});
                                loadClass.getDeclaredMethod("setContentIntent", new Class[]{PendingIntent.class}).invoke(newInstance, new Object[]{activity2});
                                loadClass.getDeclaredMethod("setAutoCancel", new Class[]{Boolean.TYPE}).invoke(newInstance, new Object[]{true});
                                if (i3 >= 16) {
                                    notification = loadClass.getDeclaredMethod("build", new Class[0]).invoke(newInstance, new Object[0]);
                                } else {
                                    notification = loadClass.getDeclaredMethod("getNotification", new Class[0]).invoke(newInstance, new Object[0]);
                                }
                                if (notification == null) {
                                    return;
                                }
                            } else {
                                notification = new Notification();
                                Notification.class.getDeclaredMethod("setLatestEventInfo", new Class[]{Context.class, String.class, String.class, PendingIntent.class}).invoke(notification, new Object[]{sharedContext, getAppName(), string, activity2});
                                Notification.class.getField("icon").set(notification, Integer.valueOf(getSmallIcon()));
                                notification.flags = 16;
                            }
                            ((NotificationManager) currentActivity.getSystemService("notification")).notify(new SecureRandom().nextInt(), notification);
                        } catch (ClassNotFoundException e) {
                            StaticMethods.logErrorFormat("Messages - error posting notification, class not found (%s)", e.getMessage());
                        } catch (NoSuchMethodException e2) {
                            StaticMethods.logErrorFormat("Messages - error posting notification, method not found (%s)", e2.getMessage());
                        } catch (StaticMethods.NullContextException e3) {
                            StaticMethods.logErrorFormat("Messages - error posting notification (%s)", e3.getMessage());
                        } catch (Exception e4) {
                            StaticMethods.logErrorFormat("Messages - unexpected error posting notification (%s)", e4.getMessage());
                        }
                    } catch (StaticMethods.NullContextException e5) {
                        StaticMethods.logErrorFormat(e5.getMessage(), new Object[0]);
                    }
                } catch (StaticMethods.NullActivityException e6) {
                    StaticMethods.logErrorFormat(e6.getMessage(), new Object[0]);
                }
            }
        } catch (Exception e7) {
            StaticMethods.logDebugFormat("Messages - unable to load message from local notification (%s)", e7.getMessage());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x000e, code lost:
        r4 = r3.getApplicationInfo(com.adobe.mobile.StaticMethods.getSharedContext().getPackageName(), 0);
     */
    private String getAppName() {
        ApplicationInfo applicationInfo;
        try {
            PackageManager packageManager = StaticMethods.getSharedContext().getPackageManager();
            return (packageManager == null || applicationInfo == null || packageManager.getApplicationLabel(applicationInfo) == null) ? "" : (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            StaticMethods.logDebugFormat("Messages - unable to retrieve app name for local notification (%s)", e.getMessage());
            return "";
        } catch (StaticMethods.NullContextException e2) {
            StaticMethods.logDebugFormat("Messages - unable to get app name (%s)", e2.getMessage());
            return "";
        }
    }

    private int getSmallIcon() {
        if (Messages.getSmallIconResourceId() != -1) {
            return Messages.getSmallIconResourceId();
        }
        return 17301651;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x006b, code lost:
        r2 = com.adobe.mobile.StaticMethods.getSharedContext().getPackageManager();
     */
    private Bitmap getLargeIcon() throws ClassNotFoundException, NoSuchMethodException, StaticMethods.NullContextException, IllegalAccessException, InvocationTargetException {
        Drawable drawable;
        PackageManager packageManager;
        int largeIconResourceId = Messages.getLargeIconResourceId();
        if (largeIconResourceId != -1) {
            Context sharedContext = StaticMethods.getSharedContext();
            if (Build.VERSION.SDK_INT > 20) {
                drawable = (Drawable) Resources.class.getDeclaredMethod("getDrawable", new Class[]{Integer.TYPE, Resources.Theme.class}).invoke(sharedContext.getResources(), new Object[]{Integer.valueOf(largeIconResourceId), sharedContext.getTheme()});
            } else {
                drawable = (Drawable) Resources.class.getDeclaredMethod("getDrawable", new Class[]{Integer.TYPE}).invoke(sharedContext.getResources(), new Object[]{Integer.valueOf(largeIconResourceId)});
            }
        } else {
            ApplicationInfo applicationInfo = StaticMethods.getSharedContext().getApplicationInfo();
            drawable = (applicationInfo == null || packageManager == null) ? null : packageManager.getApplicationIcon(applicationInfo);
        }
        if (drawable != null) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return null;
    }
}
