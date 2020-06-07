package com.adobe.mobile;

public final class Visitor {
    public static String appendToURL(String str) {
        return VisitorIDService.sharedInstance().appendVisitorInfoForURL(str);
    }
}
