package com.ibasco.sourcebuddy.gui.listeners;

import com.ibasco.sourcebuddy.service.AppService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Scope(SCOPE_PROTOTYPE)
public class RunnableTextChangeListener implements ChangeListener<String> {

    private final Runnable action;

    private AppService appService;

    private AtomicBoolean activated = new AtomicBoolean();

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RunnableTextChangeListener(Runnable action) {
        this.action = wrapAction(action);
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (!activated.getAndSet(true)) {
            appService.runAfter(Duration.ofMillis(1000), action);
            return;
        }
        appService.reset(action);
    }

    private Runnable wrapAction(Runnable action) {
        return () -> {
            try {
                action.run();
            } finally {
                activated.set(false);
            }
        };
    }

    @Autowired
    public void setAppService(AppService appService) {
        this.appService = appService;
    }
}
