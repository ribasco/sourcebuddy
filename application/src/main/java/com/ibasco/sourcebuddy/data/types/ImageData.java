package com.ibasco.sourcebuddy.data.types;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

public class ImageData implements Serializable {

    private transient Image data;

    private byte[] buffer;

    public ImageData(byte[] buffer) {
        this.buffer = buffer;
        data = new Image(new ByteArrayInputStream(buffer));
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public Image getImage() {
        return data;
    }
}
