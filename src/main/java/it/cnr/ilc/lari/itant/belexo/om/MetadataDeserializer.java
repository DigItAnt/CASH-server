package it.cnr.ilc.lari.itant.belexo.om;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class MetadataDeserializer extends JsonDeserializer<Map<String, String>> {

    @Override
    public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        // TODO Auto-generated method stub
        return null;
    }
}