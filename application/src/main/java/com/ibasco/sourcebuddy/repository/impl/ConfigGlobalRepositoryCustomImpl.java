package com.ibasco.sourcebuddy.repository.impl;

import com.ibasco.sourcebuddy.domain.ConfigGlobal;
import com.ibasco.sourcebuddy.repository.ConfigGlobalRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class ConfigGlobalRepositoryCustomImpl implements ConfigGlobalRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void saveKeyValue(String key, Object value) {
        String query = String.format("MERGE INTO %s (%s, %s) VALUES (?, ?)", ConfigGlobal.TABLE_NAME, ConfigGlobal.KEY, ConfigGlobal.VALUE);
        entityManager.createNativeQuery(query)
                .setParameter(1, key)
                .setParameter(2, value)
                .executeUpdate();
    }
}
