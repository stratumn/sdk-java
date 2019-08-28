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
package com.stratumn.sdk.model.file;

/**
 * A file information interface.
 */
public class FileInfo 
{
  
   private String mimetype;
   private Long size;
   private String name;
   private String key;

   public FileInfo(String name, Long size, String mimetype, String key) throws IllegalArgumentException
   {
 
      if(name == null)
      {
         throw new IllegalArgumentException("name cannot be null");
      }
      if(size == null)
      {
         throw new IllegalArgumentException("size cannot be null");
      }
      if(mimetype == null)
      {
         throw new IllegalArgumentException("mimetype cannot be null");
      }

      this.name = name;
      this.size = size;
      this.mimetype = mimetype;
      this.key = key;
   }

   public String getMimetype()
   {
      return this.mimetype;
   }

   public void setMimetype(String mimetype)
   {
      this.mimetype = mimetype;
   }

   public Long getSize()
   {
      return this.size;
   }

   public void setSize(Long size)
   {
      this.size = size;
   }

   public String getName()
   {
      return this.name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getKey()
   {
      return key;
   }

   public void setKey(String key)
   {
      this.key = key;
   }

   @Override
   public String toString()
   {
      return "FileInfo [mimetype=" + mimetype + ", size=" + size + ", name=" + name + ", key=" + key + "]";
   }

}
