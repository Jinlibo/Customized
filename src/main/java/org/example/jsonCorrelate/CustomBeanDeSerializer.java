package org.example.jsonCorrelate;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;

public class CustomBeanDeSerializer extends JsonDeserializer<Object> implements ContextualDeserializer {
    @Override
    public Object deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JacksonException {
        return parser.currentValue();
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) throws JsonMappingException {
        if (property.getAnnotation(CustomJsonSerDeSer.class) != null) {
            return this;
        }
        return ctx.findContextualValueDeserializer(property.getType(), property);
    }
}
