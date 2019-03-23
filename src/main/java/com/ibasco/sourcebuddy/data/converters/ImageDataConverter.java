package com.ibasco.sourcebuddy.data.converters;

import com.ibasco.sourcebuddy.data.types.ImageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;

public class ImageDataConverter implements AttributeConverter<ImageData, byte[]> {

    private static final Logger log = LoggerFactory.getLogger(ImageDataConverter.class);

    @Override
    public byte[] convertToDatabaseColumn(ImageData attribute) {
        log.info("ImageDataConverter :: convertToDatabaseColumn");
        return attribute.getBuffer();
    }

    @Override
    public ImageData convertToEntityAttribute(byte[] dbData) {
        log.info("ImageDataConverter :: convertToEntityAttribute");
        return new ImageData(dbData);
    }
}
