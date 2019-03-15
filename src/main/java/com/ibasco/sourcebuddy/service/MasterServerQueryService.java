package com.ibasco.sourcebuddy.service;

import com.ibasco.agql.protocols.valve.steam.master.MasterServerFilter;
import com.ibasco.agql.protocols.valve.steam.master.client.MasterServerQueryClient;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerRegion;
import com.ibasco.agql.protocols.valve.steam.master.enums.MasterServerType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.stream.Collectors;

@Service
public class MasterServerQueryService extends ScheduledService<ObservableList<InetSocketAddress>> {

    private MasterServerQueryClient masterQueryClient;

    @Override
    protected Task<ObservableList<InetSocketAddress>> createTask() {
        return new Task<>() {
            @Override
            protected ObservableList<InetSocketAddress> call() throws Exception {
                MasterServerFilter filter = MasterServerFilter.create().dedicated(true).appId(550);
                return masterQueryClient.getServerList(MasterServerType.SOURCE, MasterServerRegion.REGION_ALL, filter).join().stream()
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));
            }
        };
    }

    @Autowired
    public void setMasterQueryClient(MasterServerQueryClient masterQueryClient) {
        this.masterQueryClient = masterQueryClient;
    }
}
