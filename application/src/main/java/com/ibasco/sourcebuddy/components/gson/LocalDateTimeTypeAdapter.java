package com.ibasco.sourcebuddy.components.gson;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
public class LocalDateTimeTypeAdapter implements JsonTypeAdapter<LocalDateTime> {

    private static final Logger log = LoggerFactory.getLogger(LocalDateTimeTypeAdapter.class);

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return LocalDateTime.ofEpochSecond(json.getAsLong(), 0, ZoneOffset.UTC);
    }

    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        ZoneId zoneId = ZoneId.systemDefault();
        long epoch = src.atZone(zoneId).toEpochSecond();
        return new JsonPrimitive(epoch);
    }

    @Override
    public Class<LocalDateTime> getType() {
        return LocalDateTime.class;
    }
}
