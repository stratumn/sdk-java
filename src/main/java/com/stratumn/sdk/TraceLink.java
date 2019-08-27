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

import java.util.Date;

import com.stratumn.chainscript.ChainscriptException;
import com.stratumn.chainscript.Link;
import com.stratumn.sdk.model.trace.Account;
import com.stratumn.sdk.model.trace.ITraceLink;
import com.stratumn.sdk.model.trace.TraceLinkMetaData;
import com.stratumn.sdk.model.trace.TraceLinkType;
/**
 * A TraceLink is an extension of a Chainscript Link
 * that provides useful methods
 */
public class TraceLink<TLinkData> extends Link implements ITraceLink<TLinkData> {

  private TLinkData formData;
  
  public TraceLink(Link link,TLinkData formData) {
    super(link.getLink());
    this.formData=formData;
  }

  @SuppressWarnings("unchecked")
public  TLinkData formData() throws ChainscriptException  {
     
    return formData!=null?formData:(TLinkData)super.data();
  }

  public String traceId() throws ChainscriptException {
   return super.mapId();
  }

  public String workflowId() throws ChainscriptException  {
	   return super.process().getName();
	  }

	    
  public TraceLinkType type() throws ChainscriptException  {
    return TraceLinkType.valueOf(super.process().getState());
  }

  public TraceLinkMetaData metadata() throws ChainscriptException {
	  TraceLinkMetaData traceLinkMd= super.metadata(TraceLinkMetaData.class); 
	    return traceLinkMd;
  }

  public Account createdBy() throws ChainscriptException   {
   return new Account(this.metadata().getCreatedById());
  }

  public Date createdAt() throws ChainscriptException   {
	  return  this.metadata().getCreatedAt();
  }
  

  public Account owner() throws ChainscriptException  {
   return new Account(this.metadata().getOwnerId());
  }
  
  /**
   * The id of the group under which the trace is.
 * @throws ChainscriptException 
 * @throws Exception 
   *
   * @returns the group id
   */
  public String group() throws ChainscriptException   {
      return this.metadata().getGroupId();
  }
  /**
   * The id of the form that was used to create the link.
 * @throws ChainscriptException 
 * @throws Exception 
   *
   * @returns the form id
   */
  public String form() throws ChainscriptException   {
      return this.metadata().getFormId();
  }
  /**
   * The id of the form that was last used to create the link.
 * @throws ChainscriptException 
 * @throws Exception 
   *
   * @returns the last form id
   */
  public String lastForm() throws ChainscriptException   {
      return this.metadata().getLastFormId();
  }
  /**
   * The inputs of the link, used for transfer of ownership.
 * @throws ChainscriptException 
 * @throws Exception 
   *
   * @returns the inputs (array)
   */
  public String[] inputs() throws ChainscriptException  {
      return this.metadata().getInputs();
  }
  
  
  /**
   * Convert a plain object to a TraceLink.
   * @param rawLink plain object.
   */
  public static <TLinkData> TraceLink<TLinkData> fromObject(String rawLink, TLinkData formData)
  {
     return new TraceLink<TLinkData>(Link.fromObject( rawLink), formData);
  }
  



}
