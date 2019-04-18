package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.domain.SteamAppDetails;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SteamAppDetailsRepository extends CustomRepository<SteamAppDetails, Integer> {

    @Query("select a from SteamAppDetails a where a.steamApp = :steamApp")
    Optional<SteamAppDetails> findByApp(@Param("steamApp") SteamApp app);
}
