package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.model.TreeDataModel;
import com.ibasco.sourcebuddy.service.ServerManager;
import static java.util.stream.Collectors.groupingBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import org.springframework.context.annotation.Scope;

import java.util.List;

@Scope(SCOPE_PROTOTYPE)
public class BuildManagedServersTreeTask extends BaseTask<TreeDataModel<ServerDetails>> {

    private static final Logger log = LoggerFactory.getLogger(BuildManagedServersTreeTask.class);

    private ServerManager serverManager;

    @Override
    protected TreeDataModel<ServerDetails> process() throws Exception {
        TreeDataModel<ServerDetails> detailsTree = new TreeDataModel<>();
        var groupedServers = serverManager.findManagedServer()
                .stream()
                .map(ManagedServer::getServerDetails)
                .collect(groupingBy(ServerDetails::getSteamApp));
        for (var entry : groupedServers.entrySet()) {
            List<ServerDetails> detailList = entry.getValue();
            TreeDataModel<ServerDetails> tiApp = new TreeDataModel<>(new ServerDetails(entry.getKey().getName()));
            detailsTree.getChildren().add(tiApp);
            for (ServerDetails server : detailList) {
                TreeDataModel<ServerDetails> serverTree = new TreeDataModel<>(server);
                tiApp.getChildren().add(serverTree);
            }
        }
        return detailsTree;
    }

    @Autowired
    public void setServerManager(ServerManager serverManager) {
        this.serverManager = serverManager;
    }
}
