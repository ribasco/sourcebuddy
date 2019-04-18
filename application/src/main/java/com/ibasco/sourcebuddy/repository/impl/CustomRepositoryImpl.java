package com.ibasco.sourcebuddy.repository.impl;

import com.ibasco.sourcebuddy.repository.CustomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.Serializable;

public class CustomRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements CustomRepository<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(CustomRepositoryImpl.class);

    private final EntityManager entityManager;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public CustomRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public T refresh(T t) {
        if (!entityManager.contains(t)) {
            t = entityManager.merge(t);
            log.debug("refresh() :: merging entity to current persistence context ({})", t);
        }
        entityManager.refresh(t);
        return t;
    }
}
