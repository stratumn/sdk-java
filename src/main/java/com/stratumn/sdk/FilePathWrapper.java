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
import java.util.Arrays;
import java.util.Base64;

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

   public FileInfo info()  
   {
      File file = filePath.toFile();
      if(!file.exists())
      {
         throw new IllegalArgumentException("Error while loading file " + file.getAbsolutePath());
      }

      if(!file.isFile())
      {
         throw new IllegalArgumentException(file.getAbsolutePath() + " is not a valid file");
      }

      final Long size = file.length();
      final String mimetype = URLConnection.guessContentTypeFromName(file.getName());
      final String name = file.getName();

      FileInfo fileInfo = new FileInfo(name, size, mimetype, null);

      return addKeyToFileInfo(fileInfo);

   }

   public Path getFilePath()
   {
      return this.filePath;
   }

   public void setFilePath(Path filePath)
   {
      this.filePath = filePath;
   }

   private ByteBuffer data() throws TraceSdkException
   {
      File file = filePath.toFile();
      if(!file.exists() || !file.isFile())
      {
         throw new IllegalArgumentException("File not found " + file.getAbsolutePath());
      }
      ByteBuffer buffer = null;  
      try(RandomAccessFile rFile = new RandomAccessFile(filePath.toFile(), "r");
         FileChannel inChannel = rFile.getChannel();
         )
      { 
         long fileSize = inChannel.size();
         buffer = ByteBuffer.allocate((int) fileSize);
         inChannel.read(buffer);
         buffer.rewind();
      }
      catch (IOException e) { 
         throw new TraceSdkException("Error reading file " , e); 
      } 

      return buffer;
   }

   @Override
   public ByteBuffer decryptedData() throws TraceSdkException
   {
      return super.decryptData(data());
   }

   @Override
   public ByteBuffer encryptedData() throws TraceSdkException
   {
      return super.encryptData(data());
   }

}
