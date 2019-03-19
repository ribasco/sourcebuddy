module sourcebuddy {
    exports com.ibasco.sourcebuddy;
    /*exports com.ibasco.sourcebuddy.enums;
    exports com.ibasco.sourcebuddy.exceptions;*/

    exports com.ibasco.sourcebuddy.util.dialect to org.hibernate.orm.core;
    exports com.ibasco.sourcebuddy.entities to org.hibernate.orm.core, spring.beans;
    exports com.ibasco.sourcebuddy.service to spring.beans;
    exports com.ibasco.sourcebuddy.components to spring.beans, spring.core;
    exports com.ibasco.sourcebuddy.util to spring.beans, spring.core;
    exports com.ibasco.sourcebuddy.model to spring.beans, spring.core;

    opens com.ibasco.sourcebuddy to spring.core;
    opens com.ibasco.sourcebuddy.config to spring.core, spring.beans, spring.context;
    opens com.ibasco.sourcebuddy.controllers to javafx.fxml, spring.beans, spring.core;
    opens com.ibasco.sourcebuddy.entities to javafx.base, spring.core;
    opens com.ibasco.sourcebuddy.components to spring.beans, spring.core;

    requires javafx.graphics;
    requires javafx.fxml;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.data.jpa;
    requires spring.batch.core;
    requires spring.batch.infrastructure;
    requires spring.jdbc;
    requires spring.orm;
    requires spring.tx;
    requires javafx.controls;
    requires logback.classic;
    requires slf4j.api;
    requires agql.source.query;
    requires agql.steam.master;
    requires gson;
    requires agql.lib.core;
    requires java.sql;
    requires org.hibernate.orm.core;
    requires net.bytebuddy;
    requires commons.dbcp2;
    requires java.management;
    requires java.persistence;
    requires java.naming;
    requires org.controlsfx.controls;
    requires geoip2;
    requires dockfx;
    requires java.annotation;
    requires org.apache.commons.lang3;
}