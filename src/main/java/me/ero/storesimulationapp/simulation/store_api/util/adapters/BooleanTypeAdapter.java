package me.ero.storesimulationapp.simulation.store_api.util.adapters;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;

public class BooleanTypeAdapter implements JsonSerializer<Boolean>, JsonDeserializer<Boolean> {

    @Override
    public JsonElement serialize(final Boolean bool, final Type typeOfSrc,
                                 final JsonSerializationContext context) {
        return new JsonPrimitive(bool.toString());
    }

    @Override
    public Boolean deserialize(final JsonElement json, final Type typeOfT,
                                final JsonDeserializationContext context) throws JsonParseException {
        return Boolean.getBoolean(json.getAsString());
    }
}