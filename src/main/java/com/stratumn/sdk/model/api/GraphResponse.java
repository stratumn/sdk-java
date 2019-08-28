package com.stratumn.sdk.model.api;

import java.util.Arrays;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stratumn.chainscript.utils.JsonHelper;

/***
 * Class that holds graph response and has helper function to extract data from
 * path.
 */
public class GraphResponse {
   private JsonObject data;
   private ErrorResponse[] errors;

   @Override
   public String toString() {
      return "GraphResponse [data=" + data + ", errors=" + Arrays.asList(errors).toString() + "]";
   }

   public JsonObject getData() {
      return data;
   }

   public ErrorResponse[] getErrors() {
      return errors;
   }

   public boolean hasErrors() {
      return (errors != null && errors.length > 0);
   }

   /***
    * Extracts jsonElement from path like A.B.C or A.B.C[i] or A.B[i].C
    * 
    * @param path
    * @return JsonElement
    */
   public JsonElement getData(String path) {
      if (path == null || data == null)
         return null;
      return JsonHelper.getData(data, path);
   }

}
