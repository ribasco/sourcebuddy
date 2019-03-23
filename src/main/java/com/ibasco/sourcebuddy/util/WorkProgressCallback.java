package com.ibasco.sourcebuddy.util;

@FunctionalInterface
public interface WorkProgressCallback<T> {

    void onProgress(T item, Throwable exception);
}
