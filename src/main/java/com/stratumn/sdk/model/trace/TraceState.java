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

import java.util.Arrays;
import java.util.Date;
/**
 * The state of trace is composed of:
 * - the trace id
 * - the link hash of the head of the trace
 * - the date at which it was last updated
 * - the person who last updated it
 * - some abstract data validated against a predefined schema
 * - the tags of the trace
 */
public class TraceState<TState, TLinkData>
{

   private String traceId;
   private ITraceLink<TLinkData> headLink;
   private Date updatedAt;
   private Account updatedBy;
   private TState data;
   private String[] tags;

   public TraceState(String traceId, ITraceLink<TLinkData> headLink, Date updatedAt, Account updatedBy, TState data, String[] tags)
      throws IllegalArgumentException
   {
      if(traceId == null)
      {
         throw new IllegalArgumentException("traceId cannot be null in a TraceState object");
      }

      this.traceId = traceId;
      this.headLink = headLink;
      this.updatedAt = updatedAt;
      this.updatedBy = updatedBy;
      this.data = data;
      this.tags = tags;

   }

   public String getTraceId()
   {
      return this.traceId;
   }

   public void setTraceId(String traceId)
   {
      this.traceId = traceId;
   }

   public ITraceLink<TLinkData> getHeadLink()
   {
      return this.headLink;
   }

   public void setHeadLink(ITraceLink<TLinkData> headLink)
   {
      this.headLink = headLink;
   }

   public Date getUpdatedAt()
   {
      return this.updatedAt;
   }

   public void setUpdatedAt(Date updatedAt)
   {
      this.updatedAt = updatedAt;
   }

   public Account getUpdatedBy()
   {
      return this.updatedBy;
   }

   public void setUpdatedBy(Account updatedBy)
   {
      this.updatedBy = updatedBy;
   }

   public TState getData()
   {
      return this.data;
   }

   public void setData(TState data)
   {
      this.data = data;
   }

   public String[] getTags()
   {
      return tags;
   }

   public void setTags(String[] tags)
   {
      this.tags = tags;
   }

   @Override
   public String toString()
   {
      return "TraceState [traceId=" + traceId + ", headLink=" + headLink + ", updatedAt=" + updatedAt + ", updatedBy=" + updatedBy + ", data=" + data
         + ", tags=" + Arrays.toString(tags) + "]";
   }

   
}
