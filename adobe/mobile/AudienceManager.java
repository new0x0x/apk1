package com.adobe.mobile;

public final class AudienceManager {

    public interface AudienceManagerCallback<T> {
        void call(T t);
    }
}
