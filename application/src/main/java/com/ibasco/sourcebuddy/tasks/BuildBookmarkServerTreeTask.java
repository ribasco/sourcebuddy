package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.components.GuiHelper;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.model.TreeDataModel;
import com.ibasco.sourcebuddy.service.SourceServerService;
import static java.util.stream.Collectors.groupingBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import org.springframework.context.annotation.Scope;

@Scope(SCOPE_PROTOTYPE)
public class BuildBookmarkServerTreeTask extends BaseTask<TreeDataModel<ServerDetails>> {

    private static final Logger log = LoggerFactory.getLogger(BuildBookmarkServerTreeTask.class);

    private SourceServerService sourceServerService;

    @Override
    protected TreeDataModel<ServerDetails> process() throws Exception {
        var groupedServers = sourceServerService.findBookmarkedServers().stream().collect(groupingBy(ServerDetails::getSteamApp));
        return GuiHelper.mapToTreeDataModel(groupedServers, steamApp -> new ServerDetails(steamApp.getName()));
    }

    @Autowired
    public void setSourceServerService(SourceServerService sourceServerService) {
        this.sourceServerService = sourceServerService;
    }
}
