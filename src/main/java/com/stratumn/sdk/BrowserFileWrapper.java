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

import com.stratumn.sdk.model.file.FileInfo;

/**
* The browser implementation of a FileWrapper.
* 
*/
public class BrowserFileWrapper extends FileWrapper
{

   private File file;

   BrowserFileWrapper(File file)
   {
      super();
      this.file = file;
   }

   public FileInfo info()  
   {

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

   public File getFile()
   {
      return this.file;
   }

   public void setFile(File file)
   {
      this.file = file;
   }

   private ByteBuffer data() throws TraceSdkException
   {

      if(!file.exists() || !file.isFile())
      {
         throw new TraceSdkException("File not found " + file.getAbsolutePath());
      }
      ByteBuffer buffer = null;
      try (RandomAccessFile rFile = new RandomAccessFile(file, "r"); FileChannel inChannel = rFile.getChannel();)
      {
         long fileSize = inChannel.size();
         buffer = ByteBuffer.allocate((int) fileSize);
         inChannel.read(buffer);
         buffer.rewind();
      }
      catch(IOException e)
      {
         throw new TraceSdkException("Error reading file ", e);
      }
      return buffer;
   }

   @Override
   public ByteBuffer decryptedData() throws TraceSdkException
   {
      ByteBuffer buffer = null;

      buffer = data();

      return buffer;
   }

   @Override
   public ByteBuffer encryptedData() throws TraceSdkException
   {
      ByteBuffer buffer = null;

      buffer = super.encryptData(data());

      return buffer;
   }

}
