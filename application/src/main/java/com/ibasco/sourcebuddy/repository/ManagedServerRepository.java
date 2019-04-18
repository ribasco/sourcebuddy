package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.ManagedServer;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManagedServerRepository extends CustomRepository<ManagedServer, UUID> {

    @Query("select a from ManagedServer a where a.serverDetails = :server")
    Optional<ManagedServer> findByServer(@Param("server") ServerDetails server);

    @Query("select a from ManagedServer a inner join fetch a.serverDetails d where d.steamApp = :app")
    List<ManagedServer> findByApp(@Param("app") SteamApp app);
}
