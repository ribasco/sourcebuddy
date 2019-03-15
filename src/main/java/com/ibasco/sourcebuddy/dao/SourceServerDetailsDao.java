package com.ibasco.sourcebuddy.dao;

import com.ibasco.sourcebuddy.model.SourceServerDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SourceServerDetailsDao extends JpaRepository<SourceServerDetails, UUID> {

}
