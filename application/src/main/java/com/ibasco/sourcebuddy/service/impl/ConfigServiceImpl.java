package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.sourcebuddy.domain.GlobalConfig;
import com.ibasco.sourcebuddy.repository.ConfigRepository;
import com.ibasco.sourcebuddy.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ConfigServiceImpl implements ConfigService {

    private ConfigRepository configRepository;

    @Override
    public GlobalConfig getDefaultConfig() {
        return null;
    }

    @Override
    public List<GlobalConfig> getAllConfigs() {
        return null;
    }

    @Override
    public void saveConfig(GlobalConfig config) {

    }

    @Override
    public GlobalConfig newConfig() {
        return null;
    }

    @Override
    public void setDefault(GlobalConfig config, boolean value) {

    }

    @Autowired
    public void setConfigRepository(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }
}
