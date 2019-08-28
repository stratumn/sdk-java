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
 * A record of a file in the Media service.
 */
public class MediaRecord
{
   private String name;
   private String digest;

   public MediaRecord(String name, String digest) throws IllegalArgumentException
   {
      if(name == null)
      {
         throw new IllegalArgumentException("name cannot be null");
      }
      if(digest == null)
      {
         throw new IllegalArgumentException("digest cannot be null");
      }

      this.name = name;
      this.digest = digest;
   }

   public String getDigest()
   {
      return this.digest;
   }

   public void setDigest(String digest)
   {
      this.digest = digest;
   }

   public String getName()
   {
      return this.name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @Override
   public String toString()
   {
      return "MediaRecord [name=" + name + ", digest=" + digest + "]";
   }

}
