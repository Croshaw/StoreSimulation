package me.ero.storesimulationapp.simulation.store_api.util.adapters;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeTypeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

    @Override
    public JsonElement serialize(final LocalTime time, final Type typeOfSrc,
                                 final JsonSerializationContext context) {
        return new JsonPrimitive(time.format(formatter));
    }

    @Override
    public LocalTime deserialize(final JsonElement json, final Type typeOfT,
                                 final JsonDeserializationContext context) throws JsonParseException {
        return LocalTime.parse(json.getAsString(), formatter);
    }
}