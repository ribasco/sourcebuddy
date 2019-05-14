package com.ibasco.sourcebuddy.components;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleBuffer {

    private static final Logger log = LoggerFactory.getLogger(ConsoleBuffer.class);

    private StringProperty buffer = new SimpleStringProperty() {

        private boolean changing = false;

        @Override
        protected void invalidated() {
            if (changing || get() == null)
                return;
            changing = true;
            try {
                String data = get();
                int max = bufferSize.get();
                int length = data.length();
                if (length > max) {
                    log.warn("Buffer size has reached it's limit (Max: {}, Actual: {})", max, length);
                    String trimmedVersion = data.substring(length - max, length - 1);
                    log.info("Trimmed version: {}", trimmedVersion);
                    set(trimmedVersion);
                }
            } finally {
                changing = false;
            }
        }
    };

    private IntegerProperty bufferSize = new SimpleIntegerProperty(512000);

    public ConsoleBuffer(int bufferSize) {
        setBufferSize(bufferSize);
    }

    public void clear() {
        buffer.set(null);
    }

    public String getBuffer() {
        return buffer.get();
    }

    public StringProperty bufferProperty() {
        return buffer;
    }

    public void setBuffer(String buffer) {
        this.buffer.set(buffer);
    }

    public int getBufferSize() {
        return bufferSize.get();
    }

    public IntegerProperty bufferSizeProperty() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize.set(bufferSize);
    }
}
