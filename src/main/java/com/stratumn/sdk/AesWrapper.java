package com.stratumn.sdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.stratumn.chainscript.Constants;

/***
 *  AES Wrapper : A Helper class to easily work with AES  
 */
public class AesWrapper
{

   private SecretKeySpec secretKey;
   private Cipher cipher;

   public AesWrapper(String secret) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException
   {
      this(secret, 16, "AES");
   }

   public SecretKeySpec getSecretKey()
   {
      return secretKey;
   }

   public void setSecretKey(SecretKeySpec secretKey)
   {
      this.secretKey = secretKey;
   }

   public AesWrapper(String secret, int length, String algorithm)
      throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException
   {
      byte[] key = new byte[length];
      key = fixSecret(secret, length);
      this.secretKey = new SecretKeySpec(key, algorithm);
      this.cipher = Cipher.getInstance(algorithm);
   }

   /*** 
    * Adjusts the length of the secret to match the 
    * @param s
    * @param length
    * @return
    * @throws UnsupportedEncodingException
    */
   private byte[] fixSecret(String s, int length) throws UnsupportedEncodingException
   {
      if(s.length() < length)
      {
         int missingLength = length - s.length();
         for(int i = 0; i < missingLength; i++)
         {
            s += " ";
         }
      }
      return s.substring(0, length).getBytes(Constants.UTF8);
   }

   
   /***
    * Encrypts bytes in the buffer
    * @param bytes
    * @return
    * @throws InvalidKeyException
    * @throws IllegalBlockSizeException
    * @throws BadPaddingException
    */ 
   public ByteBuffer encrypt(ByteBuffer bytes) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException
   { 
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      byte[] byteArr = new byte[bytes.capacity()];
      bytes.get(byteArr);
      return ByteBuffer.wrap(  Base64.getEncoder().encode (cipher.doFinal(byteArr))); 
   }
   
   /**
    * Decryptes the passed bufferS
    * @param bytes
    * @return
    * @throws IllegalBlockSizeException
    * @throws BadPaddingException
    * @throws InvalidKeyException
    */
   public ByteBuffer decrypt(ByteBuffer bytes) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException 
   { 
      cipher.init(Cipher.DECRYPT_MODE, secretKey);
      byte[] byteArr = new byte[bytes.capacity()];
      bytes.get(byteArr);
      byteArr = Base64.getDecoder().decode(byteArr);
      
      return ByteBuffer.wrap( cipher.doFinal(byteArr)); 
   }
   

   
   /***
    * Returns the encryoted string
    * @param strToEncrypt
    * @return
    * @throws InvalidKeyException 
    * @throws UnsupportedEncodingException 
    * @throws BadPaddingException 
    * @throws IllegalBlockSizeException 
    */
   public String encrypt(String strToEncrypt) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException
   {

      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(Constants.UTF8)));

   }

   /***
    * Returns the decrypted string
    * @param strToDecrypt
    * @return
    * @throws InvalidKeyException 
    * @throws BadPaddingException 
    * @throws IllegalBlockSizeException 
    */
   public String decrypt(String strToDecrypt) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException
   {
      cipher.init(Cipher.DECRYPT_MODE, secretKey);
      return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));

   }

   /**
    * Encrypts the file and writes it back in encrypted form 
    * @param f
    * @throws InvalidKeyException
    * @throws IOException
    * @throws IllegalBlockSizeException
    * @throws BadPaddingException
    */
   public void encryptFile(File f) throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException
   { 
      this.cipher.init(Cipher.ENCRYPT_MODE, this.secretKey);
      this.writeToFile(f);
   }

   /***
    * Decryptes the file and writes it back in decrypted form
    * @param f
    * @throws InvalidKeyException
    * @throws IOException
    * @throws IllegalBlockSizeException
    * @throws BadPaddingException
    */
   public void decryptFile(File f) throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException
   {
       
      this.cipher.init(Cipher.DECRYPT_MODE, this.secretKey);
      this.writeToFile(f);
   }

   /***
    * Writes the file back in encrypted form
    * @param f
    * @throws IOException
    * @throws IllegalBlockSizeException
    * @throws BadPaddingException
    */
   private void writeToFile(File f) throws IOException, IllegalBlockSizeException, BadPaddingException
   {
      FileInputStream in = new FileInputStream(f);
      byte[] input = new byte[(int) f.length()];
      in.read(input);

      FileOutputStream out = new FileOutputStream(f);
      byte[] output = this.cipher.doFinal(input);
      out.write(output);

      out.flush();
      out.close();
      in.close();
   }
}
