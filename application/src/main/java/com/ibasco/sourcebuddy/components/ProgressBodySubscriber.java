package com.ibasco.sourcebuddy.components;

import com.ibasco.sourcebuddy.util.ProgressCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@SuppressWarnings("Duplicates")
public class ProgressBodySubscriber<T> implements HttpResponse.BodySubscriber<T> {

    private static final Logger log = LoggerFactory.getLogger(ProgressBodySubscriber.class);

    private final Function<byte[], T> mapper;

    private final CompletableFuture<T> result = new CompletableFuture<>();

    private final List<ByteBuffer> received = new ArrayList<>();

    private volatile Flow.Subscription subscription;

    private final AtomicInteger progress = new AtomicInteger();

    private final ProgressCallback progressCallback;

    private final int length;

    public ProgressBodySubscriber(int length, Function<byte[], T> mapper, ProgressCallback progressCallback) {
        this.mapper = mapper;
        this.length = length;
        this.progressCallback = progressCallback;
    }

    @Override
    public CompletionStage<T> getBody() {
        return result;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        if (this.subscription != null) {
            subscription.cancel();
            return;
        }
        this.subscription = subscription;
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(List<ByteBuffer> items) {
        if (progressCallback != null) {
            int work = progress.addAndGet(remaining(items));
            progressCallback.onProgress(work, length, "Downloading..");
        }
        received.addAll(items);
    }

    @Override
    public void onError(Throwable throwable) {
        result.completeExceptionally(throwable);
    }

    private byte[] join(List<ByteBuffer> bytes) {
        int size = remaining(bytes);
        byte[] res = new byte[size];
        int from = 0;
        for (ByteBuffer b : bytes) {
            int l = b.remaining();
            b.get(res, from, l);
            from += l;
        }
        return res;
    }

    private int remaining(List<ByteBuffer> bufs) {
        int remainingBytes = 0;
        synchronized (bufs) {
            for (ByteBuffer buf : bufs) {
                remainingBytes += buf.remaining();
            }
        }
        return remainingBytes;
    }

    @Override
    public void onComplete() {
        try {
            if (mapper != null) {
                result.complete(mapper.apply(join(received)));
            } else {
                result.complete((T) join(received));
            }
            received.clear();
        } catch (IllegalArgumentException e) {
            result.completeExceptionally(e);
        }
    }
}
