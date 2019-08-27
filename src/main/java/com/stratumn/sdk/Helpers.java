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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.gson.JsonObject;
import com.stratumn.chainscript.Constants;
import com.stratumn.chainscript.utils.CryptoUtils;
import com.stratumn.sdk.model.client.Endpoints;
import com.stratumn.sdk.model.misc.Identifiable;

public class Helpers
{

    private static final String TRACE_RELEASE_URL = "https://trace.stratumn.com";
    private static final String ACCOUNT_RELEASE_URL = "https://account.stratumn.com";
    private static final String MEDIA_RELEASE_URL = "https://media.stratumn.com";
     

   /***
    * The release endpoints
    */
   private static final Endpoints ReleaseEndpoints = new Endpoints(ACCOUNT_RELEASE_URL, TRACE_RELEASE_URL, MEDIA_RELEASE_URL);

   /**
    * Generates the endpoints object. If not specified, the release
    * endpoints will be used by default.
    *
    * @param endpoints (optional) the custom endpoints object
    * @return the endpoints object
    */
   public static Endpoints makeEndpoints(Endpoints endpoints) throws InvalidParameterException
   {

      if(endpoints == null)
      {
         return ReleaseEndpoints;
      }

      if(endpoints.getAccount() == null || endpoints.getTrace() == null || endpoints.getMedia() == null)
      {
         throw new InvalidParameterException("The provided endpoints argument is not valid.");
      }
      return endpoints;
   }

   /***
    * Generates a signed token that can be used to
   * authenticate to retrieve a long lived auth token.
    * @param key
    * @return 
    * @throws UnsupportedEncodingException 
    * @throws SignatureException 
    * @throws NoSuchAlgorithmException 
    * @throws InvalidKeyException 
    * @throws InvalidKeySpecException 
    * @throws Exception 
    */
   public static String makeAuthPayload(PrivateKey key)
      throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException
   {
      JsonObject timeJson = new JsonObject();
      long nowInSeconds = nowInSeconds();
      timeJson.addProperty("iat", nowInSeconds);
      timeJson.addProperty("exp", nowInSeconds + 60 * 5);
      String b64 = null;

      // Convert Message to Bytes
      byte[] message = timeJson.toString().getBytes(Constants.UTF8);
      // sign the message
      String signature = CryptoUtils.sign(key, message);
      String publicKeyPem = CryptoUtils.encodePublicKey(CryptoUtils.getPublicKeyFromPrivateKey(key));
     
      // convert message , signature and public key to base64
      String messagebase64 = Base64.getEncoder().encodeToString(message);
      String signaturebase64 = Base64.getEncoder().encodeToString(signature.getBytes(Constants.UTF8));
      String publickKeyBase64 = Base64.getEncoder().encodeToString(publicKeyPem.getBytes(Constants.UTF8));

      JsonObject keysJson = new JsonObject(); 
      keysJson.addProperty("signature", signaturebase64);
      keysJson.addProperty("message", messagebase64);
      keysJson.addProperty("public_key", publickKeyBase64);
      String keysJsonStr = keysJson.toString() ; 
      b64 = Base64.getEncoder().encodeToString(keysJsonStr.getBytes());

      return b64;
   }
 

   public static long nowInSeconds()
   {
      return Calendar.getInstance().getTime().getTime() / 1000;
   }
   
 
 
   /**
    * Extract all file records from some data.
    *
    * @param data the data containing file records to extract
    */
   public static <T> Map<String, FileRecord> extractFileRecords(T data) 
   {
      return extractObjects(data, (T) -> FileRecord.isFileRecord(data), (T) -> {
         try
         {
            return FileRecord.fromObject((String) data);
         }
         catch(IOException e)
         { return null;
         }
      });
   }
   
   /**
    * Extract all file wrappers from some data.
    *
    * @param data the data containing file wrappers to extract
    * @return [pathToIdMap,idToObjectMap]
    */
   public static <T> Map<String, FileWrapper> extractFileWrappers(T data)
   {
      return extractObjects(data, (X) -> FileWrapper.isFileWrapper(X),null);
   }
   
   /**
    * Extracts all identifiable objects in data that satisfy a predicate
    * and return two maps to easily work with them:
    * - pathToIdMap: maps all the paths where an object was found to their id
    * - idToObjectMap: maps all ids to the actual extracted objects
    *
    * @param data the data where objects should be extracted
    * @param predicate the predicate function used to determine
    * if object should be extracted
    * @param reviver (optional) a function to apply on the extracted object
    * @return Map Array [pathToIdMap,idToObjectMap]
    */
      private static <T, V extends Identifiable> Map<String, V> extractObjects  (T data, 
       Predicate<T> predicate ,
       Function<V, V> reviver)
      {
    
         // create a new idToObject map
         Map<String, V> idToObjectMap = new HashMap<String, V>();
            
         // call the implementation
         extractObjectsImpl(data,"", idToObjectMap, predicate, reviver);
         // return the maps
         return idToObjectMap;
      }
   
      /**
       * The implementation of the extractObjects function below.
       *
       * @param data the data where objects should be extracted
       * @param path the current path in the data
       * @param pathToIdMap the path to id map
       * @param idToObjectMap the id to object map
       * @param predicate the predicate function used to determine
       * if object should be extracted
       * @param reviver (optional) a function to apply on the extracted object
       */
      @SuppressWarnings("unchecked")
      private static <T , V extends Identifiable>  void extractObjectsImpl (T data, String path,   Map<String,V> idToObjectMap,
         Predicate<T> predicate ,
         Function<V, V> reviver )
      {
         // if the predicate is true, then this data should be extracted
         if (predicate.test(data))
         {
            
            // apply reviver if provided 
            V newData =reviver!= null ? reviver.apply((V)data):(V) data; 
    
            // add a new entry to the idToObject map
            idToObjectMap.put(newData.getId(), newData);
         }else if (data!=null && data.getClass().isArray())
         {
            // if it is an array, iterate through each element and
            // extract objects recursively
            int idx=0;
            for(T value : ((T[]) data))
            {
               extractObjectsImpl(
                  // the new data to extract from
                  value,
                  // the new path is `path[idx]`
                  path + "[" + idx + "]",
                 
                  idToObjectMap,
                  predicate,
                  reviver
                );
               idx++;
            }

         } 
         else if ( data instanceof Map )
         {
            for (Entry<String, Object> element : ((Map<String, Object>)data).entrySet() )
            {       
               extractObjectsImpl(
                  // the new data to extract from
                  (T)element.getValue(),
                  // the new path is `path.key`
                  // when in the root data, path is empty
                  // so use the key directly
                  path.isEmpty()? element.getKey() : path + "." + element.getKey()  ,
                
                  idToObjectMap,
                  predicate,
                  reviver
                );
            }
         }
 
         
      }
}