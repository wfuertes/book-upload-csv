package ca.nevercoded.infra.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateSerializer implements JsonSerializer<LocalDate> {

    @Override
    public JsonElement serialize(LocalDate source, Type type, JsonSerializationContext context) {
        final var localDate = source.format(DateTimeFormatter.ISO_DATE);
        return new JsonPrimitive(localDate);
    }
}
