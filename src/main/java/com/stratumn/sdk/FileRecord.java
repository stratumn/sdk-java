/*
Copyright 2017 Stratumn SAS. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.stratumn.sdk;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.stratumn.chainscript.utils.JsonHelper;
import com.stratumn.sdk.model.file.FileInfo;
import com.stratumn.sdk.model.file.MediaRecord;
import com.stratumn.sdk.model.misc.Identifiable;

/**
 * A file record contains file information (FileInfo) and Media service record
 * information (MediaRecord). It corresponds to a file stored in the Media
 * service.
 */
public class FileRecord implements Identifiable
{

   private String name;
   private String digest;
   private String mimetype;
   private Long size;
   private String key;
    

   public FileRecord()
   {
      super();
   }

   public FileRecord(MediaRecord media, FileInfo info)
   {
      this.name = media.getName();
      this.digest = media.getDigest();
      this.mimetype = info.getMimetype();
      this.size = info.getSize();
      this.key = info.getKey();
   }
   
   public FileInfo getFileInfo()
   {
      return new FileInfo(name,size,mimetype,key);
   }
   
   public MediaRecord getMediaRecord()
   {
      return new MediaRecord(name,digest);
   }

   /**
    * This getter implements the Identifiable interface.
    */
   public String getId()
   {
      return this.digest;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getDigest()
   {
      return digest;
   }

   public void setDigest(String digest)
   {
      this.digest = digest;
   }

   public String getMimetype()
   {
      return mimetype;
   }

   public void setMimetype(String mimetype)
   {
      this.mimetype = mimetype;
   }

   public Number getSize()
   {
      return size;
   }

   public void setSize(Long size)
   {
      this.size = size;
   }

   public String getKey()
   {
      return key;
   }

   public void setKey(String key)
   {
      this.key = key;
   }

   /**
    * Creates a FileRecord from an object.
    *
    * @param obj the object record + info
    * @throws IOException 
    */
   public static FileRecord fromObject(Object obj) throws IOException
   { 
      return   JsonHelper.fromJson(JsonHelper.toJson(obj), FileRecord.class)  ; 
   }

   /**
    * Test if the object is a FileRecord
    *
    * @param obj the object to test.
    */
   public static Boolean isFileRecord(Object obj)
   {

      String json = null;
      boolean isFileRecord = false;
      if(obj instanceof FileRecord)

         isFileRecord = false;
      else
         if(obj != null)
         {
            if(obj instanceof JsonObject)
               json = ((JsonObject) obj).getAsString();
            else
               if(obj instanceof String)//assume json
                  json = (String) obj;
            if(json != null)
            {
               try
               {
                  //attempt to generate FileRecord from json.
                  JsonHelper.fromJson(json, FileRecord.class);
                  isFileRecord = true;
               }
               catch(Exception ex)
               { //ignore
               }
            }

         }
      return isFileRecord;
   }

}
