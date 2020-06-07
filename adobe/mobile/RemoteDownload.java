package com.adobe.mobile;

import com.appdynamics.eumagent.runtime.InstrumentationCallbacks;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

class RemoteDownload {

    protected interface RemoteDownloadBlock {
        void call(boolean z, File file);
    }

    RemoteDownload() {
    }

    protected static boolean stringIsUrl(String str) {
        if (str == null || str.length() <= 0) {
            return false;
        }
        try {
            new URL(str);
            return true;
        } catch (MalformedURLException unused) {
            return false;
        }
    }

    protected static void remoteDownloadAsync(String str, int i, int i2, RemoteDownloadBlock remoteDownloadBlock, String str2) {
        new Thread(new DownloadFileTask(str, remoteDownloadBlock, i, i2, str2)).start();
    }

    protected static void remoteDownloadAsync(String str, RemoteDownloadBlock remoteDownloadBlock) {
        remoteDownloadAsync(str, 10000, 10000, remoteDownloadBlock, "adbdownloadcache");
    }

    protected static void remoteDownloadSync(String str, int i, int i2, RemoteDownloadBlock remoteDownloadBlock, String str2) {
        new DownloadFileTask(str, remoteDownloadBlock, i, i2, str2).run();
    }

    protected static void remoteDownloadSync(String str, RemoteDownloadBlock remoteDownloadBlock) {
        remoteDownloadSync(str, 10000, 10000, remoteDownloadBlock, "adbdownloadcache");
    }

    protected static File getFileForCachedURL(String str) {
        return getFileForCachedURL(str, "adbdownloadcache");
    }

    protected static File getFileForCachedURL(String str, String str2) {
        File downloadCacheDirectory;
        if (str == null || str.length() < 1 || (downloadCacheDirectory = getDownloadCacheDirectory(str2)) == null) {
            return null;
        }
        String[] list = downloadCacheDirectory.list();
        if (list == null || list.length < 1) {
            StaticMethods.logDebugFormat("Cached Files - Directory is empty (%s).", downloadCacheDirectory.getAbsolutePath());
            return null;
        }
        String md5hash = md5hash(str);
        for (String str3 : list) {
            if (str3.substring(0, str3.lastIndexOf(46)).equals(md5hash)) {
                return new File(downloadCacheDirectory, str3);
            }
        }
        StaticMethods.logDebugFormat("Cached Files - This file has not previously been cached (%s).", str);
        return null;
    }

    protected static void deleteFilesForDirectoryNotInList(String str, List<String> list) {
        File[] listFiles;
        if (list == null || list.size() <= 0) {
            deleteFilesInDirectory(str);
            return;
        }
        File downloadCacheDirectory = getDownloadCacheDirectory(str);
        if (downloadCacheDirectory != null && (listFiles = downloadCacheDirectory.listFiles()) != null && listFiles.length > 0) {
            ArrayList arrayList = new ArrayList();
            for (String md5hash : list) {
                arrayList.add(md5hash(md5hash));
            }
            for (File file : listFiles) {
                String name = file.getName();
                if (!arrayList.contains(name.substring(0, name.indexOf(".")))) {
                    if (file.delete()) {
                        StaticMethods.logDebugFormat("Cached File - Removed unused cache file", new Object[0]);
                    } else {
                        StaticMethods.logWarningFormat("Cached File - Failed to remove unused cache file", new Object[0]);
                    }
                }
            }
        }
    }

