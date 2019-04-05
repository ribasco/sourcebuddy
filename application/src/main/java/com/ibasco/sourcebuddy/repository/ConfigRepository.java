package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.GlobalConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConfigRepository extends JpaRepository<GlobalConfig, UUID> {

}
