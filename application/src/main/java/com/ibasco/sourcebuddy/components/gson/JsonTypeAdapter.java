package com.ibasco.sourcebuddy.components.gson;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public interface JsonTypeAdapter<T> extends JsonSerializer<T>, JsonDeserializer<T> {

    Class<T> getType();
}
