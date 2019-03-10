module sourcebuddy {
    exports com.ibasco.sourcebuddy.sourcebuddy;
    opens com.ibasco.sourcebuddy.sourcebuddy to spring.core;
    opens com.ibasco.sourcebuddy.sourcebuddy.config to spring.core, spring.beans, spring.context;
    opens com.ibasco.sourcebuddy.sourcebuddy.controllers to javafx.fxml, spring.beans;
    opens com.ibasco.sourcebuddy.sourcebuddy.model to javafx.base;

    requires javafx.graphics;
    requires javafx.fxml;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires javafx.controls;
    requires logback.classic;
    requires slf4j.api;
    requires agql.source.query;
    requires agql.steam.master;
    requires gson;
    requires spring.beans;
    requires agql.lib.core;
}