package com.ibasco.sourcebuddy.service;

import com.ibasco.sourcebuddy.domain.ConfigProfile;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Function;

public interface ConfigService {

    ConfigProfile getDefaultProfile();

    List<ConfigProfile> getProfiles();

    void saveGlobalConfig(String key, Object value);

    default String getGlobalConfig(String key) {
        return getGlobalConfig(key, null);
    }

    default <T> T getMappedGlobalConfig(String key, Function<String, T> mapper) {
        return getMappedGlobalConfig(key, null, mapper);
    }

    default <T> T getMappedGlobalConfig(String key, T defaultValue, Function<String, T> mapper) {
        String value = getGlobalConfig(key);
        if (StringUtils.isBlank(value))
            return defaultValue;
        return mapper.apply(value);
    }

    String getGlobalConfig(String key, String defaultValue);

    ConfigProfile saveProfile(ConfigProfile profile);

    void deleteProfile(ConfigProfile profile);

    ConfigProfile createProfile();

    void setDefaultProfile(ConfigProfile config);

    boolean isDefaultProfile(ConfigProfile profile);

    ConfigProfile refresh(ConfigProfile profile);
}
