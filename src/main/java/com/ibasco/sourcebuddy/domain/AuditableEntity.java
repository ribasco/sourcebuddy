package com.ibasco.sourcebuddy.domain;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
abstract public class AuditableEntity<T> {

    private ObjectProperty<LocalDateTime> createdDate = new SimpleObjectProperty<>();

    private StringProperty createUser = new SimpleStringProperty();

    private StringProperty updateUser = new SimpleStringProperty();

    private ObjectProperty<LocalDateTime> updateDate = new SimpleObjectProperty<>();

    @CreatedDate
    @Column(name = "create_date")
    public LocalDateTime getCreatedDate() {
        return createdDate.get();
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate.set(createdDate);
    }

    public ObjectProperty<LocalDateTime> createdDateProperty() {
        return createdDate;
    }

    @CreatedBy
    @Column(name = "create_user")
    public String getCreateUser() {
        return createUser.get();
    }

    public void setCreateUser(String createUser) {
        this.createUser.set(createUser);
    }

    public StringProperty createUserProperty() {
        return createUser;
    }

    @LastModifiedBy
    @Column(name = "update_user")
    public String getUpdateUser() {
        return updateUser.get();
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser.set(updateUser);
    }

    public StringProperty updateUserProperty() {
        return updateUser;
    }

    @LastModifiedDate
    @Column(name = "update_date")
    public LocalDateTime getUpdateDate() {
        return updateDate.get();
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate.set(updateDate);
    }

    public ObjectProperty<LocalDateTime> updateDateProperty() {
        return updateDate;
    }
}
