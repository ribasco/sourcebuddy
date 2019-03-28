package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.service.SourceServerService;
import com.ibasco.sourcebuddy.util.WorkProgressCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope("prototype")
public class UpdateMasterServerListTask extends BaseTask<Void> {

    private static final Logger log = LoggerFactory.getLogger(UpdateMasterServerListTask.class);

    private SteamApp steamApp;

    private SourceServerService sourceServerService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public UpdateMasterServerListTask(SteamApp steamApp) {
        this.steamApp = steamApp;
    }

    @Autowired
    public void setSourceServerService(SourceServerService sourceServerService) {
        this.sourceServerService = sourceServerService;
    }

    @Override
    protected Void process() throws Exception {
        updateTitle("New server list update for '%s'", steamApp);
        sourceServerService.updateServerEntries(steamApp, createIndeterminateProgressCallback());
        return null;
    }

    protected WorkProgressCallback<ServerDetails> createIndeterminateProgressCallback() {
        return new WorkProgressCallback<>() {

            private AtomicInteger addedCtr = new AtomicInteger();

            private AtomicInteger skippedCtr = new AtomicInteger();

            {
                updateProgress(-1, Long.MIN_VALUE);
            }

            @Override
            public void onProgress(ServerDetails item, Throwable exception) {
                if (exception != null) {
                    skippedCtr.incrementAndGet();
                } else {
                    addedCtr.incrementAndGet();
                }
                updateMessage("Processing (Added: %s, Skipped: %d)", addedCtr.get(), skippedCtr.get());
            }
        };
    }
}
