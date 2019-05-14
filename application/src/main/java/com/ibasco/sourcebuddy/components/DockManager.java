package com.ibasco.sourcebuddy.components;

import com.ibasco.sourcebuddy.config.DockConfig;
import com.ibasco.sourcebuddy.domain.ConfigProfile;
import com.ibasco.sourcebuddy.domain.DockEntry;
import com.ibasco.sourcebuddy.domain.DockLayout;
import com.ibasco.sourcebuddy.domain.DockLayoutEntry;
import com.ibasco.sourcebuddy.model.AppModel;
import com.ibasco.sourcebuddy.repository.DockEntryRepository;
import com.ibasco.sourcebuddy.repository.DockLayoutEntryRepository;
import com.ibasco.sourcebuddy.repository.DockLayoutRepository;
import com.ibasco.sourcebuddy.service.ConfigService;
import com.ibasco.sourcebuddy.util.Check;
import com.ibasco.sourcebuddy.util.DockLayoutEntryComparator;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;
import org.dockfx.DockTitleBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
public class DockManager {

    private static final Logger log = LoggerFactory.getLogger(DockManager.class);

    private static final DockPos DEFAULT_POS = DockPos.RIGHT;

    private DockEntryRepository dockEntryRepository;

    private DockLayoutRepository dockLayoutRepository;

    private DockLayoutEntryRepository dockLayoutEntryRepository;

    private ConfigurableApplicationContext applicationContext;

    private AppModel appModel;

    private ConfigService configService;

    private Map<String, DockNode> beanDockMapping;

    @PostConstruct
    void init() {
        log.info("Initializing Dock Manager");
        initDockEntries();
        log.info("Checking default layout");
        initDefaultLayout();
        beanDockMapping = applicationContext.getBeansOfType(DockNode.class);
    }

    private void initDockEntries() {
        Map<String, DockNode> dockEntries = applicationContext.getBeansOfType(DockNode.class);
        log.info("Initializing dock entries in repository");
        for (Map.Entry<String, DockNode> entry : dockEntries.entrySet()) {
            if (!dockEntryRepository.existsById(entry.getKey())) {
                log.info(" - {} = {}", entry.getKey(), entry.getValue());
                DockEntry dockEntry = new DockEntry();
                dockEntry.setId(entry.getKey());
                dockEntry.setName(entry.getValue().getTitle());
                dockEntryRepository.save(dockEntry);
                log.info("Added new dock entry: {}", dockEntry);
            }
        }
    }

    private void initDefaultLayout() {
        List<DockLayout> layoutList = dockLayoutRepository.findAll();
        ConfigProfile activeProfile = appModel.getActiveProfile();
        DockLayout defaultLayout = activeProfile.getDefaultLayout();

        if (defaultLayout == null) {
            log.info("No default layout is set on the current active profile");
            if (layoutList.isEmpty()) {
                log.info("No layout(s) found in repository. Creating default layout");
                defaultLayout = createDefaultLayout();
            } else {
                log.info("Existing layout entries found in repository. Selecting first item.");
                defaultLayout = layoutList.stream().findFirst().orElseThrow();
            }
            log.info("Saving default layout to repository '{}'", defaultLayout);
            defaultLayout = dockLayoutRepository.save(defaultLayout);
            log.info("Default layout saved to repository");
            activeProfile.setDefaultLayout(defaultLayout);
            configService.saveProfile(activeProfile);
            log.info("Default layout '{}' saved to active profile '{}'", defaultLayout, activeProfile);
        }
    }

    public Optional<DockLayout> findLayoutById(int id) {
        return dockLayoutRepository.findById(id);
    }

    /**
     * Creates a new DockLayout instance based on the current DockPane state
     *
     * @param dockPane
     *         The DockPane to process
     * @param name
     *         The name of the new layout
     *
     * @return The {@link DockLayout} created
     */
    public DockLayout createLayout(DockPane dockPane, String name) {
        log.info("createLayout() :: Creating new layout '{}'", name);
        DockLayout layout = new DockLayout();
        refreshLayout(dockPane, layout);
        layout.setName(name);
        return layout;
    }

    public DockLayout saveLayout(DockLayout layout) {
        return dockLayoutRepository.save(layout);
    }

    public DockLayout updateLayout(DockPane dockPane, DockLayout layout) {
        Check.requireNonNull(layout, "Layout cannot be null");
        refreshLayout(dockPane, layout);
        return saveLayout(layout);
    }

