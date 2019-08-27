package com.stratumn.sdk;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.stratumn.sdk.model.file.FileInfo;

public class FileWrapperSerializer implements JsonSerializer<FileWrapper>
{

   @Override
   public JsonElement serialize(FileWrapper src, Type typeOfSrc, JsonSerializationContext context)
   { 
      if (src == null)
      return null;
      
      
      try
      {
         FileInfo info = src.info() ;
         FileRecord fr = new FileRecord(null, info);
         return context.serialize(fr);
      }
      catch(  TraceSdkException e)
      { 
         e.printStackTrace();
         return context.serialize(e.getMessage());
      } 
   }

}
