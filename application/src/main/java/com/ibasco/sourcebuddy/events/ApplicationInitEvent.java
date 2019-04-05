package com.ibasco.sourcebuddy.events;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class ApplicationInitEvent extends ApplicationEvent {

    private String message;

    private Stage stage;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source
     *         the object on which the event initially occurred (never {@code null})
     */
    public ApplicationInitEvent(Object source, Stage stage) {
        super(source);
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