    public void dockNode(DockPane dockPane, DockNode dockNode) {
        Check.requireNonNull(dockPane, "Dock pane cannot be null");
        Check.requireNonNull(dockNode, "Dock node cannot be null");

        String beanId = findBeanIdByNode(dockNode);
        ConfigProfile activeProfile = appModel.getActiveProfile();
        assert activeProfile != null;

        DockLayoutEntry layoutEntry = activeProfile.getDefaultLayout().getLayoutEntries()
                .stream()
                .filter(p -> p.getFrom().getId().equalsIgnoreCase(beanId))
                .findFirst()
                .orElse(null);

        if (layoutEntry != null) {
            DockNode sibling = null;

            if (layoutEntry.getTo() != null) {
                sibling = findDockNodeById(layoutEntry.getTo().getId());
            }

            if (sibling != null) {
                dockNode.dock(dockPane, layoutEntry.getPosition(), sibling);
            } else {
                dockNode.dock(dockPane, layoutEntry.getPosition());
            }
        } else {
            log.debug("No layout entry found for dock '{}'. Using default properties.", dockNode.getTitle());
            dockNode.dock(dockPane, DEFAULT_POS);
        }
    }

    public void undockNode(DockNode dockNode) {
        Check.requireNonNull(dockNode, "Dock node cannot be null");
        dockNode.undock();
    }

    public void lockLayout(DockLayout layout) {
        beanDockMapping.values().forEach(d -> {
            d.setDockTitleBar(null);
        });
        layout.setLocked(true);
        List<DockLayout> layoutList = appModel.getActiveProfile().getDockLayouts();
        int idx = layoutList.indexOf(layout);
        if (idx > -1)
            layoutList.set(idx, layout);
        appModel.setActiveProfile(configService.saveProfile(appModel.getActiveProfile()));
    }

    public void unlockLayout(DockLayout layout) {
        beanDockMapping.values().forEach(d -> d.setDockTitleBar(new DockTitleBar(d)));
        layout.setLocked(false);
        List<DockLayout> layoutList = appModel.getActiveProfile().getDockLayouts();
        int idx = layoutList.indexOf(layout);
        if (idx > -1)
            layoutList.set(idx, layout);
        appModel.setActiveProfile(configService.saveProfile(appModel.getActiveProfile()));
    }

    public void refreshLayout(Parent pane, DockLayout layout) {
        if (layout.getLayoutEntries() == null)
            layout.setLayoutEntries(new HashSet<>());
        layout.getLayoutEntries().clear();
        refreshLayout(pane, layout, 0);
        log.info("Refreshed layout entries for '{}' (Total: {})", layout, layout.getLayoutEntries().size());
    }

    private void refreshLayout(Parent pane, DockLayout layout, int level) {
        for (Node child : pane.getChildrenUnmodifiable()) {
            if (child instanceof DockNode) {
                DockLayoutEntry layoutEntry = createLayoutEntry((DockNode) child, layout);
                layout.getLayoutEntries().add(layoutEntry);
            }
            if (child instanceof Parent) {
                refreshLayout((Parent) child, layout, ++level);
            }
        }
    }

    public void applyLayout(DockPane dockPane, DockLayout dockLayout) {
        clearDocks();
        int idx = 0;
        log.info("Applying dock layout '{}'", dockLayout);

        Set<DockLayoutEntry> sortedSet = dockLayout.getLayoutEntries().stream().sorted(new DockLayoutEntryComparator()).collect(Collectors.toCollection(LinkedHashSet::new));

        for (DockLayoutEntry layoutEntry : sortedSet) {
            DockPos position = layoutEntry.getPosition();
            String fromBeanId = layoutEntry.getFrom().getId();
            String toBeanId = null;
            DockNode fromDock = applicationContext.getBean(fromBeanId, DockNode.class);
            if (fromDock.isDocked())
                continue;
            DockNode toDock = null;
            if (layoutEntry.getTo() != null) {
                toBeanId = layoutEntry.getTo().getId();
                toDock = applicationContext.getBean(toBeanId, DockNode.class);
            }
            log.debug("{}) Docking node FROM: {}, TO: {}, Position: {} (Dock Pane: {})", ++idx, fromBeanId, toBeanId, position, dockPane);
            if (toDock != null)
                fromDock.dock(dockPane, position, toDock);
            else
                fromDock.dock(dockPane, position);
        }
    }

