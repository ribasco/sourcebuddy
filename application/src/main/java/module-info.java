module sourcebuddy.app {
    exports com.ibasco.sourcebuddy;
    exports com.ibasco.sourcebuddy.events;
    exports com.ibasco.sourcebuddy.repository;
    exports com.ibasco.sourcebuddy.exceptions;
    exports com.ibasco.sourcebuddy.components.rcon;
    exports com.ibasco.sourcebuddy.components.rcon.parsers;
    exports com.ibasco.sourcebuddy.components.rcon.parsers.status;
    exports com.ibasco.sourcebuddy.domain to spring.beans, org.apache.commons.lang3, spring.data.commons;
    exports com.ibasco.sourcebuddy.service to spring.core, spring.beans, spring.aop;
    exports com.ibasco.sourcebuddy.components to spring.beans, spring.core;
    exports com.ibasco.sourcebuddy.util to spring.beans, spring.core;
    exports com.ibasco.sourcebuddy.model to spring.beans, spring.core;
    exports com.ibasco.sourcebuddy.enums to spring.data.jpa, spring.beans, spring.core;
    exports com.ibasco.sourcebuddy.service.impl to spring.beans, spring.core;
    exports com.ibasco.sourcebuddy.tasks to spring.beans, spring.core;
    exports com.ibasco.sourcebuddy.controllers to spring.context, spring.core, spring.beans;
    exports com.ibasco.sourcebuddy.util.preload to spring.beans, spring.context;
    exports com.ibasco.sourcebuddy.gui.tableview.cells to spring.beans, spring.context;
    exports com.ibasco.sourcebuddy.gui.tableview.factory to spring.beans, spring.context;
    exports com.ibasco.sourcebuddy.gui.treetableview.cells to spring.beans, spring.context;
    exports com.ibasco.sourcebuddy.gui.treetableview.factory to spring.beans, spring.context;
    exports com.ibasco.sourcebuddy.gui.decorators to spring.beans, spring.context;
    exports com.ibasco.sourcebuddy.controllers.fragments to spring.beans, spring.context;
    exports com.ibasco.sourcebuddy.repository.impl to spring.beans, spring.context, spring.data.commons;
    exports com.ibasco.sourcebuddy.components.gson to spring.beans, spring.context;
    exports com.ibasco.sourcebuddy.gui.listeners to spring.beans, spring.context;
    exports com.ibasco.sourcebuddy.util.converters to org.hibernate.orm.core;

    opens com.ibasco.sourcebuddy.repository.impl to spring.core;
    opens com.ibasco.sourcebuddy to spring.core;
    opens com.ibasco.sourcebuddy.config to spring.core, spring.beans, spring.context;
    opens com.ibasco.sourcebuddy.controllers to javafx.fxml, spring.beans, spring.core;
    opens com.ibasco.sourcebuddy.domain to javafx.base, spring.core, org.apache.commons.lang3, org.hibernate.orm.core;
    opens com.ibasco.sourcebuddy.components to spring.beans, spring.core;
    opens com.ibasco.sourcebuddy.service to spring.beans, spring.core, spring.aop;
    opens com.ibasco.sourcebuddy.service.impl to spring.core, spring.beans, spring.aop;
    opens com.ibasco.sourcebuddy.tasks to spring.core, spring.beans;
    opens com.ibasco.sourcebuddy.util to spring.core, spring.beans;
    opens com.ibasco.sourcebuddy.controllers.fragments to javafx.fxml;
    opens com.ibasco.sourcebuddy.gui.tableview.cells to spring.core, spring.beans;
    opens com.ibasco.sourcebuddy.model to spring.beans, spring.core, spring.context;
    opens com.ibasco.sourcebuddy.util.preload to spring.core;

    requires org.apache.commons.text;
    requires sourcebuddy.controls;
    requires java.net.http;
    requires com.jfoenix;
    requires com.h2database;
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
    //requires org.slf4j;
    //requires ch.qos.logback.core;
    //requires ch.qos.logback.classic;
    requires logback.core;
    requires slf4j.api;
    requires agql.steam.webapi;
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
    requires java.annotation;
    requires javax.inject;
    requires org.apache.commons.lang3;
    requires dockfx.clearcontrol;
    requires spring.data.commons;
    requires spring.core;
    requires jdk.internal.opt;
    requires richtextfx;
    requires fx.gson;
    requires org.apache.commons.codec;
    requires annotations;
    //requires dockfx;
}