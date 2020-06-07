package com.adobe.mobile;

import com.appdynamics.eumagent.runtime.InstrumentationCallbacks;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

final class RequestHandler {

    protected interface HeaderCallback {
        void call(Map<String, List<String>> map);
    }

    RequestHandler() {
    }

    protected static byte[] retrieveData(String str, final Map<String, String> map, int i, String str2) {
        if (StaticMethods.isWearableApp()) {
            return WearableFunctionBridge.retrieveData(str, i);
        }
        AnonymousClass1 r0 = new Callable<Map<String, String>>() {
            public Map<String, String> call() throws Exception {
                return map;
            }
        };
        if (map == null) {
            r0 = null;
        }
        return retrieveData(str, i, str2, r0, (HeaderCallback) null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:63:0x011e, code lost:
        r11 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0121, code lost:
        r11 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0122, code lost:
        r12 = null;
        r5 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0125, code lost:
        r11 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0126, code lost:
        r12 = null;
        r5 = r4;
     */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x019e  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x01a3 A[SYNTHETIC, Splitter:B:106:0x01a3] */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x01be  */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x01c3 A[SYNTHETIC, Splitter:B:116:0x01c3] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x011e A[ExcHandler: all (th java.lang.Throwable), Splitter:B:8:0x002d] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0121 A[ExcHandler: Error (e java.lang.Error), Splitter:B:8:0x002d] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0125 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:8:0x002d] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0144  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0149 A[SYNTHETIC, Splitter:B:80:0x0149] */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x0171  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0176 A[SYNTHETIC, Splitter:B:93:0x0176] */
    protected static byte[] retrieveData(String str, int i, String str2, Callable<Map<String, String>> callable, HeaderCallback headerCallback) {
        HttpURLConnection httpURLConnection;
        InputStream inputStream;
        InputStream inputStream2;
        InputStream inputStream3;
        Map call;
        InputStream inputStream4 = null;
        String str3 = str;
        HttpURLConnection httpURLConnection2 = null;
        int i2 = 0;
        int i3 = 0;
        while (true) {
            if (i2 > 21) {
                try {
                    StaticMethods.logErrorFormat("%s - Too many redirects for (%s) - %d", str2, str, Integer.valueOf(i2));
                } catch (IOException e) {
                    e = e;
                    inputStream2 = null;
                    StaticMethods.logWarningFormat("%s - IOException while sending request (%s)", str2, e.getLocalizedMessage());
                    if (httpURLConnection2 != null) {
                        httpURLConnection2.disconnect();
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                            StaticMethods.logWarningFormat("%s - Unable to close stream (%s)", str2, e2.getLocalizedMessage());
                        }
                    }
                    return null;
                } catch (Exception e3) {
                    e = e3;
                    inputStream3 = null;
                    StaticMethods.logWarningFormat("%s - Exception while sending request (%s)", str2, e.getLocalizedMessage());
                    if (httpURLConnection2 != null) {
                        httpURLConnection2.disconnect();
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e4) {
                            StaticMethods.logWarningFormat("%s - Unable to close stream (%s)", str2, e4.getLocalizedMessage());
                        }
                    }
                    return null;
                } catch (Error e5) {
                    e = e5;
                    inputStream = null;
                    try {
                        StaticMethods.logWarningFormat("%s - Unexpected error while sending request (%s)", str2, e.getLocalizedMessage());
                        if (httpURLConnection2 != null) {
                            httpURLConnection2.disconnect();
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e6) {
                                StaticMethods.logWarningFormat("%s - Unable to close stream (%s)", str2, e6.getLocalizedMessage());
                            }
                        }
                        return null;
                    } catch (Throwable th) {
                        th = th;
                        inputStream4 = inputStream;
                        httpURLConnection = httpURLConnection2;
                        if (httpURLConnection != null) {
                        }
                        if (inputStream4 != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    httpURLConnection = httpURLConnection2;
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    if (inputStream4 != null) {
                        try {
                            inputStream4.close();
                        } catch (IOException e7) {
                            StaticMethods.logWarningFormat("%s - Unable to close stream (%s)", str2, e7.getLocalizedMessage());
                        }
                    }
                    throw th;
                }
            } else {
                httpURLConnection = (HttpURLConnection) new URL(str3).openConnection();
                try {
                    httpURLConnection.setConnectTimeout(2000);
                    httpURLConnection.setReadTimeout(i);
                    httpURLConnection.setInstanceFollowRedirects(false);
                    httpURLConnection.setRequestProperty("Accept-Language", StaticMethods.getDefaultAcceptLanguage());
                    httpURLConnection.setRequestProperty("User-Agent", StaticMethods.getDefaultUserAgent());
                    if (!(callable == null || (call = callable.call()) == null)) {
                        for (Map.Entry entry : call.entrySet()) {
                            httpURLConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
                        }
                    }
                    InstrumentationCallbacks.requestAboutToBeSent(httpURLConnection);
                    int responseCode = httpURLConnection.getResponseCode();
                    InstrumentationCallbacks.requestSent(httpURLConnection);
                    InstrumentationCallbacks.requestHarvestable(httpURLConnection);
                    if (headerCallback != null) {
                        InstrumentationCallbacks.requestAboutToBeSent(httpURLConnection);
                        Map headerFields = httpURLConnection.getHeaderFields();
                        InstrumentationCallbacks.requestSent(httpURLConnection);
                        InstrumentationCallbacks.requestHarvestable(httpURLConnection);
                        headerCallback.call(headerFields);
                    }
                    switch (responseCode) {
                        case 301:
                        case 302:
                            i2++;
                            InstrumentationCallbacks.requestAboutToBeSent(httpURLConnection);
                            String headerField = httpURLConnection.getHeaderField("Location");
                            InstrumentationCallbacks.requestSent(httpURLConnection);
                            InstrumentationCallbacks.requestHarvestable(httpURLConnection);
                            str3 = new URL(new URL(str3), headerField).toExternalForm();
                            int i4 = responseCode;
                            httpURLConnection2 = httpURLConnection;
                            i3 = i4;
                        default:
                            int i5 = responseCode;
                            httpURLConnection2 = httpURLConnection;
                            i3 = i5;
                            break;
                    }
                } catch (IOException e8) {
                    InstrumentationCallbacks.networkError(httpURLConnection, e8);
                    throw e8;
                } catch (Exception e9) {
                } catch (Error e10) {
                } catch (IOException e11) {
                    e = e11;
                    inputStream2 = null;
                    httpURLConnection2 = httpURLConnection;
                    StaticMethods.logWarningFormat("%s - IOException while sending request (%s)", str2, e.getLocalizedMessage());
                    if (httpURLConnection2 != null) {
                    }
                    if (inputStream != null) {
                    }
                    return null;
                } catch (Throwable th3) {
                }
            }
        }
        if (i3 == 200) {
            InputStream inputStream5 = InstrumentationCallbacks.getInputStream(httpURLConnection2);
            try {
                byte[] bArr = new byte[1024];
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                while (true) {
                    int read = inputStream5.read(bArr);
                    if (read == -1) {
                        inputStream5.close();
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        if (httpURLConnection2 != null) {
                            httpURLConnection2.disconnect();
                        }
                        if (inputStream5 != null) {
                            try {
                                inputStream5.close();
                            } catch (IOException e12) {
                                StaticMethods.logWarningFormat("%s - Unable to close stream (%s)", str2, e12.getLocalizedMessage());
                            }
                        }
                        return byteArray;
                    }
                    byteArrayOutputStream.write(bArr, 0, read);
                }
            } catch (IOException e13) {
                IOException iOException = e13;
                inputStream2 = inputStream5;
                e = iOException;
            } catch (Exception e14) {
                Exception exc = e14;
                inputStream3 = inputStream5;
                e = exc;
                StaticMethods.logWarningFormat("%s - Exception while sending request (%s)", str2, e.getLocalizedMessage());
                if (httpURLConnection2 != null) {
                }
                if (inputStream != null) {
                }
                return null;
            } catch (Error e15) {
                Error error = e15;
                inputStream = inputStream5;
                e = error;
                StaticMethods.logWarningFormat("%s - Unexpected error while sending request (%s)", str2, e.getLocalizedMessage());
                if (httpURLConnection2 != null) {
                }
                if (inputStream != null) {
                }
                return null;
            } catch (Throwable th4) {
                inputStream4 = inputStream5;
                th = th4;
                httpURLConnection = httpURLConnection2;
                if (httpURLConnection != null) {
                }
                if (inputStream4 != null) {
                }
                throw th;
            }
        } else {
            if (httpURLConnection2 != null) {
                httpURLConnection2.disconnect();
            }
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0073, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0074, code lost:
        com.adobe.mobile.StaticMethods.logWarningFormat("%s - Exception while attempting to send hit, will not retry(%s)", r9, r6.getLocalizedMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0084, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0085, code lost:
        com.adobe.mobile.StaticMethods.logWarningFormat("%s - Exception while attempting to send hit, will not retry(%s)", r9, r6.getLocalizedMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00a6, code lost:
        com.adobe.mobile.StaticMethods.logWarningFormat("%s - Timed out sending request(%s)", r9, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0073 A[ExcHandler: Error (r6v5 'e' java.lang.Error A[CUSTOM_DECLARE]), Splitter:B:7:0x0010] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0084 A[ExcHandler: Exception (r6v3 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:7:0x0010] */
    /* JADX WARNING: Removed duplicated region for block: B:33:? A[ExcHandler: SocketTimeoutException (unused java.net.SocketTimeoutException), SYNTHETIC, Splitter:B:7:0x0010] */
    protected static void sendGenericRequest(String str, Map<String, String> map, int i, String str2) {
        HttpURLConnection requestConnect;
        if (str != null) {
            if (StaticMethods.isWearableApp()) {
                WearableFunctionBridge.sendGenericRequest(str, i, str2);
                return;
            }
            try {
                requestConnect = requestConnect(str);
                if (requestConnect != null) {
                    requestConnect.setConnectTimeout(i);
                    requestConnect.setReadTimeout(i);
                    if (map != null) {
                        for (Map.Entry next : map.entrySet()) {
                            String str3 = (String) next.getValue();
                            if (str3.trim().length() > 0) {
                                requestConnect.setRequestProperty((String) next.getKey(), str3);
                            }
                        }
                    }
                    StaticMethods.logDebugFormat("%s - Request Sent(%s)", str2, str);
                    InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
                    requestConnect.getResponseCode();
                    InstrumentationCallbacks.requestSent(requestConnect);
                    InstrumentationCallbacks.requestHarvestable(requestConnect);
                    InstrumentationCallbacks.getInputStream(requestConnect).close();
                    requestConnect.disconnect();
                }
            } catch (IOException e) {
                InstrumentationCallbacks.networkError(requestConnect, e);
                throw e;
            } catch (SocketTimeoutException unused) {
            } catch (Exception e2) {
            } catch (Error e3) {
            } catch (IOException e4) {
                StaticMethods.logWarningFormat("%s - IOException while sending request, may retry(%s)", str2, e4.getLocalizedMessage());
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00df, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00e1, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        com.adobe.mobile.StaticMethods.logErrorFormat("%s - Exception while attempting to send hit, will not retry(%s)", r11, r8.getLocalizedMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0100, code lost:
        return new byte[0];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0101, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:?, code lost:
        com.adobe.mobile.StaticMethods.logErrorFormat("%s - Exception while attempting to send hit, will not retry(%s)", r11, r8.getLocalizedMessage());
        r8 = new byte[0];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x011b, code lost:
        if (com.adobe.mobile.MobileConfig.getInstance().getSSL() == false) goto L_0x011d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x011d, code lost:
        r7.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0120, code lost:
        return r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:?, code lost:
        com.adobe.mobile.StaticMethods.logDebugFormat("%s - Timed out sending request(%s)", r11, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0152, code lost:
        if (com.adobe.mobile.MobileConfig.getInstance().getSSL() == false) goto L_0x0154;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0154, code lost:
        r7.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0157, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0160, code lost:
        if (com.adobe.mobile.MobileConfig.getInstance().getSSL() == false) goto L_0x0162;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0162, code lost:
        r7.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0165, code lost:
        throw r8;
     */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00e1 A[ExcHandler: Error (r8v10 'e' java.lang.Error A[CUSTOM_DECLARE]), Splitter:B:11:0x0019] */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0101 A[ExcHandler: Exception (r8v7 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:11:0x0019] */
    /* JADX WARNING: Removed duplicated region for block: B:75:? A[ExcHandler: SocketTimeoutException (unused java.net.SocketTimeoutException), SYNTHETIC, Splitter:B:11:0x0019] */
    protected static byte[] retrieveAnalyticsRequestData(String str, String str2, Map<String, String> map, int i, String str3) {
        if (str == null) {
            return null;
        }
        if (StaticMethods.isWearableApp()) {
            return WearableFunctionBridge.retrieveAnalyticsRequestData(str, str2, i, str3);
        }
        HttpURLConnection requestConnect = requestConnect(str);
        if (requestConnect == null) {
            return null;
        }
        try {
            requestConnect.setConnectTimeout(i);
            requestConnect.setReadTimeout(i);
            requestConnect.setRequestMethod("POST");
            if (!MobileConfig.getInstance().getSSL()) {
                requestConnect.setRequestProperty("connection", "close");
            }
            byte[] bytes = str2.getBytes("UTF-8");
            requestConnect.setFixedLengthStreamingMode(bytes.length);
            requestConnect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if (map != null) {
                for (Map.Entry next : map.entrySet()) {
                    requestConnect.setRequestProperty((String) next.getKey(), (String) next.getValue());
                }
            }
            InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
            OutputStream outputStream = requestConnect.getOutputStream();
            InstrumentationCallbacks.requestSent(requestConnect);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(bytes);
            bufferedOutputStream.close();
            InputStream inputStream = InstrumentationCallbacks.getInputStream(requestConnect);
            byte[] bArr = new byte[1024];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            if (!MobileConfig.getInstance().getSSL()) {
                InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
                int responseCode = requestConnect.getResponseCode();
                InstrumentationCallbacks.requestSent(requestConnect);
                InstrumentationCallbacks.requestHarvestable(requestConnect);
                if (responseCode == 200) {
                }
                inputStream.close();
                StaticMethods.logDebugFormat("%s - Request Sent(%s)", str3, str2);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                if (!MobileConfig.getInstance().getSSL()) {
                    requestConnect.disconnect();
                }
                return byteArray;
            }
            while (true) {
                int read = inputStream.read(bArr);
                if (read == -1) {
                    break;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            }
            inputStream.close();
            StaticMethods.logDebugFormat("%s - Request Sent(%s)", str3, str2);
            byte[] byteArray2 = byteArrayOutputStream.toByteArray();
            if (!MobileConfig.getInstance().getSSL()) {
            }
            return byteArray2;
        } catch (IOException e) {
            InstrumentationCallbacks.networkError(requestConnect, e);
            throw e;
        } catch (SocketTimeoutException unused) {
        } catch (Exception e2) {
        } catch (Error e3) {
        } catch (IOException e4) {
            InstrumentationCallbacks.networkError(requestConnect, e4);
            throw e4;
        } catch (IOException e5) {
            StaticMethods.logDebugFormat("%s - IOException while sending request, may retry(%s)", str3, e5.getLocalizedMessage());
            if (!MobileConfig.getInstance().getSSL()) {
                requestConnect.disconnect();
            }
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b0, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00b1, code lost:
        com.adobe.mobile.StaticMethods.logErrorFormat("%s - Exception while attempting to send hit, will not retry (%s)", r10, r5.getLocalizedMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00c1, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00c2, code lost:
        com.adobe.mobile.StaticMethods.logErrorFormat("%s - Exception while attempting to send hit, will not retry (%s)", r10, r5.getLocalizedMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00e3, code lost:
        com.adobe.mobile.StaticMethods.logDebugFormat("%s - Timed out sending request (%s)", r10, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00ee, code lost:
        return false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00b0 A[ExcHandler: Error (r5v6 'e' java.lang.Error A[CUSTOM_DECLARE]), Splitter:B:11:0x0018] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c1 A[ExcHandler: Exception (r5v4 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:11:0x0018] */
    /* JADX WARNING: Removed duplicated region for block: B:48:? A[ExcHandler: SocketTimeoutException (unused java.net.SocketTimeoutException), SYNTHETIC, Splitter:B:11:0x0018] */
    protected static boolean sendThirdPartyRequest(String str, String str2, Map<String, String> map, int i, String str3, String str4) {
        if (str == null) {
            return false;
        }
        if (StaticMethods.isWearableApp()) {
            return WearableFunctionBridge.sendThirdPartyRequest(str, str2, i, str3, str4);
        }
        HttpURLConnection requestConnect = requestConnect(str);
        if (requestConnect == null) {
            return false;
        }
        try {
            requestConnect.setConnectTimeout(i);
            requestConnect.setReadTimeout(i);
            requestConnect.setRequestMethod("GET");
            if (map != null) {
                for (Map.Entry next : map.entrySet()) {
                    requestConnect.setRequestProperty((String) next.getKey(), (String) next.getValue());
                }
            }
            if (str2 != null && str2.length() > 0) {
                requestConnect.setRequestMethod("POST");
                String str5 = (str3 == null || str3.length() <= 0) ? "application/x-www-form-urlencoded" : str3;
                byte[] bytes = str2.getBytes("UTF-8");
                requestConnect.setFixedLengthStreamingMode(bytes.length);
                requestConnect.setRequestProperty("Content-Type", str5);
                InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
                OutputStream outputStream = requestConnect.getOutputStream();
                InstrumentationCallbacks.requestSent(requestConnect);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                bufferedOutputStream.write(bytes);
                bufferedOutputStream.close();
            }
            InputStream inputStream = InstrumentationCallbacks.getInputStream(requestConnect);
            while (inputStream.read(new byte[10]) > 0) {
            }
            inputStream.close();
            StaticMethods.logDebugFormat("%s - Successfully forwarded hit (%s body: %s type: %s)", str4, str, str2, str3);
        } catch (IOException e) {
            InstrumentationCallbacks.networkError(requestConnect, e);
            throw e;
        } catch (SocketTimeoutException unused) {
        } catch (Exception e2) {
        } catch (Error e3) {
        } catch (IOException e4) {
            StaticMethods.logDebugFormat("%s - IOException while sending request, will not retry (%s)", str4, e4.getLocalizedMessage());
        }
        return true;
    }

    protected static HttpURLConnection requestConnect(String str) {
        try {
            return (HttpURLConnection) new URL(str).openConnection();
        } catch (Exception e) {
            StaticMethods.logErrorFormat("Adobe Mobile - Exception opening URL(%s)", e.getLocalizedMessage());
            return null;
        }
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.adobe.mobile.NetworkObject, java.io.InputStream] */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x01a0, code lost:
        r5.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x01a3, code lost:
        if (r0 != 0) goto L_0x01a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x01a9, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x01aa, code lost:
        r6 = "%s - Unable to close stream (%s)";
        r7 = new java.lang.Object[]{r11, r5.getLocalizedMessage()};
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0101, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
        com.adobe.mobile.StaticMethods.logErrorFormat("%s - Exception while trying to get content (%s)", r11, r6.getLocalizedMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0111, code lost:
        r5.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0114, code lost:
        if (r0 != 0) goto L_0x0116;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x011b, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x011c, code lost:
        r6 = "%s - Unable to close stream (%s)";
        r7 = new java.lang.Object[]{r11, r5.getLocalizedMessage()};
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x012a, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:?, code lost:
        com.adobe.mobile.StaticMethods.logErrorFormat("%s - Exception while trying to get content (%s)", r11, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0136, code lost:
        r5.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0139, code lost:
        if (r0 != 0) goto L_0x013b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0140, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0141, code lost:
        r6 = "%s - Unable to close stream (%s)";
        r7 = new java.lang.Object[]{r11, r5.getLocalizedMessage()};
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0171, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:?, code lost:
        com.adobe.mobile.StaticMethods.logErrorFormat("%s - NullPointerException while trying to get content (%s)", r11, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x017d, code lost:
        r5.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x0180, code lost:
        if (r0 != 0) goto L_0x0182;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0186, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0187, code lost:
        r6 = "%s - Unable to close stream (%s)";
        r7 = new java.lang.Object[]{r11, r5.getLocalizedMessage()};
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x0194, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:?, code lost:
        com.adobe.mobile.StaticMethods.logErrorFormat("%s - ProtocolException while trying to get content (%s)", r11, r6);
     */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0101 A[ExcHandler: Exception (r6v10 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:4:0x0010] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x012a A[ExcHandler: Error (r6v8 'e' java.lang.Object A[CUSTOM_DECLARE]), Splitter:B:4:0x0010] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0171 A[ExcHandler: NullPointerException (r6v4 'e' java.lang.Object A[CUSTOM_DECLARE]), Splitter:B:4:0x0010] */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x0194 A[ExcHandler: ProtocolException (r6v2 'e' java.lang.Object A[CUSTOM_DECLARE]), Splitter:B:4:0x0010] */
    protected static NetworkObject retrieveNetworkObject(String str, String str2, String str3, String str4, int i, String str5, String str6, String str7) {
        Object[] objArr;
        String str8;
        HttpURLConnection requestConnect = requestConnect(str);
        ? r0 = 0;
        if (requestConnect == null) {
            return r0;
        }
        NetworkObject networkObject = new NetworkObject();
        try {
            requestConnect.setRequestMethod(str2);
            int i2 = i * 1000;
            requestConnect.setReadTimeout(i2);
            requestConnect.setConnectTimeout(i2);
            if (str5 != null && !str5.isEmpty()) {
                requestConnect.setRequestProperty("Content-Type", str5);
            }
            if (str3 != null && !str3.isEmpty()) {
                requestConnect.setRequestProperty("Accept", str3);
            }
            requestConnect.setRequestProperty("Accept-Encoding", "identity");
            requestConnect.setRequestProperty("Accept-Language", StaticMethods.getDefaultAcceptLanguage());
            requestConnect.setRequestProperty("User-Agent", StaticMethods.getDefaultUserAgent());
            if (str7 != null && !str7.isEmpty()) {
                requestConnect.setRequestProperty("session-id", str7);
            }
            if (str2 != null && (str2.equalsIgnoreCase("POST") || str2.equalsIgnoreCase("PUT"))) {
                requestConnect.setDoOutput(true);
            }
            if (str4 != null && !str4.isEmpty()) {
                byte[] bytes = str4.getBytes("UTF-8");
                requestConnect.setFixedLengthStreamingMode(bytes.length);
                InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
                OutputStream outputStream = requestConnect.getOutputStream();
                InstrumentationCallbacks.requestSent(requestConnect);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                bufferedOutputStream.write(bytes);
                bufferedOutputStream.close();
            }
            InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
            int responseCode = requestConnect.getResponseCode();
            InstrumentationCallbacks.requestSent(requestConnect);
            InstrumentationCallbacks.requestHarvestable(requestConnect);
            networkObject.responseCode = responseCode;
            InputStream inputStream = InstrumentationCallbacks.getInputStream(requestConnect);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                sb.append(readLine);
            }
            networkObject.response = sb.toString();
            InstrumentationCallbacks.requestAboutToBeSent(requestConnect);
            Map<String, List<String>> headerFields = requestConnect.getHeaderFields();
            InstrumentationCallbacks.requestSent(requestConnect);
            InstrumentationCallbacks.requestHarvestable(requestConnect);
            networkObject.responseHeaders = headerFields;
            requestConnect.disconnect();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    str8 = "%s - Unable to close stream (%s)";
                    objArr = new Object[]{str6, e.getLocalizedMessage()};
                }
            }
        } catch (IOException e2) {
            InstrumentationCallbacks.networkError(requestConnect, e2);
            throw e2;
        } catch (ProtocolException e3) {
        } catch (NullPointerException e4) {
        } catch (Error e5) {
        } catch (Exception e6) {
        } catch (IOException e7) {
            InstrumentationCallbacks.networkError(requestConnect, e7);
            throw e7;
        } catch (IOException e8) {
            StaticMethods.logErrorFormat("%s - IOException while trying to get content (%s)", str6, e8);
            requestConnect.disconnect();
            if (r0 != 0) {
                try {
                    r0.close();
                } catch (IOException e9) {
                    str8 = "%s - Unable to close stream (%s)";
                    objArr = new Object[]{str6, e9.getLocalizedMessage()};
                }
            }
        } catch (Throwable th) {
            requestConnect.disconnect();
            if (r0 != 0) {
                try {
                    r0.close();
                } catch (IOException e10) {
                    StaticMethods.logWarningFormat("%s - Unable to close stream (%s)", str6, e10.getLocalizedMessage());
                }
            }
            throw th;
        }
        return networkObject;
        StaticMethods.logWarningFormat(str8, objArr);
        return networkObject;
    }
}
