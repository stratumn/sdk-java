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
import java.io.RandomAccessFile;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import com.stratumn.sdk.model.file.FileInfo;

/**
 * The   implementation of a FileWrapper using a file path to point to the
 * actual file.
 */
public class FilePathWrapper extends FileWrapper
{

   private Path filePath;

   public FilePathWrapper(Path fp)
   {
      super();
      this.filePath = fp;
   }

   public FileInfo info() throws TraceSdkException
   {

      File file = filePath.toFile();
      if(!file.exists())
      {
         throw new TraceSdkException("Error while loading file " + file.getAbsolutePath());
      }

      if(!file.isFile())
      {
         throw new TraceSdkException(file.getAbsolutePath() + " is not a valid file");
      }

      final Long size = file.length(); 
      final String mimetype = URLConnection.guessContentTypeFromName(file.getName());
      final String name = file.getName();

      FileInfo fileInfo = new FileInfo(name, size, mimetype, null);

      return addKeyToFileInfo(fileInfo) ;

   }

   public Path getFilePath()
   {
      return this.filePath;
   }

   public void setFilePath(Path filePath)
   {
      this.filePath = filePath;
   }

   public  ByteBuffer  data() throws IOException  
   {
      ByteBuffer buffer;
      RandomAccessFile rFile = null;
      FileChannel inChannel = null;
      try 
      {
         rFile = new RandomAccessFile(filePath.toFile(), "r");
         inChannel = rFile.getChannel();
         long fileSize = inChannel.size();
         buffer = ByteBuffer.allocate((int) fileSize);
         inChannel.read(buffer);
         buffer.rewind();
      }
      finally
      {
         if (inChannel!=null)
         inChannel.close();
         if (rFile!=null)
         rFile.close();
      }
      return buffer;
   }

   @Override
   public  ByteBuffer  decryptedData() throws TraceSdkException  
   {
      ByteBuffer buffer = null;
      try
      {
         buffer = data() ;
      }
      catch(IOException  e)
      { 
         throw new TraceSdkException("Decryption failed ", e);
      }
      return buffer ;
   }

   @Override
   public ByteBuffer encryptedData () throws TraceSdkException  
   { 
      ByteBuffer buffer = null;
      try
      {
         buffer = super.encryptData( data() )   ;
      }
      catch(IOException |   BadPaddingException | InvalidKeyException | IllegalBlockSizeException   e)
      { 
         throw new TraceSdkException("Encryption failed ", e);
      }
      return buffer;
   }

}
