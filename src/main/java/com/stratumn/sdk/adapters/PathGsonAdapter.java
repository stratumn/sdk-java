package com.stratumn.sdk.adapters;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/***
 *  Enables serialization of path
 */
public class PathGsonAdapter implements JsonDeserializer<Path>, JsonSerializer<Path>    {

    @Override
    public JsonElement serialize(Path src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toUri().toASCIIString());
    }

    @Override
    public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         try
         {
            return Paths.get(new URI(json.toString()));
         }
         catch(URISyntaxException e)
         { 
            throw new JsonParseException(e);
         }
    }
 
}