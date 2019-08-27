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

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import com.stratumn.sdk.model.file.FileInfo;

/**
 * The implementation of a FileWrapper using the blob and info to represent it.
 */
public class FileBlobWrapper extends FileWrapper {
	private ByteBuffer blob;
	private FileInfo fileInfo;

	public FileBlobWrapper(ByteBuffer blob, FileInfo fileInfo) {
		 super(fileInfo.getKey()==null,fileInfo.getKey());
		this.blob = blob;
		this.fileInfo = fileInfo;
	}

	public FileInfo info() {
		return this.fileInfo;
	}



   @Override
   public ByteBuffer encryptedData() throws TraceSdkException
   {
      try
      {
         ByteBuffer data = super.decryptData(this.blob);
         return data;
      }
      catch(InvalidKeyException | IllegalBlockSizeException | BadPaddingException e)
      {
         throw new TraceSdkException("Decryption failed", e);
      }
   }

   @Override
   public  ByteBuffer  decryptedData() throws TraceSdkException
   {
 
         return  this.blob ;
      
   }

}
