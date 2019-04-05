package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.ManagedGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ManagedGameRepository extends JpaRepository<ManagedGame, UUID> {

}