    public void clearDocks() {
        log.info("Clearing docks");
        applicationContext.getBeansOfType(DockNode.class).values().forEach(d -> {
            if (d.isDocked()) {
                d.undock();
            }
        });
    }

    private DockLayoutEntry createLayoutEntry(DockNode dockNode, DockLayout layout) {
        DockNode dockSibling = null;
        if (dockNode.getLastDockSibling() instanceof DockNode) {
            dockSibling = (DockNode) dockNode.getLastDockSibling();
        }
        DockPos position = dockNode.getLastDockPos();
        String fromId = findDockBeanId(dockNode);
        String toId = findDockBeanId(dockSibling);

        DockLayoutEntry layoutEntry = new DockLayoutEntry();
        DockEntry fromEntry = Check.requireNonNull(dockEntryRepository.findById(fromId).orElse(null), "From dock cannot be null");

        DockEntry toEntry = null;
        if (toId != null) {
            toEntry = dockEntryRepository.findById(toId).orElse(null);
        }

        layoutEntry.setLayout(layout);
        layoutEntry.setFrom(fromEntry);
        layoutEntry.setTo(toEntry);
        layoutEntry.setPosition(position);

        log.debug("createLayoutEntry() :: Dock: {}, Position: {}, Sibling: {}", fromId, position, dockSibling != null ? toId : "N/A");
        return layoutEntry;
    }

    private DockLayout createDefaultLayout() {
        DockLayout layout = new DockLayout();
        layout.setName("Default");
        layout.setProfile(appModel.getActiveProfile());
        addDockEntry(layout, DockPos.TOP, DockConfig.DOCK_SERVER_BROWSER, null);
        addDockEntry(layout, DockPos.RIGHT, DockConfig.DOCK_PLAYER_BROWSER, null);
        addDockEntry(layout, DockPos.LEFT, DockConfig.DOCK_GAME_BROWSER, null);
        addDockEntry(layout, DockPos.BOTTOM, DockConfig.DOCK_RCON, DockConfig.DOCK_SERVER_BROWSER);
        addDockEntry(layout, DockPos.CENTER, DockConfig.DOCK_LOGS, DockConfig.DOCK_RCON);
        addDockEntry(layout, DockPos.CENTER, DockConfig.DOCK_CHAT, DockConfig.DOCK_RCON);
        addDockEntry(layout, DockPos.BOTTOM, DockConfig.DOCK_RULES_BROWSER, DockConfig.DOCK_PLAYER_BROWSER);
        return layout;
    }

    private void addDockEntry(DockLayout layout, DockPos pos, String fromId, String toId) {
        DockEntry fromDockEntry = dockEntryRepository.findById(fromId).orElse(null);
        DockEntry toDockEntry = null;
        if (toId != null) {
            toDockEntry = dockEntryRepository.findById(toId).orElse(null);
        }
        layout.getLayoutEntries().add(new DockLayoutEntry(layout, pos, fromDockEntry, toDockEntry));
    }

    private String findDockBeanId(DockNode dockNode) {
        Map<String, DockNode> nodes = applicationContext.getBeansOfType(DockNode.class);
        return nodes.entrySet().stream().filter(p -> p.getValue().equals(dockNode)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private String findBeanIdByNode(DockNode node) {
        return beanDockMapping.entrySet().stream().filter(p -> p.getValue().equals(node)).map(Map.Entry::getKey).findFirst().orElseThrow();
    }

    private DockNode findDockNodeById(String id) {
        return beanDockMapping.get(id);
    }

    @Autowired
    public void setDockEntryRepository(DockEntryRepository dockEntryRepository) {
        this.dockEntryRepository = dockEntryRepository;
    }

    @Autowired
    public void setDockLayoutRepository(DockLayoutRepository dockLayoutRepository) {
        this.dockLayoutRepository = dockLayoutRepository;
    }

    @Autowired
    public void setDockLayoutEntryRepository(DockLayoutEntryRepository dockLayoutEntryRepository) {
        this.dockLayoutEntryRepository = dockLayoutEntryRepository;
    }

    @Autowired
    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setAppModel(AppModel appModel) {
        this.appModel = appModel;
    }

    @Autowired
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
