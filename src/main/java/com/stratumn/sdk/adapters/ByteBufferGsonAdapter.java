package com.stratumn.sdk.adapters;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Base64;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
/***
 *  Enables serialization of byte buffers  
 */
public class ByteBufferGsonAdapter implements JsonDeserializer<ByteBuffer>, JsonSerializer<ByteBuffer> {

    @Override
    public JsonElement serialize(ByteBuffer src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(Base64.getEncoder().encodeToString(src.array()));
    }

    @Override
    public ByteBuffer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        byte[] bytes = Base64.getDecoder().decode(json.getAsString());
        return ByteBuffer.wrap(bytes);
    }
      
}