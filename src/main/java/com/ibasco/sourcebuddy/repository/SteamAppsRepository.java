package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.SteamApp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SteamAppsRepository extends JpaRepository<SteamApp, Integer> {

}
