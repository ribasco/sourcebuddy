package com.ibasco.sourcebuddy.components.gson;

import com.google.gson.*;
import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.service.SteamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class SteamAppTypeAdapter implements JsonTypeAdapter<SteamApp> {

    private static final Logger log = LoggerFactory.getLogger(SteamAppTypeAdapter.class);

    private SteamService steamService;

    @Override
    public SteamApp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        int appId = json.getAsInt();
        return steamService.findSteamAppById(appId).orElse(new SteamApp(appId, null));
    }

    @Override
    public JsonElement serialize(SteamApp src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getId());
    }

    @Autowired
    public void setSteamService(SteamService steamService) {
        this.steamService = steamService;
    }

    @Override
    public Class<SteamApp> getType() {
        return SteamApp.class;
    }
}
