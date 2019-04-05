package com.ibasco.sourcebuddy.tasks;

import org.springframework.context.annotation.Scope;

@Scope("prototype")
public class UpdateSteamAppsTask extends BaseTask<Void> {

    @Override
    protected Void process() throws Exception {
        return null;
    }
}
