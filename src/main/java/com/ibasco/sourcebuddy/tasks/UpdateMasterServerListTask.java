package com.ibasco.sourcebuddy.tasks;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UpdateMasterServerListTask extends BaseTask<Void> {

    @Override
    protected Void call() throws Exception {
        return null;
    }
}
