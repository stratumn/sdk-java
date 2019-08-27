package com.stratumn.sdk.model.trace;
/**
 * Interface used as argument to add tags to an existing trace.
 * User must provide the trace id and the tags.
 */
public class AddTagsToTraceInput
{
   private String traceId;
   private String[] tags;
   public AddTagsToTraceInput()
   {
      super(); 
   }
   public AddTagsToTraceInput(String traceId, String[] tags)
   {
      super();
      this.traceId = traceId;
      this.tags = tags;
   }
   public String getTraceId()
   {
      return traceId;
   }
   public void setTraceId(String traceId)
   {
      this.traceId = traceId;
   }
   public String[] getTags()
   {
      return tags;
   }
   public void setTags(String[] tags)
   {
      this.tags = tags;
   }
   
   
}