    protected static void deleteFilesInDirectory(String str) {
        File[] listFiles;
        File downloadCacheDirectory = getDownloadCacheDirectory(str);
        if (downloadCacheDirectory != null && (listFiles = downloadCacheDirectory.listFiles()) != null && listFiles.length > 0) {
            for (File delete : listFiles) {
                if (delete.delete()) {
                    StaticMethods.logDebugFormat("Cached File - Removed unused cache file", new Object[0]);
                } else {
                    StaticMethods.logWarningFormat("Cached File - Failed to remove unused cache file", new Object[0]);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static File getNewCachedFile(String str, Date date, String str2, String str3) {
        String md5hash;
        if (str == null || str.length() < 1) {
            StaticMethods.logWarningFormat("Cached File - Invalid url parameter while attempting to create cache file. Could not save data.", new Object[0]);
            return null;
        } else if (date == null) {
            StaticMethods.logWarningFormat("Cached File - Invalid lastModified parameter while attempting to create cache file. Could not save data.", new Object[0]);
            return null;
        } else if (str2 == null || str2.length() < 1) {
            StaticMethods.logWarningFormat("Cached File - Invalid etag parameter while attempting to create cache file. Could not save data.", new Object[0]);
            return null;
        } else {
            File downloadCacheDirectory = getDownloadCacheDirectory(str3);
            if (downloadCacheDirectory == null || (md5hash = md5hash(str)) == null || md5hash.length() < 1) {
                return null;
            }
            return new File(downloadCacheDirectory.getPath() + File.separator + md5hash(str) + "." + date.getTime() + "_" + str2);
        }
    }

    protected static File getDownloadCacheDirectory(String str) {
        File file = new File(StaticMethods.getCacheDirectory(), str);
        if (file.exists() || file.mkdir()) {
            return file;
        }
        StaticMethods.logWarningFormat("Cached File - unable to open/make download cache directory", new Object[0]);
        return null;
    }

    protected static boolean deleteCachedDataForURL(String str, String str2) {
        if (str == null || str.length() < 1) {
            StaticMethods.logWarningFormat("Cached File - tried to delete cached file, but file path was empty", new Object[0]);
            return false;
        }
        File fileForCachedURL = getFileForCachedURL(str, str2);
        if (fileForCachedURL == null || !fileForCachedURL.delete()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static long getLastModifiedOfFile(String str) {
        if (str == null || str.length() < 1) {
            StaticMethods.logWarningFormat("Cached File - Path was null or empty for Cache File. Could not get Last Modified Date.", new Object[0]);
            return 0;
        }
        String[] splitPathExtension = splitPathExtension(getPathExtension(str));
        if (splitPathExtension != null && splitPathExtension.length >= 1) {
            return Long.parseLong(splitPathExtension[0]);
        }
        StaticMethods.logWarningFormat("Cached File - No last modified date for file. Extension had no values after split.", new Object[0]);
        return 0;
    }

    /* access modifiers changed from: private */
    public static String getEtagOfFile(String str) {
        if (str == null || str.length() < 1) {
            StaticMethods.logWarningFormat("Cached File - Path was null or empty for Cache File", new Object[0]);
            return null;
        }
        String[] splitPathExtension = splitPathExtension(getPathExtension(str));
        if (splitPathExtension != null && splitPathExtension.length >= 2) {
            return splitPathExtension[1];
        }
        StaticMethods.logWarningFormat("Cached File - No etag for file. Extension had no second value after split.", new Object[0]);
        return null;
    }

    private static String getPathExtension(String str) {
        if (str != null && str.length() >= 1) {
            return str.substring(str.lastIndexOf(".") + 1);
        }
        StaticMethods.logWarningFormat("Cached File - Path was null or empty for Cache File", new Object[0]);
        return null;
    }

    private static String[] splitPathExtension(String str) {
        if (str == null || str.length() < 1) {
            StaticMethods.logWarningFormat("Cached File - Extension was null or empty on Cache File", new Object[0]);
            return null;
        }
        String[] split = str.split("_");
        if (split.length == 2) {
            return split;
        }
        StaticMethods.logWarningFormat("Cached File - Invalid Extension on Cache File (%s)", str);
        return null;
    }

    /* access modifiers changed from: private */
    public static SimpleDateFormat createRFC2822Formatter() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat;
    }

    private static String md5hash(String str) {
        if (str == null || str.length() < 1) {
            return null;
        }
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(str.getBytes("UTF-8"));
            byte[] digest = instance.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                String hexString = Integer.toHexString(b & 255);
                while (hexString.length() < 2) {
                    hexString = "0" + hexString;
                }
                sb.append(hexString);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            StaticMethods.logErrorFormat("Cached Files - unable to get md5 hash (%s)", e.getMessage());
            return null;
        } catch (UnsupportedEncodingException e2) {
            StaticMethods.logErrorFormat("Cached Files - Unsupported Encoding: UTF-8 (%s)", e2.getMessage());
            return null;
        }
    }

    private static class DownloadFileTask implements Runnable {
        private final RemoteDownloadBlock callback;
        private final int connectionTimeout;
        private final String directory;
        private final int readTimeout;
        private final String url;

        private DownloadFileTask(String str, RemoteDownloadBlock remoteDownloadBlock, int i, int i2, String str2) {
            this.url = str;
            this.callback = remoteDownloadBlock;
            this.connectionTimeout = i;
            this.readTimeout = i2;
            this.directory = str2;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:142:0x0230, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:143:0x0231, code lost:
            r4 = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:144:0x0234, code lost:
            r0 = e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:145:0x0235, code lost:
            r4 = null;
            r6 = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:150:0x0248, code lost:
            r13.callback.call(false, (java.io.File) null);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:153:?, code lost:
            r6.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:154:0x0253, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:157:0x0257, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:159:0x025f, code lost:
            r1 = "Cached Files - Exception while attempting to close streams (%s)";
            r3 = new java.lang.Object[]{r0.getLocalizedMessage()};
         */
        /* JADX WARNING: Code restructure failed: missing block: B:160:0x026b, code lost:
            r0 = e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:161:0x026c, code lost:
            r4 = null;
            r6 = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:166:0x027f, code lost:
            r13.callback.call(false, (java.io.File) null);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:169:?, code lost:
            r6.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:170:0x028a, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:173:0x028e, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:175:0x0296, code lost:
            r1 = "Cached Files - Exception while attempting to close streams (%s)";
            r3 = new java.lang.Object[]{r0.getLocalizedMessage()};
         */
        /* JADX WARNING: Code restructure failed: missing block: B:192:0x02d7, code lost:
            r4 = null;
            r6 = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:199:0x02e8, code lost:
            r13.callback.call(false, (java.io.File) null);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:202:?, code lost:
            r6.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:203:0x02f3, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:206:0x02f7, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:208:0x02fe, code lost:
            r1 = "Cached Files - Exception while attempting to close streams (%s)";
            r3 = new java.lang.Object[]{r0.getLocalizedMessage()};
         */
        /* JADX WARNING: Code restructure failed: missing block: B:214:?, code lost:
            r1.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:215:0x0314, code lost:
            r1 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:218:0x0318, code lost:
            r4.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:220:0x031f, code lost:
            com.adobe.mobile.StaticMethods.logWarningFormat("Cached Files - Exception while attempting to close streams (%s)", r1.getLocalizedMessage());
         */
        /* JADX WARNING: Removed duplicated region for block: B:142:0x0230 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:22:0x0089] */
        /* JADX WARNING: Removed duplicated region for block: B:144:0x0234 A[ExcHandler: Error (e java.lang.Error), Splitter:B:22:0x0089] */
        /* JADX WARNING: Removed duplicated region for block: B:150:0x0248 A[Catch:{ all -> 0x030c }] */
        /* JADX WARNING: Removed duplicated region for block: B:152:0x024f A[SYNTHETIC, Splitter:B:152:0x024f] */
        /* JADX WARNING: Removed duplicated region for block: B:157:0x0257 A[Catch:{ IOException -> 0x0253 }] */
        /* JADX WARNING: Removed duplicated region for block: B:160:0x026b A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:22:0x0089] */
        /* JADX WARNING: Removed duplicated region for block: B:166:0x027f A[Catch:{ all -> 0x030c }] */
        /* JADX WARNING: Removed duplicated region for block: B:168:0x0286 A[SYNTHETIC, Splitter:B:168:0x0286] */
        /* JADX WARNING: Removed duplicated region for block: B:173:0x028e A[Catch:{ IOException -> 0x028a }] */
        /* JADX WARNING: Removed duplicated region for block: B:182:0x02b6 A[Catch:{ all -> 0x030c }] */
        /* JADX WARNING: Removed duplicated region for block: B:184:0x02bd A[SYNTHETIC, Splitter:B:184:0x02bd] */
        /* JADX WARNING: Removed duplicated region for block: B:189:0x02c5 A[Catch:{ IOException -> 0x02c1 }] */
        /* JADX WARNING: Removed duplicated region for block: B:193:? A[ExcHandler: SocketTimeoutException (unused java.net.SocketTimeoutException), SYNTHETIC, Splitter:B:22:0x0089] */
        /* JADX WARNING: Removed duplicated region for block: B:199:0x02e8 A[Catch:{ all -> 0x030c }] */
        /* JADX WARNING: Removed duplicated region for block: B:201:0x02ef A[SYNTHETIC, Splitter:B:201:0x02ef] */
        /* JADX WARNING: Removed duplicated region for block: B:206:0x02f7 A[Catch:{ IOException -> 0x02f3 }] */
        /* JADX WARNING: Removed duplicated region for block: B:213:0x0310 A[SYNTHETIC, Splitter:B:213:0x0310] */
        /* JADX WARNING: Removed duplicated region for block: B:218:0x0318 A[Catch:{ IOException -> 0x0314 }] */
        public void run() {
            InputStream inputStream;
            FileOutputStream fileOutputStream;
            Object[] objArr;
            String str;
            String str2 = this.url;
            FileOutputStream fileOutputStream2 = null;
            if (str2 == null) {
                StaticMethods.logDebugFormat("Cached Files - url is null and cannot be cached", new Object[0]);
                RemoteDownloadBlock remoteDownloadBlock = this.callback;
                if (remoteDownloadBlock != null) {
                    remoteDownloadBlock.call(false, (File) null);
                }
            } else if (!RemoteDownload.stringIsUrl(str2)) {
                StaticMethods.logDebugFormat("Cached Files - given url is not valid and cannot be cached (\"%s\")", this.url);
                RemoteDownloadBlock remoteDownloadBlock2 = this.callback;
                if (remoteDownloadBlock2 != null) {
                    remoteDownloadBlock2.call(false, (File) null);
                }
            } else {
                File fileForCachedURL = RemoteDownload.getFileForCachedURL(this.url, this.directory);
                SimpleDateFormat access$100 = RemoteDownload.createRFC2822Formatter();
                HttpURLConnection requestConnect = requestConnect(this.url);
                if (requestConnect == null) {
                    RemoteDownloadBlock remoteDownloadBlock3 = this.callback;
                    if (remoteDownloadBlock3 != null) {
                        remoteDownloadBlock3.call(false, (File) null);
                        return;
                    }
                    return;
                }
                requestConnect.setConnectTimeout(this.connectionTimeout);
                requestConnect.setReadTimeout(this.readTimeout);
                if (fileForCachedURL != null) {
                    String hexToString = StaticMethods.hexToString(RemoteDownload.getEtagOfFile(fileForCachedURL.getPath()));
                    Long valueOf = Long.valueOf(RemoteDownload.getLastModifiedOfFile(fileForCachedURL.getPath()));
                    if (valueOf.longValue() != 0) {
                        requestConnect.setRequestProperty("If-Modified-Since", access$100.format(valueOf));
                    }
                    if (hexToString != null) {
                        requestConnect.setRequestProperty("If-None-Match", hexToString);
                    }
                }
                try {
                    InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
                    requestConnect.connect();
                    InstrumentationCallbacks.requestSent(requestConnect);
                    InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
                    int responseCode = requestConnect.getResponseCode();
                    InstrumentationCallbacks.requestSent(requestConnect);
                    InstrumentationCallbacks.requestHarvestable(requestConnect);
                    if (responseCode == 304) {
                        StaticMethods.logDebugFormat("Cached Files - File has not been modified since last download. (%s)", this.url);
                        if (this.callback != null) {
                            this.callback.call(false, fileForCachedURL);
                        }
                        try {
                            requestConnect.disconnect();
                        } catch (IOException e) {
                            StaticMethods.logWarningFormat("Cached Files - Exception while attempting to close streams (%s)", e.getLocalizedMessage());
                        }
                    } else {
                        InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
                        int responseCode2 = requestConnect.getResponseCode();
                        InstrumentationCallbacks.requestSent(requestConnect);
                        InstrumentationCallbacks.requestHarvestable(requestConnect);
                        if (responseCode2 == 404) {
                            StaticMethods.logDebugFormat("Cached Files - File not found. (%s)", this.url);
                            if (this.callback != null) {
                                this.callback.call(false, fileForCachedURL);
                            }
                            try {
                                requestConnect.disconnect();
                            } catch (IOException e2) {
                                StaticMethods.logWarningFormat("Cached Files - Exception while attempting to close streams (%s)", e2.getLocalizedMessage());
                            }
                        } else {
                            InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
                            int responseCode3 = requestConnect.getResponseCode();
                            InstrumentationCallbacks.requestSent(requestConnect);
                            InstrumentationCallbacks.requestHarvestable(requestConnect);
                            if (responseCode3 != 200) {
                                Object[] objArr2 = new Object[3];
                                objArr2[0] = this.url;
                                InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
                                int responseCode4 = requestConnect.getResponseCode();
                                InstrumentationCallbacks.requestSent(requestConnect);
                                InstrumentationCallbacks.requestHarvestable(requestConnect);
                                objArr2[1] = Integer.valueOf(responseCode4);
                                InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
                                String responseMessage = requestConnect.getResponseMessage();
                                InstrumentationCallbacks.requestSent(requestConnect);
                                InstrumentationCallbacks.requestHarvestable(requestConnect);
                                objArr2[2] = responseMessage;
                                StaticMethods.logWarningFormat("Cached Files - File could not be downloaded from URL (%s) Response: (%d) Message: (%s)", objArr2);
                                if (this.callback != null) {
                                    this.callback.call(false, fileForCachedURL);
                                }
                                try {
                                    requestConnect.disconnect();
                                } catch (IOException e3) {
                                    StaticMethods.logWarningFormat("Cached Files - Exception while attempting to close streams (%s)", e3.getLocalizedMessage());
                                }
                            } else {
                                if (fileForCachedURL != null) {
                                    RemoteDownload.deleteCachedDataForURL(this.url, this.directory);
                                }
                                InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
                                long lastModified = requestConnect.getLastModified();
                                InstrumentationCallbacks.requestSent(requestConnect);
                                InstrumentationCallbacks.requestHarvestable(requestConnect);
                                Date date = new Date(lastModified);
                                InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
                                String headerField = requestConnect.getHeaderField("ETag");
                                InstrumentationCallbacks.requestSent(requestConnect);
                                InstrumentationCallbacks.requestHarvestable(requestConnect);
                                if (headerField != null) {
                                    headerField = StaticMethods.getHexString(headerField);
                                }
                                File access$400 = RemoteDownload.getNewCachedFile(this.url, date, headerField, this.directory);
                                if (access$400 == null) {
                                    if (this.callback != null) {
                                        this.callback.call(false, (File) null);
                                    }
                                    try {
                                        requestConnect.disconnect();
                                    } catch (IOException e4) {
                                        StaticMethods.logWarningFormat("Cached Files - Exception while attempting to close streams (%s)", e4.getLocalizedMessage());
                                    }
                                } else {
                                    inputStream = InstrumentationCallbacks.getInputStream(requestConnect);
                                    try {
                                        fileOutputStream = new FileOutputStream(access$400);
                                        try {
                                            byte[] bArr = new byte[4096];
                                            while (true) {
                                                int read = inputStream.read(bArr);
                                                if (read == -1) {
                                                    break;
                                                }
                                                fileOutputStream.write(bArr, 0, read);
                                            }
                                            StaticMethods.logDebugFormat("Cached Files - Caching successful (%s)", this.url);
                                            if (this.callback != null) {
                                                this.callback.call(true, access$400);
                                            }
                                            try {
                                                fileOutputStream.close();
                                                if (inputStream != null) {
                                                    inputStream.close();
                                                }
                                                requestConnect.disconnect();
                                            } catch (IOException e5) {
                                                str = "Cached Files - Exception while attempting to close streams (%s)";
                                                objArr = new Object[]{e5.getLocalizedMessage()};
                                                StaticMethods.logWarningFormat(str, objArr);
                                            }
                                        } catch (SocketTimeoutException unused) {
                                            StaticMethods.logWarningFormat("Cached Files - Timed out making request (%s)", this.url);
                                            if (this.callback != null) {
                                            }
                                            if (fileOutputStream != null) {
                                            }
                                            if (inputStream != null) {
                                            }
                                            requestConnect.disconnect();
                                        } catch (IOException e6) {
                                            e = e6;
                                            StaticMethods.logWarningFormat("Cached Files - IOException while making request (%s)", e.getLocalizedMessage());
                                            if (this.callback != null) {
                                            }
                                            if (fileOutputStream != null) {
                                            }
                                            if (inputStream != null) {
                                            }
                                            requestConnect.disconnect();
                                        } catch (Exception e7) {
                                            e = e7;
                                            StaticMethods.logWarningFormat("Cached Files - Unexpected exception while attempting to get remote file (%s)", e.getLocalizedMessage());
                                            if (this.callback != null) {
                                            }
                                            if (fileOutputStream != null) {
                                            }
                                            if (inputStream != null) {
                                            }
                                            requestConnect.disconnect();
                                        } catch (Error e8) {
                                            e = e8;
                                            try {
                                                StaticMethods.logWarningFormat("Cached Files - Unexpected error while attempting to get remote file (%s)", e.getLocalizedMessage());
                                                if (this.callback != null) {
                                                }
                                                if (fileOutputStream != null) {
                                                }
                                                if (inputStream != null) {
                                                }
                                                requestConnect.disconnect();
                                            } catch (Throwable th) {
                                                th = th;
                                                fileOutputStream2 = fileOutputStream;
                                                if (fileOutputStream2 != null) {
                                                }
                                                if (inputStream != null) {
                                                }
                                                requestConnect.disconnect();
                                                throw th;
                                            }
                                        }
                                    } catch (SocketTimeoutException unused2) {
                                        fileOutputStream = null;
                                        StaticMethods.logWarningFormat("Cached Files - Timed out making request (%s)", this.url);
                                        if (this.callback != null) {
                                        }
                                        if (fileOutputStream != null) {
                                        }
                                        if (inputStream != null) {
                                        }
                                        requestConnect.disconnect();
                                    } catch (IOException e9) {
                                        e = e9;
                                        fileOutputStream = null;
                                        StaticMethods.logWarningFormat("Cached Files - IOException while making request (%s)", e.getLocalizedMessage());
                                        if (this.callback != null) {
                                        }
                                        if (fileOutputStream != null) {
                                        }
                                        if (inputStream != null) {
                                        }
                                        requestConnect.disconnect();
                                    } catch (Exception e10) {
                                        e = e10;
                                        fileOutputStream = null;
                                        StaticMethods.logWarningFormat("Cached Files - Unexpected exception while attempting to get remote file (%s)", e.getLocalizedMessage());
                                        if (this.callback != null) {
                                        }
                                        if (fileOutputStream != null) {
                                        }
                                        if (inputStream != null) {
                                        }
                                        requestConnect.disconnect();
                                    } catch (Error e11) {
                                        e = e11;
                                        fileOutputStream = null;
                                        StaticMethods.logWarningFormat("Cached Files - Unexpected error while attempting to get remote file (%s)", e.getLocalizedMessage());
                                        if (this.callback != null) {
                                        }
                                        if (fileOutputStream != null) {
                                        }
                                        if (inputStream != null) {
                                        }
                                        requestConnect.disconnect();
                                    } catch (Throwable th2) {
                                        th = th2;
                                        if (fileOutputStream2 != null) {
                                        }
                                        if (inputStream != null) {
                                        }
                                        requestConnect.disconnect();
                                        throw th;
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e12) {
                    InstrumentationCallbacks.networkError(requestConnect, e12);
                    throw e12;
                } catch (SocketTimeoutException unused3) {
                } catch (Exception e13) {
                } catch (Error e14) {
                } catch (IOException e15) {
                    InstrumentationCallbacks.networkError(requestConnect, e15);
                    throw e15;
                } catch (IOException e16) {
                    InstrumentationCallbacks.networkError(requestConnect, e16);
                    throw e16;
                } catch (IOException e17) {
                    InstrumentationCallbacks.networkError(requestConnect, e17);
                    throw e17;
                } catch (IOException e18) {
                    InstrumentationCallbacks.networkError(requestConnect, e18);
                    throw e18;
                } catch (IOException e19) {
                    InstrumentationCallbacks.networkError(requestConnect, e19);
                    throw e19;
                } catch (IOException e20) {
                    e = e20;
                    inputStream = null;
                    fileOutputStream = null;
                    StaticMethods.logWarningFormat("Cached Files - IOException while making request (%s)", e.getLocalizedMessage());
                    if (this.callback != null) {
                        this.callback.call(false, (File) null);
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e21) {
                            str = "Cached Files - Exception while attempting to close streams (%s)";
                            objArr = new Object[]{e21.getLocalizedMessage()};
                            StaticMethods.logWarningFormat(str, objArr);
                        }
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    requestConnect.disconnect();
                } catch (Throwable th3) {
                }
            }
        }

        protected static HttpURLConnection requestConnect(String str) {
            try {
                return (HttpURLConnection) new URL(str).openConnection();
            } catch (Exception e) {
                StaticMethods.logErrorFormat("Cached Files - Exception opening URL(%s)", e.getLocalizedMessage());
                return null;
            }
        }
    }
}
