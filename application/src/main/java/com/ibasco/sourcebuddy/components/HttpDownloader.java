package com.ibasco.sourcebuddy.components;

import com.google.gson.Gson;
import com.ibasco.sourcebuddy.util.ProgressBodySubscriber;
import com.ibasco.sourcebuddy.util.ProgressCallback;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

@Component
public class HttpDownloader {

    private Gson gsonProvider;

    public <T> CompletableFuture<T> downloadJson(String url, Type type, ProgressCallback progressCallback) throws IOException {
        return downloadText(url, progressCallback).thenApply(s -> {
            if (!StringUtils.isBlank(s))
                return gsonProvider.fromJson(s, type);
            return null;
        });
    }

    public CompletableFuture<String> downloadText(String url, ProgressCallback progressCallback) throws IOException {
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        return client.sendAsync(request, responseInfo -> {
            int contentLength = responseInfo.headers().firstValue("content-length").map(Integer::valueOf).orElse(-1);
            return new ProgressBodySubscriber<>(contentLength, String::new, progressCallback);
        }).thenApply(HttpResponse::body);
    }

    public CompletableFuture<byte[]> downloadRaw(String url, ProgressCallback progressCallback) throws IOException {
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        return client.sendAsync(request, responseInfo -> {
            int contentLength = responseInfo.headers().firstValue("content-length").map(Integer::valueOf).orElse(-1);
            return new ProgressBodySubscriber<>(contentLength, this::noMap, progressCallback);
        }).thenApply(HttpResponse::body);
    }

    private byte[] noMap(byte[] bytes) {
        return bytes;
    }

    @Autowired
    public void setGsonProvider(Gson gsonProvider) {
        this.gsonProvider = gsonProvider;
    }
}
