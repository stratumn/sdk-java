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

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stratumn.canonicaljson.CanonicalJson;
import com.stratumn.chainscript.utils.JsonHelper;
import com.stratumn.sdk.model.file.FileInfo;
import com.stratumn.sdk.model.file.MediaRecord;
import com.stratumn.sdk.model.misc.Identifiable;

/**
 * A file record contains file information (FileInfo) and Media service record
 * information (MediaRecord). It corresponds to a file stored in the Media
 * service.
 */
public class FileRecord implements Identifiable {

   private String name;
   private String digest;
   private String mimetype;
   private Long size;
   private String key;
   private String createdAt;

   public FileRecord() {
      super();
   }

   public FileRecord(MediaRecord media, FileInfo info) {
      this.name = media.getName();
      this.digest = media.getDigest();
      this.mimetype = info.getMimetype();
      this.size = info.getSize();
      this.key = info.getKey();
      this.createdAt = info.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
   }

   public FileInfo getFileInfo() {
      return new FileInfo(name, size, mimetype, key);
   }

   public MediaRecord getMediaRecord() {
      return new MediaRecord(name, digest);
   }

   /**
    * This getter implements the Identifiable interface.
    */
   public String getId() {
      return this.digest;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDigest() {
      return digest;
   }

   public void setDigest(String digest) {
      this.digest = digest;
   }

   public String getMimetype() {
      return mimetype;
   }

   public void setMimetype(String mimetype) {
      this.mimetype = mimetype;
   }

   public Long getSize() {
      return size;
   }

   public void setSize(Long size) {
      this.size = size;
   }

   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public String getCreatedAt() {
      return createdAt;
   }

   public void setCreatedAt(String createdAt) {
      this.createdAt = createdAt;
   }

   /**
    * Creates a FileRecord from an object.
    *
    * @param obj the object record + info
    */
   public static FileRecord fromObject(Object obj) {

      return JsonHelper.objectToObject(obj, FileRecord.class);
   }

   /**
    * Test if the object is a FileRecord it is a FileRecord if it is an instance of
    * FileRecord or if It can be converted to a FileRecord .
    * 
    * @param obj the object to test.
    */
   public static Boolean isFileRecord(Object obj) {

      String json = null;
      boolean isFileRecord = false;
      try {

         if (obj instanceof FileRecord)
            isFileRecord = true;
         else if (obj != null) {
            if (obj instanceof JsonObject)
               json = JsonHelper.toCanonicalJson(obj);
            else if (obj instanceof String)// assume json
               json = CanonicalJson.canonizalize((String) obj);
            else
               json = JsonHelper.toCanonicalJson(obj);
            if (json != null) {
               // attempt to generate FileRecord from json.
               json = removeAdditionalFields(json);

               Object ob = JsonHelper.fromJson(json, FileRecord.class);
               String json2 = JsonHelper.toCanonicalJson(ob);
               if (json2.equalsIgnoreCase(json))
                  isFileRecord = true;
            }
         }
      } catch (Exception ex) { // ignore
      }
      return isFileRecord;
   }

   // Since we do a canonicalized JSON string comparison to check if the object is
   // a FileRecord above, we need to remove the additional fields that can be added
   // by the media API.
   private static String removeAdditionalFields(String json) {
      List<String> recordFields = Arrays.asList(new String[] { "id", "key", "digest", "name", "mimetype", "size" });
      JsonObject o = JsonHelper.fromJson(json, JsonObject.class);

      for (Entry<String, JsonElement> e : o.entrySet()) {
         if (!recordFields.contains(e.getKey())) {
            o.remove(e.getKey());
         }
      }

      return JsonHelper.toJson(o);

   }

   @Override
   public String toString() {
      return "FileRecord [name=" + name + ", digest=" + digest + ", mimetype=" + mimetype + ", size=" + size + ", key="
            + key + ", createdAt=" + createdAt + "]";
   }

}
