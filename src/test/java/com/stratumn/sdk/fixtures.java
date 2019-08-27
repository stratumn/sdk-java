package com.stratumn.sdk;

import com.google.gson.JsonObject; 

public class fixtures
{
   public static class SigningKey
   {
      public static final String pemPrivateKey ="\n" + 
         "-----BEGIN ED25519 PRIVATE KEY-----\n" + 
         "MFACAQAwBwYDK2VwBQAEQgRA3YYGIIAg4D7hsT5bXYE/OZsZrOon3h2u5R4ugDC1\n" + 
         "gjwSP9BQ2Dx7GyfNr8QX5fp695xnBr53x9i6YJCrLtWS8A==\n" + 
         "-----END ED25519 PRIVATE KEY-----\n" ;
      public static final String pemPublicKey ="\n" + 
         "-----BEGIN ED25519 PUBLIC KEY-----\n" + 
         "MCowBQYDK2VwAyEAEj/QUNg8exsnza/EF+X6evecZwa+d8fYumCQqy7VkvA=\n" + 
         "-----END ED25519 PUBLIC KEY-----\n" + 
         "";
   }
    
 
   public static final String FileObj ;
   static {  
         JsonObject jo =new JsonObject();
         jo.addProperty("digest","abc123");
         jo.addProperty("mimetype","text/plain");
         jo.addProperty("name","data.txt");
         jo.addProperty("size",123); 
         FileObj = jo.getAsString();
      }   ;  
      
   
}
