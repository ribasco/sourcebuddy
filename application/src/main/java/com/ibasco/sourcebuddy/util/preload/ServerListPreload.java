package com.ibasco.sourcebuddy.util.preload;

import com.ibasco.sourcebuddy.annotations.PreloadOrder;
import com.ibasco.sourcebuddy.util.PreloadTask;

@PreloadOrder(2)
public class ServerListPreload extends PreloadTask {

    @Override
    public void preload() throws Exception {
        updateMessage("Preloading server list");
        /*for (double i = 0.0; i < 1.0; i += 0.001) {
            updateMessage("Item: " + i);
            updateProgress(i);
            Thread.sleep(50);
        }*/
    }
}
