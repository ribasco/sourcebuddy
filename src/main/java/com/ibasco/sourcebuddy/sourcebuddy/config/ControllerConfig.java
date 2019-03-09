package com.ibasco.sourcebuddy.sourcebuddy.config;

import com.ibasco.sourcebuddy.sourcebuddy.controllers.MainController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ControllerConfig {

    @Bean
    public MainController mainController() {
        return new MainController();
    }
}
