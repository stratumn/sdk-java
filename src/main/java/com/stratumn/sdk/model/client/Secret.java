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
package com.stratumn.sdk.model.client;

/***
 * The secret abstract class 
 *
 */
public abstract class Secret
{  

   public static Secret newCredentialSecret(String email, String password)
   { 
      return   new CredentialSecret(email, password) ;
   }

   public static Secret newPrivateKeySecret(String privateKey)
   {
      return  new PrivateKeySecret(privateKey) ;
   }

   public static Secret newProtectedKeySecret(String publicKey, String password)
   {
        return  new ProtectedKeySecret(publicKey, password) ;
   }
   
   /**
    * Helper method to test that an object is of type CredentialSecret
    * @param secret
    * @return
    */
   public static boolean isCredentialSecret(Secret secret)
   {
      return (secret instanceof CredentialSecret);
   }

   /***
    * Helper method to test that an object is of type PrivateKeySecret
    * @param secret
    * @return
    */
   public static boolean isPrivateKeySecret(Secret secret)
   {
      return secret instanceof PrivateKeySecret;
   }
   
   /***
    * Helper method to test that an object is of type ProtectedKeySecret
    * @param secret
    * @return
    */
   public static Boolean isProtectedKeySecret(Secret secret)
   {
      return secret instanceof ProtectedKeySecret;
   }

}
