package it.cnr.ilc.lari.itant.cash.om;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class MetadataSerializer extends JsonSerializer<Map<String, String>> {
    @Override
    public void serialize(Map<String, String> value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
                gen.writeStartArray();
                for (String key: value.keySet()) {
                    gen.writeStartObject();
                    gen.writeStringField("key-meta", key);
                    gen.writeStringField("value-meta", value.get(key));
                    gen.writeEndObject();
                }
                gen.writeEndArray();
    }
}
