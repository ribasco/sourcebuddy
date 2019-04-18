package com.ibasco.sourcebuddy.tasks;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.model.TreeDataModel;
import com.ibasco.sourcebuddy.service.SourceServerService;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import org.springframework.context.annotation.Scope;

@Scope(SCOPE_PROTOTYPE)
public class BuildBookmarkServerTreeTask extends BaseTask<TreeItem<ServerDetails>> {

    private static final Logger log = LoggerFactory.getLogger(BuildBookmarkServerTreeTask.class);

    private SourceServerService sourceServerService;

    private static TreeItem<ServerDetails> rootItem;

    private boolean update;

    public BuildBookmarkServerTreeTask() {
        this(false);
    }

    public BuildBookmarkServerTreeTask(boolean update) {
        this.update = update;
    }

    @Override
    protected TreeItem<ServerDetails> process() throws Exception {
        if (rootItem == null || update) {
            rootItem = new TreeItem<>();
            rootItem.setExpanded(true);

            TreeDataModel<ServerDetails> detailsTree = new TreeDataModel<>();

            log.debug("Building server bookmark tree");
            for (SteamApp steamApp : sourceServerService.findBookmarkedSteamApps()) {
                TreeDataModel<ServerDetails> tiApp = new TreeDataModel<>(new ServerDetails(steamApp.getName()));

                TreeItem<ServerDetails> tiSteamApp = new TreeItem<>(new ServerDetails(steamApp.getName()));
                tiSteamApp.setExpanded(true);
                rootItem.getChildren().add(tiSteamApp);
                detailsTree.getChildren().add(tiApp);
                for (ServerDetails details : sourceServerService.findBookmarkedServers(steamApp)) {
                    TreeItem<ServerDetails> tiBookmarkedServer = new TreeItem<>(details);
                    TreeDataModel<ServerDetails> tiBServer = new TreeDataModel<>(details);
                    tiApp.getChildren().add(tiBServer);
                    tiSteamApp.getChildren().add(tiBookmarkedServer);
                }
            }

            detailsTree.toList().forEach(s -> log.debug("Recursive item: {}", s));
        }
        return rootItem;
    }

    @Autowired
    public void setSourceServerService(SourceServerService sourceServerService) {
        this.sourceServerService = sourceServerService;
    }
}
