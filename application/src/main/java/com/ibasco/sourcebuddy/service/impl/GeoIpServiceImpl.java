package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.sourcebuddy.repository.CountryRepository;
import com.ibasco.sourcebuddy.service.GeoIpService;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.record.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetSocketAddress;

@Transactional
public class GeoIpServiceImpl implements GeoIpService {

    private static final Logger log = LoggerFactory.getLogger(GeoIpServiceImpl.class);

    private DatabaseReader geoIpDatabaseReader;

    private CountryRepository countryRepository;

    @Override
    public Country findCountry(InetSocketAddress address) {
        try {
            return geoIpDatabaseReader.city(address.getAddress()).getCountry();
        } catch (IOException | GeoIp2Exception e) {
            log.debug("Could not obtain geoip location of address {}", address);
        }
        return null;
    }

    @Override
    public Country refreshCountryRepository(InetSocketAddress address) {
        return null;
    }

    @Autowired
    public void setGeoIpDatabaseReader(DatabaseReader geoIpDatabaseReader) {
        this.geoIpDatabaseReader = geoIpDatabaseReader;
    }

    @Autowired
    public void setCountryRepository(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }
}
