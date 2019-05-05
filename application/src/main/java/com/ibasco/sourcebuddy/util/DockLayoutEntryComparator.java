package com.ibasco.sourcebuddy.util;

import com.ibasco.sourcebuddy.config.DockConfig;
import com.ibasco.sourcebuddy.domain.DockEntry;
import com.ibasco.sourcebuddy.domain.DockLayoutEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class DockLayoutEntryComparator implements Comparator<DockLayoutEntry> {

    private static final Logger log = LoggerFactory.getLogger(DockLayoutEntryComparator.class);

    @Override
    public int compare(DockLayoutEntry o1, DockLayoutEntry o2) {
        if (o1 == o2)
            return 0;

        if (o1 != null && DockConfig.DOCK_SERVER_BROWSER.equals(o1.getFrom().getId())) {
            return -1;
        } else if (o2 != null && DockConfig.DOCK_SERVER_BROWSER.equals(o2.getFrom().getId())) {
            return 1;
        }

        if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else {
            DockEntry to1 = o1.getTo();
            DockEntry to2 = o2.getTo();

            if (to1 == null && to2 == null) {
                return -1;
            } else if (to1 == null) {
                return -1;
            } else if (to2 == null) {
                return 1;
            } else {
                String fromId1 = o1.getFrom().getId();
                String toId1 = o1.getTo().getId();
                String fromId2 = o2.getFrom().getId();
                String toId2 = o2.getTo().getId();

                if (fromId1.equals(toId2)) {
                    return -1;
                }
                return 1;
            }
        }
    }
}
