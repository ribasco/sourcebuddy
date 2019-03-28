package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SteamAppDetailsRepository extends JpaRepository<SteamAppDetails, UUID> {

    @Query("select a from SteamAppDetails a where a.steamApp = :steamApp")
    Optional<SteamAppDetails> findByApp(@Param("steamApp") SteamApp app);
}
