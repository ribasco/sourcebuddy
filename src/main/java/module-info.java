module sourcebuddy {
    exports com.ibasco.sourcebuddy.sourcebuddy;
    opens com.ibasco.sourcebuddy.sourcebuddy to spring.core;
    opens com.ibasco.sourcebuddy.sourcebuddy.config to spring.core, spring.beans, spring.context;

    requires javafx.graphics;
    requires javafx.fxml;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires javafx.controls;
    requires logback.classic;
    requires slf4j.api;
}