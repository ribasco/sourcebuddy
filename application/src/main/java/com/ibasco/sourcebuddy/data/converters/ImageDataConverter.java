package com.ibasco.sourcebuddy.data.converters;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import java.io.ByteArrayInputStream;

public class ImageDataConverter implements AttributeConverter<Image, byte[]> {

    private static final Logger log = LoggerFactory.getLogger(ImageDataConverter.class);

    @Override
    public byte[] convertToDatabaseColumn(Image img) {
        if (img == null) {
            return null;
        }
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        byte[] buf = new byte[w * h * 4];
        if (buf.length <= 0)
            return null;
        img.getPixelReader().getPixels(0, 0, w, h, PixelFormat.getByteBgraInstance(), buf, 0, w * 4);
        return buf;
    }

    @Override
    public Image convertToEntityAttribute(byte[] dbData) {
        if (dbData == null)
            return null;
        return new Image(new ByteArrayInputStream(dbData));
    }
}
