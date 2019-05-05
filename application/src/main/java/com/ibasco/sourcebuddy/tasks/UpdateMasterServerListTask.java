package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Scope("prototype")
public class UpdateMasterServerListTask extends BaseTask<Integer> {

    private static final Logger log = LoggerFactory.getLogger(UpdateMasterServerListTask.class);

    private SteamApp steamApp;

    private SourceServerService sourceServerService;

    public UpdateMasterServerListTask(SteamApp steamApp) {
        this.steamApp = steamApp;
    }

    @Autowired
    public void setSourceServerService(SourceServerService sourceServerService) {
        this.sourceServerService = sourceServerService;
    }

    @Override
    protected Integer process() throws Exception {
        updateTitle("New server list update for '%s'", steamApp);
        return sourceServerService.fetchNewServerEntries(steamApp, createIndeterminateProgressCallback());
    }

    public SteamApp getSteamApp() {
        return steamApp;
    }

    private WorkProgressCallback<ServerDetails> createIndeterminateProgressCallback() {
        return new WorkProgressCallback<>() {

            private AtomicInteger addedCtr = new AtomicInteger();

            private AtomicInteger skippedCtr = new AtomicInteger();

            {
                updateProgress(-1, Long.MIN_VALUE);
            }

            @Override
            public void onProgress(ServerDetails item, Throwable ex) {
                if (ex != null) {
                    skippedCtr.incrementAndGet();
                } else {
                    addedCtr.incrementAndGet();
                }
                updateMessage("Processing (Added: %s, Skipped: %d)", addedCtr.get(), skippedCtr.get());
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateMasterServerListTask that = (UpdateMasterServerListTask) o;
        return steamApp.equals(that.steamApp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(steamApp);
    }
}
