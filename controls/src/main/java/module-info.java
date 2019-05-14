module sourcebuddy.controls {
    exports com.ibasco.sourcebuddy.controls to sourcebuddy.app, javafx.graphics, javafx.fxml, javafx.media, javafx.web, javafx.swing;
    requires javafx.base;
    requires javafx.controls;
    requires slf4j.api;
    //requires org.slf4j;
    requires org.controlsfx.controls;
}