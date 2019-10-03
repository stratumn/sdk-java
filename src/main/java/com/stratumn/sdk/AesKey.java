package com.stratumn.sdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.stratumn.chainscript.Constants;

/***
 *  AES Wrapper : A Helper class to easily work with AES  
 */
public class AesKey
{
   public static final int SALT_LENGTH = 12;
   public static final int TAG_LENGTH = 16;
   public static final int KEY_LENGTH = 32;

   private SecretKeySpec secretKey;

   public AesKey()
   {
      this(null);

   }

   public AesKey(String secret) 
   {
      byte[] s;
      if (secret == null) {
         s = generateRandomBytes(KEY_LENGTH);
      } else {
         s = Base64.getDecoder().decode(secret);
      }
      this.secretKey = new SecretKeySpec(s, "AES");

   }

   private static byte[] generateRandomBytes(int length) {
      SecureRandom secureRandom = new SecureRandom();
      byte[] res = new byte[length];
      secureRandom.nextBytes(res);
      return res;
   }

   public String export() {
      return Base64.getEncoder().encodeToString(this.secretKey.getEncoded());
   }

   /***
    * Encrypts bytes in the buffer
    * @param bytes
    * @return
    * @throws InvalidKeyException
    * @throws IllegalBlockSizeException
    * @throws BadPaddingException
    * @throws NoSuchPaddingException
    * @throws NoSuchAlgorithmException
    * @throws InvalidAlgorithmParameterException
    */ 
   public ByteBuffer encrypt(ByteBuffer bytes) throws InvalidKeyException, InvalidAlgorithmParameterException,
      IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException
   { 
      byte[] iv = generateRandomBytes(SALT_LENGTH);
      final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      int tagLengthInBits = TAG_LENGTH * 8;
      GCMParameterSpec parameterSpec = new GCMParameterSpec(tagLengthInBits, iv);
      cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, parameterSpec);

      byte[] byteArr = new byte[bytes.capacity()];
      bytes.get(byteArr);
      byte[] ciphertext = cipher.doFinal(byteArr);
      
      // // Convert to base64 
      // byte[] b64Bytes = Base64.getEncoder().encode(ciphertext);
      
      ByteBuffer res = ByteBuffer.allocate(SALT_LENGTH + ciphertext.length);
      res.put(iv);
      res.put(ciphertext);
      res.rewind();
      return res;
   }
   
   /**
    * Decryptes the passed bufferS
    * @param bytes
    * @return
    * @throws IllegalBlockSizeException
    * @throws BadPaddingException
    * @throws InvalidKeyException
    * @throws NoSuchPaddingException
    * @throws NoSuchAlgorithmException
    * @throws InvalidAlgorithmParameterException
    */
   public ByteBuffer decrypt(ByteBuffer bytes) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
      InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException
   { 
      byte[] iv = new byte[SALT_LENGTH];
      bytes.get(iv);
      byte[] ciphertext = new byte[bytes.remaining()];
      bytes.get(ciphertext);

      final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      int tagLengthInBits = TAG_LENGTH * 8;

      cipher.init(Cipher.DECRYPT_MODE, this.secretKey, new GCMParameterSpec(tagLengthInBits, iv));
      
      return ByteBuffer.wrap(cipher.doFinal(ciphertext)); 
   }

}
