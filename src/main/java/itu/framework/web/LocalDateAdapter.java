package itu.framework.web;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * TypeAdapter personnalisé pour désérialiser les objets LocalDate avec GSON
 * Permet la conversion automatique des chaînes au format ISO (yyyy-MM-dd) en LocalDate
 */
public class LocalDateAdapter extends TypeAdapter<LocalDate> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(formatter.format(value));
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return LocalDate.parse(value, formatter);
    }
}
