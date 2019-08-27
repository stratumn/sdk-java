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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stratumn.chainscript.utils.CryptoUtils;
import com.stratumn.sdk.graph.GraphQl;
import com.stratumn.sdk.graph.GraphQlRequest;
import com.stratumn.sdk.model.client.ClientOptions;
import com.stratumn.sdk.model.client.CredentialSecret;
import com.stratumn.sdk.model.client.Endpoints;
import com.stratumn.sdk.model.client.FetchOptions;
import com.stratumn.sdk.model.client.GraphQLOptions;
import com.stratumn.sdk.model.client.PrivateKeySecret;
import com.stratumn.sdk.model.client.Secret;
import com.stratumn.sdk.model.client.Service;
import com.stratumn.sdk.model.file.FileInfo;
import com.stratumn.sdk.model.file.MediaRecord;

/**
 * A wrapper client to handle communication with account, trace and media via
 * REST and GraphQL
 *
 * The Client will handle (re-)authentication if a token is not present yet or
 * expired.
 *
 * The Client exposes 3 main methods: - get - post - graphql
 */
public class Client {
   /**
    * The default GraphQL options: - retry once
    */
   private static final GraphQLOptions DefaultGraphQLOptions = new GraphQLOptions(1);

   /**
    * The endpoint urls for all the services
    */
   private Endpoints endpoints;
   /**
    * The secret used to authenticate
    */
   private Secret secret;
   /**
    * The token received from account service after authentication
    */
   private String token;

   private Proxy proxy;

   /***
    * GSON object instance
    */
   private static final Gson gson = new Gson();

   /***
    * Constructs a new instance of the Client
    * 
    * @param opts
    */
   public Client(ClientOptions opts) {
      this.endpoints = Helpers.makeEndpoints(opts.getEndpoints());

      this.secret = opts.getSecret();

   }

   /**
    * Compute the bearer Authorization header of format "Bearer my_token". If the
    * token is undefined, the return header is an empty string "".
    *
    * @param token optional token to be used
    */
   private String makeAuthorizationHeader(String token) {
      return (token != null) ? "Bearer " + token : "";
   }

   /**
    * Retrieves an authentication token based on the following waterfall: - if
    * opts.authToken is set, use it to compute the auth header - if opts.skipAuth
    * is true, return empty auth header - otherwise login and use the retrieved
    * token to compute the auth header
    *
    * @param opts           optional options
    * @param opts.authToken optional token to be used
    * @param opts.skipAuth  optional flag to bypass authentication
    * @throws TraceSdkException
    */
   private String getAuthorizationHeader(FetchOptions opts) throws TraceSdkException {

      if (opts != null) {
         if (opts.getAuthToken() != null)
            return this.makeAuthorizationHeader(opts.getAuthToken());
         if (opts.getSkipAuth() != null)
            return this.makeAuthorizationHeader(null);
      }

      this.login();
      return this.makeAuthorizationHeader(this.token);
   }

   /**
    * To set a new token
    * 
    * @param token the new token
    */
   private void setToken(String token) {
      this.token = token;
   }

   /**
    * To clear the existing token
    */
   private void clearToken() {
      this.token = null;
   }

   /**
    * Utility method to fetch a ressource on a target service via REST.
    *
    * @param request its instance from HttpHelpers
    * @throws HttpError
    * @returns the responseContent
    */
   @SuppressWarnings("unchecked")
   private <T> T fetch(HttpHelpers request, int retry) throws HttpError {
      T responseContent = null;
      try {
         request.sendData();
         HttpURLConnection con = request.getConnection();
         int status = con.getResponseCode();
         responseContent = (T) request.read();

         // int status = con.getResponseCode();
         Boolean ok = (status < HttpURLConnection.HTTP_BAD_REQUEST)
               || (200 <= con.getResponseCode() && con.getResponseCode() <= 299);

         if (!ok) {
            // if 401 and retry > 0 then we can retry
            if (status == 401 && retry > 0) {
               // unauthenticated request might be because token expired
               // clear token and retry
               this.clearToken();
               return this.fetch(request, --retry);
            }

            // otherwise that's a proper error
            // extract the text body of the response
            // and try to convert it to JSON
            String errTxt = request.readError();

            // throw that new error
            throw new HttpError(status, errTxt);
         }

      } catch (IOException ioe) {
         throw new HttpError(HttpURLConnection.HTTP_INTERNAL_ERROR, ioe.getLocalizedMessage());
      }

      // finally return the body
      return responseContent;

   }

   /**
    * Authenticate using a signed message via the GET /login route.
    *
    * @param key the signing private key in clear text used to log in
    * @throws Exception
    */
   private void loginWithSigningPrivateKey(String pemPrivateKey) throws Exception {

      PrivateKey privateKey = CryptoUtils.decodePrivateKey(pemPrivateKey);

      String signedToken = Helpers.makeAuthPayload(privateKey);

      String tokenResponse = this.<String>get(Service.ACCOUNT, "login", null,
            new FetchOptions(signedToken, false, null));
      JsonObject tokenJson = gson.fromJson(tokenResponse, JsonObject.class);
      // finally set the new token
      this.setToken(tokenJson.get("token").getAsString());
   }

