package com.ibasco.sourcebuddy.util;

@FunctionalInterface
public interface ProgressCallback {

    void onProgress(int work, int total, String message);
}
