package com.ibasco.sourcebuddy.repository.impl;

import com.ibasco.sourcebuddy.repository.CustomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

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
    public T refresh(T t) {
        t = merge(t);
        entityManager.refresh(t);
        return t;
    }

    @Override
    public T merge(T t) {
        if (t == null)
            return null;
        if (!entityManager.contains(t)) {
            t = entityManager.merge(t);
        }
        return t;
    }

    @Override
    public void detach(T t) {
        entityManager.detach(t);
    }

    @Override
    public boolean isAttached(T t) {
        return entityManager.contains(t);
    }

}
