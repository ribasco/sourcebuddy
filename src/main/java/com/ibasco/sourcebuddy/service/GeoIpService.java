package com.ibasco.sourcebuddy.service;

import com.maxmind.geoip2.record.Country;

import java.net.InetSocketAddress;

public interface GeoIpService {

    Country findCountryByAddress(InetSocketAddress address);

    Country findAndUpdateRepository(InetSocketAddress address);
}
