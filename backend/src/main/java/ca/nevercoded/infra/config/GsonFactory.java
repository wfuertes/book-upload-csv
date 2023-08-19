package ca.nevercoded.infra.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;

public class GsonFactory {

    public static Gson create() {
        final var builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        return builder.create();
    }
}
