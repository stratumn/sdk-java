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

/**
 * Options Class used to instantiate the Client.
 */
public class ClientOptions
{
   /**
   * To configure the endpoints. Can be a short tag like 'release' or 'staging'.
   * Can also be a struct to configure each service endpoint, eg: { trace: 'https://...' .. }.
   * Defaults to release endpoints.
   */
   private Endpoints endpoints;

   /**
   * The secret used to authenticate the input.
   * Can be a signing key or a username + password.
   */
   private Secret secret;

   
   public ClientOptions(Endpoints endpoints, Secret secret) throws IllegalArgumentException
   {    
      this.endpoints = endpoints;
      this.secret = secret;
   }

   public Endpoints getEndpoints()
   {
      return this.endpoints;
   }

   public void setEndpoints(Endpoints endpoints)
   {
      this.endpoints = endpoints;
   }

   public Secret getSecret()
   {
      return this.secret;
   }

   public void setSecret(Secret secret)
   {
      this.secret = secret;
   }

}