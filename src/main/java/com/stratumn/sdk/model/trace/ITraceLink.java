
package com.stratumn.sdk.model.trace;

import java.util.Date;

import com.stratumn.chainscript.ChainscriptException;
import com.stratumn.chainscript.ILink;
/**
 * Interface extending a Chainscript Link
 * with common trace methods.
 */
public interface ITraceLink<TLinkData>  extends ILink
{
   public TLinkData formData() throws ChainscriptException ;
   public String traceId()throws ChainscriptException  ;
   public String workflowId()throws ChainscriptException ;
   public TraceLinkType type()throws ChainscriptException ; 
   public Account createdBy()throws ChainscriptException  ;
   public Date createdAt()throws ChainscriptException  ;
   public Account owner() throws ChainscriptException  ;
   public String  group()throws ChainscriptException  ;
   public String form()throws ChainscriptException ;
   public String lastForm()throws ChainscriptException  ;
   public String[] inputs() throws ChainscriptException ; 
   public TraceLinkMetaData metadata( )throws ChainscriptException ;
   
   
}
