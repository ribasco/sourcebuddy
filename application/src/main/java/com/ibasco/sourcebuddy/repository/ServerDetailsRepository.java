package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.util.Check;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServerDetailsRepository extends JpaRepository<ServerDetails, UUID> {

    default Optional<ServerDetails> findByAddress(InetSocketAddress address) {
        Check.requireNonNull(address, "Address cannot be null");
        return findByAddress(address.getAddress().getHostAddress(), address.getPort());
    }

    @Query("select a from ServerDetails  a where a.ipAddress = :addr and a.port = :port")
    Optional<ServerDetails> findByAddress(@Param("addr") String ipAddress, @Param("port") int port);

    @Query("select a from ServerDetails a where a.steamApp = :steamApp")
    List<ServerDetails> findBySteamApp(@Param("steamApp") SteamApp steamApp);

    @Query("select a from ServerDetails a where a.bookmarked = 1 and a.steamApp = :steamApp")
    List<ServerDetails> findBookmarksByApp(@Param("steamApp") SteamApp steamApp);

    @Query("select distinct a.steamApp from ServerDetails a where a.bookmarked = true")
    List<SteamApp> findBookmarkedSteamApps();

    @Query("select a from ServerDetails a where a.bookmarked = 1")
    List<ServerDetails> findBookmarkedServers();

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ServerDetails c WHERE c.bookmarked = 1")
    boolean isBookmarked(ServerDetails server);

    @Query("select new java.net.InetSocketAddress(a.ipAddress, a.port) from ServerDetails a where a.steamApp = :steamApp")
    List<InetSocketAddress> findAddressListByApp(@Param("steamApp") SteamApp steamApp);

    @Query("select count(a) from ServerDetails a where a.steamApp = :steamApp")
    long countByApp(@Param("steamApp") SteamApp app);
}
