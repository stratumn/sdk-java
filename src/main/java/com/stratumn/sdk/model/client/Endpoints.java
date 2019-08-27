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
package com.stratumn.sdk.model.client;

/***
 * The endpoints interface to describe the api urls
 * of all stratumn services 
 *
 */
public class Endpoints
{

   private String trace;
   private String account;
   private String media;

   public Endpoints(String account, String trace, String media)
   {
      this.account = account;
      this.trace = trace;
      this.media = media;
   }

   public String getTrace()
   {
      return this.trace;
   }

   public void setTrace(String trace)
   {
      this.trace = trace;
   }

   public String getAccount()
   {
      return this.account;
   }

   public void setAccount(String account)
   {
      this.account = account;
   }

   public String getMedia()
   {
      return this.media;
   }

   public void setMedia(String media)
   {
      this.media = media;
   }

   /***
    * Returns the endpoint by service type
    * @param service
    * @return
    */
   public String getEndpoint(Service service)
   {
      switch(service)
      {
         case ACCOUNT:
            return this.getAccount();

         case TRACE:
            return getTrace();

         case MEDIA:
            return getMedia();

      }
      return null;
   }
}
