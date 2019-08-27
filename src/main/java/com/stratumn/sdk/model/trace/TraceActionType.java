package com.stratumn.sdk.model.trace;
/**
 * The various action types.
 */
public enum TraceActionType {
   ATTESTATION("_ATTESTATION_"), 
   PUSH_OWNERSHIP( "_PUSH_OWNERSHIP_"),
   PULL_OWNERSHIP("_PULL_OWNERSHIP_"),
   ACCEPT_TRANSFER("_ACCEPT_TRANSFER_"),
   CANCEL_TRANSFER("_CANCEL_TRANSFER_"),
   REJECT_TRANSFER("_REJECT_TRANSFER_");
   
   private String value;

   private TraceActionType(String value)
   {
      this.value = value;
   }
   
   public String toString()
   {
      return value;
      
   }
}
