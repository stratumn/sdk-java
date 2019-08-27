package com.stratumn.sdk;
/***
 *  Exception to wrap Sdk exceptiosn
 */
public class TraceSdkException extends  Exception
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public TraceSdkException()
   {
      super(); 
   }

   public TraceSdkException(String message, Throwable cause)
   {
      super(message, cause); 
   }

   public TraceSdkException(String message)
   {
      super(message); 
   }

   public TraceSdkException(Throwable cause)
   {
      super(cause); 
   }

}
