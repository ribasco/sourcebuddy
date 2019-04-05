package com.ibasco.sourcebuddy.repository;

import com.ibasco.sourcebuddy.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, String> {

}
