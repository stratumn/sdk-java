package com.stratumn.sdk.adapters;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.stratumn.sdk.BrowserFileWrapper;
import com.stratumn.sdk.FileBlobWrapper;
import com.stratumn.sdk.FilePathWrapper;
import com.stratumn.sdk.FileWrapper;

/***
 *  Enables serialization of FileWrapper
 */
public class FileWrapperGsonAdapter implements JsonDeserializer<FileWrapper>  
{
 

    @Override
    public FileWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        
         if(json != null && json instanceof JsonObject) if(((JsonObject) json).get("blob") != null)
            return context.deserialize(json, FileBlobWrapper.class);
         else
            if(((JsonObject) json).get("filePath") != null)
               return context.deserialize(json, FilePathWrapper.class);
            else
               if(((JsonObject) json).get("file") != null) return context.deserialize(json, BrowserFileWrapper.class);
         throw new JsonParseException("Failed to deserialize FileWrapper from:" + json.toString());
          
    }
 
}