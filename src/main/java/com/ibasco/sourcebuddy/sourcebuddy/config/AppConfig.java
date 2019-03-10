package com.ibasco.sourcebuddy.sourcebuddy.config;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.util.Scanner;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    private static class PublicIp {
        private String ip;

        private String getIp() {
            return ip;
        }
    }

    @Value("${publicIpApiEndpoint}")
    private String publicIpApiEndpoint;

    @Bean("gsonProvider")
    public Gson gsonProvider() {
        return new Gson();
    }

    @Bean("publicIp")
    public InetAddress retrievePublicIp() {
        try {
            log.info("Retrieving public ip from {}", publicIpApiEndpoint);
            var httpClient = (HttpURLConnection) URI.create(publicIpApiEndpoint).toURL().openConnection();
            httpClient.setRequestMethod("GET");
            var httpResponse = httpClient.getInputStream();
            var scn = new Scanner(httpResponse);
            var json_sb = new StringBuilder();
            while (scn.hasNext()) {
                json_sb.append(scn.next());
            }
            PublicIp publicIp = gsonProvider().fromJson(json_sb.toString(), PublicIp.class);
            return InetAddress.getByName(publicIp.getIp());
        } catch (IOException e) {
            log.error("Error during http client initialization", e);
        }
        return null;
    }
}
