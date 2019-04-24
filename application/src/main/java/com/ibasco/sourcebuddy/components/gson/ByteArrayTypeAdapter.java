package com.ibasco.sourcebuddy.components.gson;

import com.google.gson.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class ByteArrayTypeAdapter implements JsonTypeAdapter<byte[]> {

    @Override
    public Class<byte[]> getType() {
        return byte[].class;
    }

    @Override
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Base64.decodeBase64(json.getAsString());
    }

    @Override
    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(Base64.encodeBase64String(src));
    }
}
