package me.ero.storesimulationapp.simulation.store_api.util.adapters;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;

public class DurationTypeAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {

    @Override
    public JsonElement serialize(final Duration duration, final Type typeOfSrc,
                                 final JsonSerializationContext context) {
        return new JsonPrimitive(duration.toString());
    }

    @Override
    public Duration deserialize(final JsonElement json, final Type typeOfT,
                                final JsonDeserializationContext context) throws JsonParseException {
        return Duration.parse(json.getAsString());
    }
}