package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.SteamApp;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SteamAppsRepository extends JpaRepository<SteamApp, Integer> {

    @Modifying
    @Query("update SteamApp a set a.bookmarked = :bookmarkVal where a = :app")
    int updateBookmark(@Param("app") SteamApp app, @Param("bookmarkVal") Boolean value);

    @Query("select a from SteamApp a left outer join SteamAppDetails d on a.id = d.steamApp where d.emptyDetails = false or d.emptyDetails is null")
    List<SteamApp> findSteamAppListNonEmpty(Sort sort);
}
