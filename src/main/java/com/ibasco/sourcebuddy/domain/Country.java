package com.ibasco.sourcebuddy.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = Country.TABLE_NAME)
@EntityListeners(AuditingEntityListener.class)
public class Country extends AuditableEntity {

    public static final String TABLE_NAME = "SB_COUNTRY";

    public static final String COUNTRY_CODE = "COUNTRY_CODE";

    public static final String COUNTRY_NAME = "NAME";

    private StringProperty countryCode = new SimpleStringProperty();

    private StringProperty countryName = new SimpleStringProperty();

    @Id
    @Column(name = COUNTRY_CODE, nullable = false, unique = true, updatable = false)
    public String getCountryCode() {
        return countryCode.get();
    }

    public void setCountryCode(String countryCode) {
        this.countryCode.set(countryCode);
    }

    public StringProperty countryCodeProperty() {
        return countryCode;
    }

    @Column(name = COUNTRY_NAME, nullable = false, unique = true, updatable = false)
    public String getCountryName() {
        return countryName.get();
    }

    public void setCountryName(String countryName) {
        this.countryName.set(countryName);
    }

    public StringProperty countryNameProperty() {
        return countryName;
    }
}
