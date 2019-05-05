package com.ibasco.sourcebuddy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface CustomRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    T refresh(T t);

    T merge(T t);

    void detach(T t);

    boolean isAttached(T t);
}