   /**
    * Authenticates using a user's credentials via the POST /login route.
    *
    * @param email    the email of the user
    * @param password the password of the user
    * @throws TraceSdkException
    */
   private void loginWithCredentials(String email, String password) throws TraceSdkException {
      // get the user salt first
      // use skipAuth = true to bypass authentication
      // GET /salt is a public route!

      Map<String, String> parameters = new HashMap<>();
      parameters.put("email", email);

      String saltResponse = this.<String>get(Service.ACCOUNT, "salt", parameters, (new FetchOptions(null, true, 0)));
      JsonObject saltJson = gson.fromJson(saltResponse, JsonObject.class);
      String salt = saltJson.get("salt").getAsString();

      // hash the password with the salt
      String passwordHash = org.springframework.security.crypto.bcrypt.BCrypt.hashpw(password, salt);

      parameters.put("passwordHash", passwordHash);
      String bodyJson = gson.toJson(parameters);

      // post the login payload
      // use skipAuth = true to bypass authentication
      // POST /login is a public route!
      String tokenResponse = this.<String>post(Service.ACCOUNT, "login", bodyJson, (new FetchOptions(null, true, 0)));
      JsonObject tokenJson = gson.fromJson(tokenResponse, JsonObject.class);
      // finally set the new token
      this.setToken(tokenJson.get("token").getAsString());

   }

   /**
    * Authenticates using a valid secret. Supported secret types are: -
    * CredentialSecret -> via email+password - PrivateKeySecret -> via signed
    * message
    * 
    * @throws TraceSdkException
    */
   private synchronized void login() throws TraceSdkException {

      // if another concurrent execution has already
      // done the job, then release and return, nothing to do.
      if (this.token != null)
         return;

      // otherwise do the job...
      if (Secret.isCredentialSecret(this.secret)) {
         // the CredentialSecret case
         final String email = ((CredentialSecret) this.secret).getEmail();
         final String password = ((CredentialSecret) this.secret).getPassword();
         try {
            this.loginWithCredentials(email, password);
         } catch (TraceSdkException e) {
            throw new TraceSdkException("Login with email password failed.", e);
         }
      } else if (Secret.isPrivateKeySecret(this.secret)) {
         // the PrivateKeySecret case
         final String privateKey = ((PrivateKeySecret) this.secret).getPrivateKey();
         try {
            this.loginWithSigningPrivateKey(privateKey);
         } catch (Exception e) {
            throw new TraceSdkException("Login with private key failed", e);
         }
      } else if (Secret.isProtectedKeySecret(this.secret)) {
         // the ProtectedKeySecret case
         // not handled yet
         throw new TraceSdkException("Authentication via password protected key is not handled");
      } else {
         // Unknown case
         throw new TraceSdkException("The provided secret does not have the right format");
      }

   }

   /**
    * Executes a POST query on a target service.
    *
    * @param service the service to target (account|trace|media)
    * @param route   the route on the target service
    * @param body    the POST body object
    * @param opts    additional fetch options
    * @throws TraceSdkException
    * @returns the response body object
    */
   public <T> T post(Service service, String route, String body, FetchOptions opts) throws TraceSdkException {
      try {
         // create default fetch options.
         if (opts == null)
            opts = new FetchOptions();

         // References: https://www.baeldung.com/java-http-request
         // https://juffalow.com/java/how-to-send-http-get-post-request-in-java
         String path = this.endpoints.getEndpoint(service) + '/' + route;
         URL url = new URL(path);

         HttpURLConnection con;
         if (this.proxy != null) {
            con = (HttpURLConnection) url.openConnection(this.proxy);
         } else {
            con = (HttpURLConnection) url.openConnection();
         }

         con.setRequestMethod("POST");
         con.setRequestProperty("Content-Type", "application/json");
         con.setRequestProperty("Accept", "application/json");
         con.setRequestProperty("Authorization", this.getAuthorizationHeader(opts));
         con.setDoOutput(true);

         HttpHelpers request = new HttpHelpers(con);
         request.setBody(body);

         Integer retry = opts.getRetry();

         // delegate to fetch wrapper
         return this.fetch(request, retry);

      } catch (Exception e) {
         throw new TraceSdkException("Error executing post request", e);
      }
   }

