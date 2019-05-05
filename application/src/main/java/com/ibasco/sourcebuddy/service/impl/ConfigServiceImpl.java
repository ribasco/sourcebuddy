package com.ibasco.sourcebuddy.service.impl;

import com.ibasco.sourcebuddy.domain.ConfigGlobal;
import com.ibasco.sourcebuddy.domain.ConfigProfile;
import com.ibasco.sourcebuddy.repository.ConfigGlobalRepository;
import com.ibasco.sourcebuddy.repository.ConfigProfileRepository;
import com.ibasco.sourcebuddy.service.ConfigService;
import com.ibasco.sourcebuddy.util.Check;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ConfigServiceImpl implements ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigServiceImpl.class);

    private ConfigProfileRepository configRepository;

    private ConfigGlobalRepository configGlobalRepository;

    private static final String DEFAULT_PROFILE = "DEFAULT_PROFILE";

    @PostConstruct
    private void init() {
        ConfigProfile defaultProfile = getDefaultProfile();
        if (defaultProfile == null) {
            log.debug("No default profile assigned. Creating new default profile");
            ConfigProfile profile = createProfile();
            profile.setName("Default");
            defaultProfile = saveProfile(profile);
            setDefaultProfile(defaultProfile);
            log.debug("Saved default profile: {}", defaultProfile);
        }
    }

    @Override
    public List<ConfigProfile> getProfiles() {
        return configRepository.findAll();
    }

    @Override
    public void saveGlobalConfig(String key, Object value) {
        ConfigGlobal config;
        Optional<ConfigGlobal> res = configGlobalRepository.findById(key);
        if (res.isPresent()) {
            config = res.get();
            config.setValue(value != null ? value.toString() : null);
        } else {
            config = new ConfigGlobal(key, value);
        }
        configGlobalRepository.saveAndFlush(config);
    }

    @Override
    public String getGlobalConfig(String key, String defaultValue) {
        Optional<ConfigGlobal> res = configGlobalRepository.findById(key);
        if (res.isPresent()) {
            return res.map(ConfigGlobal::getValue).orElse(defaultValue);
        }
        return null;
    }

    @Override
    public List<ConfigGlobal> getConfigGlobal() {
        return configGlobalRepository.findAll();
    }

    @Override
    public ConfigProfile saveProfile(ConfigProfile config) {
        return configRepository.save(Check.requireNonNull(config, "profile cannot be null"));
    }

    @Override
    public void deleteProfile(ConfigProfile profile) {
        configRepository.delete(profile);
    }

    @Override
    public ConfigProfile createProfile() {
        return new ConfigProfile();
    }

    @Override
    public void setDefaultProfile(ConfigProfile config) {
        ConfigGlobal global = configGlobalRepository.findById(DEFAULT_PROFILE).orElseGet(ConfigGlobal::new);
        if (StringUtils.isBlank(global.getKey()))
            global.setKey(DEFAULT_PROFILE);
        global.setValue(String.valueOf(config.getId()));
        configGlobalRepository.saveAndFlush(global);
    }

    @Override
    public boolean isDefaultProfile(ConfigProfile profile) {
        Check.requireNonNull(profile, "Profile argument cannot be null");
        Optional<ConfigGlobal> res = configGlobalRepository.findById(DEFAULT_PROFILE);
        if (res.isPresent() && !StringUtils.isBlank(res.get().getValue()) && StringUtils.isNumeric(res.get().getValue())) {
            int profileId = Integer.valueOf(res.get().getValue());
            return profileId == profile.getId();
        }
        return false;
    }

    @Override
    public ConfigProfile refresh(ConfigProfile profile) {
        return configRepository.refresh(profile);
    }

    @Override
    public ConfigProfile getDefaultProfile() {
        int defaultProfileId = getMappedGlobalConfig(DEFAULT_PROFILE, -1, Integer::valueOf);
        if (defaultProfileId > -1) {
            Optional<ConfigProfile> profileRes = configRepository.findById(defaultProfileId);
            if (profileRes.isPresent())
                return profileRes.get();
        }
        //Check if we have available profiles
        for (ConfigProfile profile : configRepository.findAll(Sort.by("id"))) {
            if (!isDefaultProfile(profile)) {
                setDefaultProfile(profile);
                return profile;
            }
        }
        return null;
    }

    @Autowired
    public void setConfigRepository(ConfigProfileRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Autowired
    public void setConfigGlobalRepository(ConfigGlobalRepository configGlobalRepository) {
        log.info("Autowiring config global repo");
        this.configGlobalRepository = configGlobalRepository;
    }
}
