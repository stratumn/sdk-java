package com.stratumn.sdk.adapters;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.stratumn.sdk.FileRecord;
import com.stratumn.sdk.FileWrapper;
import com.stratumn.sdk.model.misc.Identifiable;

/***
 *  Enables deserialization of Identifiable declared field.
 */
public class IdentifiableGsonAdapter  implements JsonDeserializer<Identifiable>, JsonSerializer<Identifiable>     {

     
    @Override
    public Identifiable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
           if(json != null && json instanceof JsonObject) 
              if(((JsonObject) json).get("digest") != null)
                 return context.deserialize(json, FileRecord.class);
              else 
                 return context.deserialize(json, FileWrapper.class);
          return null; 
          
    }

   @Override
   public JsonElement serialize(Identifiable src, Type typeOfSrc, JsonSerializationContext context)
   {
      JsonElement json = context.serialize(src);
      return json;
       
   }
 
}