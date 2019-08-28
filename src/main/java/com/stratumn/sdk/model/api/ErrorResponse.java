package com.stratumn.sdk.model.api;

import java.util.Arrays;
import java.util.Map;
/***
 * 
 * 
 * Class that holds deserialized errors
 */
public class ErrorResponse 
{
   private String message;
   private String[] path;
   private String status;
   private Map<String,Integer>[] locations;
   public String getMessage()
   {
      return message;
   }
   public String[] getPath()
   {
      return path;
   }
   public String getStatus()
   {
      return status;
   }
   public Map<String, Integer>[] getLocations()
   {
      return locations;
   }
   @Override
   public String toString()
   {
      return "ErrorResponse [message=" + message + ", path=" + Arrays.toString(path) + ", status=" + status + ", locations="
         + Arrays.toString(locations) + "]";
   }
   
   
}
