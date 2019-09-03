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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.google.gson.JsonObject;
import com.stratumn.canonicaljson.CanonicalJson;
import com.stratumn.chainscript.utils.JsonHelper;
import com.stratumn.sdk.model.file.FileInfo;
import com.stratumn.sdk.model.misc.Identifiable; 

/**
 * A file wrapper is a file representation on the platform. This class is
 * abstract and has various concrete implementation depending on the platform
 * (Browser, NodeJs).
 */
public abstract class FileWrapper implements Identifiable
{
   /**
    * A unique identifier of the file wrapper. Satisfies the Identifiable
    * constraint.
    */
   private String id = UUID.randomUUID().toString();

   private String key;

   @Override
   public String getId()
   {
      return this.id;
   }

   public FileWrapper()
   {
      this(true,null); 
   }
   
   
   public FileWrapper(boolean disableEncryption, String key)
   {
      if(!disableEncryption)
      {
         this.key = key; 
      }
   }
   
   /*** 
    * @param data
    * @return
    * @throws TraceSdkException 
    * @throws InvalidKeyException
    * @throws IllegalBlockSizeException
    * @throws BadPaddingException
    */
   protected ByteBuffer encryptData(ByteBuffer data) throws TraceSdkException  
   {
      if (this.key == null)
         return data;
      try
      {
          
         AesWrapper aeskey = new AesWrapper(key); 
         data  = aeskey.encrypt(data);
      }
      catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException e)
      {
         throw new TraceSdkException("Failed to encrypt file data");
      }
      return data;
   }
   
   /*** 
    * @param data
    * @return
    * @throws TraceSdkException 
    * @throws InvalidKeyException
    * @throws IllegalBlockSizeException
    * @throws BadPaddingException
    */
   protected ByteBuffer decryptData(ByteBuffer data) throws TraceSdkException  
   {
      if (this.key == null)
         return data;
      try
      {
         AesWrapper aeskey = new AesWrapper(key); 
          
         data  = aeskey.decrypt(data);
      }
      catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException e)
      {
         throw new TraceSdkException("Failed to encrypt file data");
      }
      return data;
   }
   
   /***
    * Add the key info to the filewrapper
    * @param info
    * @return
    */
   protected FileInfo addKeyToFileInfo(  FileInfo info) {
      if ( this.key == null) {
        return info;
      }
      String keyEx  =  this.key ;
      
       info.setKey(keyEx);
       return info;
    }
   

   /**
    * Get the file info. This method is async as in the NodeJs case, the info is
    * retrieved asynchronously using the statAsync function.
    * @throws IOException 
    * @throws TraceSdkException 
    */
   public abstract  FileInfo  info()  ;

   /**
    * The actual file data.
    * @throws IOException 
    */
   public abstract  ByteBuffer  encryptedData() throws TraceSdkException ;
   
   public abstract  ByteBuffer  decryptedData() throws TraceSdkException ;

   /**
    * Creates a FileWrapper from a browser file representation.
    *
    * @param file the browser File object
    */
   public static FileWrapper fromBrowserFile(File file)
   {
      return new BrowserFileWrapper(file);
   }

   /**
    * Creates a FileWrapper from a file path.
    *
    * @param fp the file path
    */
   public static FileWrapper fromFilePath(Path fp)
   {
      return new FilePathWrapper(fp);
   }

   /**
    * Creates a FileWrapper from a blob + info.
    *
    * @param blob     the blob data
    * @param fileInfo the file info
    */
   public static FileBlobWrapper fromFileBlob(ByteBuffer blob, FileInfo fileInfo)
   {
      return new FileBlobWrapper(blob, fileInfo);
   }
   
   /**
    * Creates a FileWrapper from an object.
    *
    * @param obj the object record + info
    * @throws IOException 
    */
   public static FileWrapper fromObject(Object obj) 
   { 
      
      return JsonHelper.objectToObject(obj,  FileWrapper.class)  ;
   }
   /**
    * Tests that an object is a FileWrapper.
    *
    * @param obj the object to test.
    */
   public static Boolean isFileWrapper(Object obj)
   {

      String json = null;
      boolean isFileWrapper = false;
      try
      {

         if(obj instanceof FileWrapper) 
            isFileWrapper = true;
         else
            if(obj != null)
            {
               if(obj instanceof JsonObject)
                  json =   JsonHelper.toCanonicalJson(obj) ;
               else
                  if(obj instanceof String)//assume json
                     json = CanonicalJson.canonizalize((String) obj);
                  else
                     json = JsonHelper.toCanonicalJson(obj);
               if(json != null)
               {
                  //attempt to generate FileWrapper from json.
                  Object ob = JsonHelper.fromJson(json, FileWrapper.class);
                  String json2 = JsonHelper.toCanonicalJson(ob);
                  if (json2.equalsIgnoreCase(json))
                     isFileWrapper = true; 
               } 
            }
      }
      catch(Exception ex)
      { //ignore
      }
      return isFileWrapper;
   }

   @Override
   public String toString()
   {
      return "FileWrapper [id=" + id + ", key=" + key + "]";
   }
   
   

}
