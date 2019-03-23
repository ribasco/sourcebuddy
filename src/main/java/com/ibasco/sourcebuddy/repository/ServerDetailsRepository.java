package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.domain.SteamApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ServerDetailsRepository extends JpaRepository<ServerDetails, UUID> {

    @Query("select a from ServerDetails a where a.steamApp = :steamApp")
    List<ServerDetails> findBySteamApp(@Param("steamApp") SteamApp steamApp);
}
