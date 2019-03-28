package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

public interface ServerDetailsRepository extends JpaRepository<ServerDetails, UUID> {

    @Query("select a from ServerDetails a where a.steamApp = :steamApp")
    List<ServerDetails> findBySteamApp(@Param("steamApp") SteamApp steamApp);

    @Query("select a from ServerDetails a where a.bookmarked = 1 and a.steamApp = :steamApp")
    List<ServerDetails> findBookmarksByApp(@Param("steamApp") SteamApp steamApp);

    @Query("select distinct a.steamApp from ServerDetails a where a.bookmarked = 1")
    List<SteamApp> findBookmarkedSteamApps();

    @Query("select new java.net.InetSocketAddress(a.ipAddress, a.port) from ServerDetails a where a.steamApp = :steamApp")
    List<InetSocketAddress> findAddressListByApp(@Param("steamApp") SteamApp steamApp);
}
