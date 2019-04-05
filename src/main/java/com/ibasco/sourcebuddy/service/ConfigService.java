package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.GlobalConfig;

import java.util.List;

public interface ConfigService {

    GlobalConfig getDefaultConfig();

    List<GlobalConfig> getAllConfigs();

    void saveConfig(GlobalConfig config);

    GlobalConfig newConfig();

    void setDefault(GlobalConfig config, boolean value);
}
