package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import com.ibasco.sourcebuddy.service.RconService;
import com.ibasco.sourcebuddy.service.SourceServerManager;
import com.ibasco.sourcebuddy.service.SourceServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import org.springframework.context.annotation.Scope;

import java.util.Objects;

@Scope(SCOPE_PROTOTYPE)
public class UpdateSingleServerDetailsTask extends BaseTask<Void> {

    private static final Logger log = LoggerFactory.getLogger(UpdateSingleServerDetailsTask.class);

    private SourceServerService sourceServerService;

    private final ServerDetails details;

    private RconService rconService;

    private SourceServerManager serverManager;

    public UpdateSingleServerDetailsTask(ServerDetails details) {
        this.details = details;
    }

    @Override
    protected Void process() throws Exception {

        if (details == null) {
            log.debug("[Skipped: {}] Skipping single server details update", this.hashCode());
            return null;
        }

        sourceServerService.updateServerDetails(details).get();
        if (ServerStatus.ACTIVE.equals(details.getStatus())) {
            if (details.getPlayerCount() > 0) {
                sourceServerService.updatePlayerDetails(details).get();
            } else if (details.getPlayerCount() == 0 && details.getPlayers().size() > 0) {
                synchronized (details) {
                    details.getPlayers().clear();
                }
            }
            if (details.getRules() == null || details.getRules().isEmpty())
                sourceServerService.updateServerRules(details).get();
        }
        log.debug("[{}] Updating server details for '{}'", this.hashCode(), details);
        return null;
    }

    @Autowired
    public void setSourceServerService(SourceServerService sourceServerService) {
        this.sourceServerService = sourceServerService;
    }

    @Autowired
    public void setRconService(RconService rconService) {
        this.rconService = rconService;
    }

    @Autowired
    public void setServerManager(SourceServerManager serverManager) {
        this.serverManager = serverManager;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateSingleServerDetailsTask that = (UpdateSingleServerDetailsTask) o;
        return details.equals(that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(details);
    }
}
