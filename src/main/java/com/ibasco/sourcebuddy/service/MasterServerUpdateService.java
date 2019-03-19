package com.ibasco.sourcebuddy.service;

import com.ibasco.agql.protocols.valve.steam.master.MasterServerFilter;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerRegion;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.stream.Collectors;


public class MasterServerUpdateService extends ScheduledService<ObservableList<InetSocketAddress>> {

    private static final Logger log = LoggerFactory.getLogger(MasterServerUpdateService.class);

    private MasterServerQueryClient masterQueryClient;

    @Override
    protected Task<ObservableList<InetSocketAddress>> createTask() {
        return new Task<>() {
            @Override
            protected ObservableList<InetSocketAddress> call() throws Exception {
                MasterServerFilter filter = MasterServerFilter.create().dedicated(true).appId(550);
                log.info("Retrieving servers from master server (Filter: {})", filter.toString());
                //AtomicInteger ctr = new AtomicInteger();
                ObservableList<InetSocketAddress> res = null;
                try {
                    res = masterQueryClient.getServerList(MasterServerType.SOURCE, MasterServerRegion.REGION_ALL, filter, (inetSocketAddress, inetSocketAddress2, ex) -> {
                        if (ex != null) {
                            log.error("Error", ex);
                            return;
                        }
                        //log.info("IP: {}", inetSocketAddress);
                        //ctr.incrementAndGet();
                    }).join().stream().collect(Collectors.toCollection(FXCollections::observableArrayList));
                    log.info("Got total of {} servers from master", res.size());
                } catch (Exception e) {
                    log.error("Error retrieving master server list", e);
                }
                return res;
            }
        };
    }

    @Autowired
    public void setMasterQueryClient(MasterServerQueryClient masterQueryClient) {
        this.masterQueryClient = masterQueryClient;
    }
}
