package service.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import service.server.GsonAdapters;

import java.time.Duration;
import java.time.LocalDateTime;

public class GsonConfig {
    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new GsonAdapters.LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new GsonAdapters.DurationAdapter())
                .serializeNulls()
                .create();
    }
}
