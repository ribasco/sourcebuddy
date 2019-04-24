package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ServerDetails;
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

    public UpdateSingleServerDetailsTask(ServerDetails details) {
        this.details = details;
    }

    @Override
    protected Void process() throws Exception {
        if (details == null) {
            log.debug("[Skipped: {}] Skipping single server details update", this.hashCode());
            return null;
        }
        log.debug("[Start: {}] Updating server details for '{}'", this.hashCode(), details);
        try {
            sourceServerService.updateServerDetails(details).get();
            sourceServerService.updatePlayerDetails(details).get();
            sourceServerService.updateServerRules(details).get();
        } finally {
            log.debug("[End: {}] Updating server details for '{}'", this.hashCode(), details);
        }
        return null;
    }

    @Autowired
    public void setSourceServerService(SourceServerService sourceServerService) {
        this.sourceServerService = sourceServerService;
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