   /**
    * Executes a GET query on a target service.
    *
    * @param service the service to target (account|trace|media)
    * @param route   the route on the target service
    * @param params  the query parameters
    * @param opts    additional fetch options
    * @throws TraceSdkException
    * @returns the response body object
    */
   public <T> T get(Service service, String route, Map<String, String> params, FetchOptions opts)
         throws TraceSdkException {

      try {
         // create default fetch options.
         if (opts == null)
            opts = new FetchOptions();

         String path = this.endpoints.getEndpoint(service) + '/' + route;

         if (params != null) {
            path += "?" + HttpHelpers.getParamsString(params);
         }

         URL url = new URL(path);

         HttpURLConnection con;
         if (this.proxy != null) {
            con = (HttpURLConnection) url.openConnection(this.proxy);
         } else {
            con = (HttpURLConnection) url.openConnection();
         }
         con.setRequestProperty("Content-Type", "application/json; utf-8");
         con.setRequestProperty("Accept", "application/json");
         con.setRequestProperty("Authorization", this.getAuthorizationHeader(opts));
         con.setRequestMethod("GET");

         HttpHelpers request = new HttpHelpers(con);

         // delegate to fetch wrapper
         return this.fetch(request, opts.getRetry());
      } catch (Exception e) {
         throw new TraceSdkException("Error executing get request", e);
      }

   }

   /**
    * Executes a GraphQL query / mutation on the Trace service.
    *
    * @param query     the graphql query / mutation
    * @param variables the graphql variables
    * @param opts      the graphql options
    * @throws TraceSdkException
    * 
    */
   public <T> T graphql(GraphQl.Query query, Map<String, Object> variables, GraphQLOptions opts, Class<T> tclass)
         throws TraceSdkException {

      String queryStr;
      try {
         queryStr = query.loadQuery();
      } catch (IOException e) {
         throw new TraceSdkException("Error loading query", e);
      }
      if (opts == null) {
         opts = DefaultGraphQLOptions;
      }
      String gqlUrl = this.endpoints.getTrace() + "/graphql";

      // delegate the graphql request execution
      ResponseEntity<T> response = GraphQlRequest.request(gqlUrl, this.getAuthorizationHeader(null), queryStr,
            variables, tclass);
      if (response.getStatusCode() == HttpStatus.OK) {
         // if the response is empty, throw.
         if (!response.hasBody())
            throw new TraceSdkException("The graphql response is empty.");
      } else {
         Integer retry = opts.getRetry();
         // handle errors explicitly
         // extract the status from the error response
         // if 401 and retry > 0 then we can retry
         if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && retry > 0) {
            // unauthenticated request might be because token expired
            // clear token and retry
            this.clearToken();
            opts.setRetry(--retry);
            return this.graphql(query, variables, opts, tclass);
         }
         // otherwise rethrow
         throw new TraceSdkException(response.getBody().toString());
      }
      return response.getBody();

   }

   /**
    * Uploads an array of files to media-api.
    *
    * @param files the file wrappers to upload
    * @return
    * @return the array of corresponding media records
    * @throws TraceSdkException
    * @throws ExecutionException
    * @throws InterruptedException
    */
   public void uploadFiles(FileWrapper[] files) throws InterruptedException, ExecutionException, TraceSdkException {

      Map<String, ByteBuffer> fileMap = new HashMap<String, ByteBuffer>();
      for (FileWrapper fileW : files) {
         FileInfo info = fileW.info();
         fileMap.put(info.getName(), fileW.encryptedData());
      }
      String fileUrl = this.endpoints.getMedia() + "/files";

      GraphQlRequest.uploadFiles(fileUrl, this.getAuthorizationHeader(null), fileMap, MediaRecord[].class);

   }

   /**
    * Downloads a file corresponding to a media record.
    *
    * @param file the file record to download
    * @return the file data blob (Buffer)
    * @throws TraceSdkException
    * @throws HttpError
    */
   public ByteBuffer downloadFile(FileRecord file) throws TraceSdkException, HttpError {
      final int BUFFER_SIZE = 4096;
      String downloadURL = this.get(Service.MEDIA, "/files/" + file.getDigest() + "/info", null, null);
      ByteBuffer byteBuffer = null;
      try {
         URL url = new URL(downloadURL);
         HttpURLConnection httpConn;
         if (this.proxy != null) {
            httpConn = (HttpURLConnection) url.openConnection(this.proxy);
         } else {
            httpConn = (HttpURLConnection) url.openConnection();
         }
         int status = httpConn.getResponseCode();
         String statusText = httpConn.getResponseMessage();

         // always check HTTP response code first
         if (status != HttpURLConnection.HTTP_OK) {
            throw new HttpError(status, statusText);
         }
         // opens input stream from the HTTP connection
         InputStream inputStream = httpConn.getInputStream();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         byte[] buffer = new byte[BUFFER_SIZE];
         int bytesRead = 0;
         while ((bytesRead = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
         }
         baos.flush();
         byteBuffer = ByteBuffer.wrap(baos.toByteArray());
         baos.close();
         inputStream.close();
      } catch (IOException e) {
         throw new TraceSdkException(e);
      }
      return byteBuffer;
   }

}
