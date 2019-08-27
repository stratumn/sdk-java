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
package com.stratumn.sdk.model.trace;

import com.stratumn.sdk.TraceLink;

/***
 * Class to hold the traceId or prevLink used to identify the previous link.
 * @param <TLinkData>
 */
public class ParentLink<TLinkData>
{

   private String traceId;
   private TraceLink<TLinkData> prevLink;

   public ParentLink(String traceId, TraceLink<TLinkData> prevLink)
   {
      if (traceId == null && prevLink == null) {
         throw new IllegalArgumentException("TraceId and PrevLink cannot be both null");
       }
      this.traceId = traceId;
      this.prevLink = prevLink;
   }

   public String getTraceId()
   {
      return this.traceId;
   }

   public void setTraceId(String traceId)
   {
      this.traceId = traceId;
   }

   public TraceLink<TLinkData> getPrevLink()
   {
      return this.prevLink;
   }

   public void setPrevLink(TraceLink<TLinkData> prevLink)
   {
      this.prevLink = prevLink;
   }

}
