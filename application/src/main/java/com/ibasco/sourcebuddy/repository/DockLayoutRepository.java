package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.ConfigProfile;
import com.ibasco.sourcebuddy.domain.DockLayout;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DockLayoutRepository extends CustomRepository<DockLayout, Integer> {

    @Query("select a from DockLayout a where a.profile = :profile")
    List<DockLayout> findByProfile(@Param("profile") ConfigProfile profile);
}
