package com.ibasco.sourcebuddy.domain;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
abstract public class AuditableEntity<T> implements Serializable {

    private T createdBy;

    private T lastModifiedBy;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    @CreatedBy
    @Column(name = "create_user")
    public T getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(T createdBy) {
        this.createdBy = createdBy;
    }

    @LastModifiedBy
    @Column(name = "update_user")
    public T getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(T lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @CreatedDate
    @Column(name = "create_date")
    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    @LastModifiedDate
    @Column(name = "update_date")
    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }
}
