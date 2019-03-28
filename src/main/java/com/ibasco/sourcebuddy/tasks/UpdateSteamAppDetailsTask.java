package com.ibasco.sourcebuddy.tasks;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(scopeName = "prototype")
public class UpdateSteamAppDetailsTask extends BaseTask<Void> {

    @Override
    protected Void process() throws Exception {
        return null;
    }
}
