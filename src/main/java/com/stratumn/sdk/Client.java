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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.stratumn.chainscript.utils.CryptoUtils;
import com.stratumn.chainscript.utils.JsonHelper;
import com.stratumn.sdk.graph.GraphQl;
import com.stratumn.sdk.graph.GraphQlQuery;
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
 * A wrapper client to handle communication
 * with account, trace and media via REST and GraphQL
 *
 * The Client will handle (re-)authentication if
 * a token is not present yet or expired.
 *
 * The Client exposes 3 main methods:
 * - get
 * - post
 * - graphql 
 */
public class Client
{
   /**
    * The default GraphQL options:
    * - retry once
    */
   private static final GraphQLOptions  DefaultGraphQLOptions =new GraphQLOptions(1);
  

   private static final FetchOptions DefaultFetchOptions = new FetchOptions();

   /**
    * The endpoint urls for all the services
    */
   private Endpoints endpoints;
  
   /**
    * The token received from account service after authentication
    */
   private String token;

   private Proxy proxy;

   private RestTemplate restTemplate;

   /***
    * Constructs a new instance of the Client
    * @param opts
    */
   public Client(ClientOptions opts)
   {
      this.endpoints = Helpers.makeEndpoints(opts.getEndpoints());
       
      this.options = opts;
      initRestTemplate();

   }

   /***
    * Initializes the restTemplate
    */
   private void initRestTemplate() {
      restTemplate = new RestTemplate();
      GsonHttpMessageConverter converter = null;
      // find existing converter
      Iterator<HttpMessageConverter<?>> convIterator = restTemplate.getMessageConverters().iterator();
      while (convIterator.hasNext()) {
         HttpMessageConverter<?> conv = convIterator.next();
         if (conv instanceof GsonHttpMessageConverter) {
            converter = (GsonHttpMessageConverter) conv;
            break;
         }
      }
      // create converter if not found
      if (converter == null) {
         converter = new GsonHttpMessageConverter();
         restTemplate.getMessageConverters().add(converter);
      }
      converter.setGson(JsonHelper.getGson());

   }

   /***
    * Initializes the restTemplate 
    */
   private void initRestTemplate()
   {
      if (options.isEnableDebuging())
      {
         restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
         restTemplate.getInterceptors().add(new LoggingRequestInterceptor());
      
      }
      else
         restTemplate = new RestTemplate();
      
      
      GsonHttpMessageConverter converter = null;
      //find existing converter
      Iterator<HttpMessageConverter<?>> convIterator = restTemplate.getMessageConverters().iterator();
      while(convIterator.hasNext())
      {
         HttpMessageConverter<?> conv = convIterator.next();
         if(conv instanceof GsonHttpMessageConverter)
         {
            converter = (GsonHttpMessageConverter) conv;
            break;
         }
      }
      //create converter if not found
      if(converter == null)
      {
         converter = new GsonHttpMessageConverter();
         restTemplate.getMessageConverters().add(converter);
      }
      converter.setGson(JsonHelper.getGson());
      
   }

   /**
    * Compute the bearer Authorization header of format "Bearer my_token". If the
    * token is undefined, the return header is an empty string "".
    *
    * @param token optional token to be used
    */
   private String makeAuthorizationHeader(String token)
   {
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
         if (opts.getSkipAuth() != null && opts.getSkipAuth())
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
   private void setToken(String token)
   {
      this.token = token;
   }

   /**
    * To clear the existing token
    */
   private void clearToken()
   {
      this.token = null;
   }

   /**
    * Utility method to fetch a ressource on a target service via REST.
    *
    * @param request its instance from HttpHelpers
    * @throws HttpError
    * @return the responseContent
    */
   private String fetch(HttpHelpers request, int retry) throws HttpError {
      String responseContent = null;
      try {
         request.sendData();
         HttpURLConnection con = request.getConnection();
         // first check the status.
         int status = con.getResponseCode();
         Boolean ok = (status < HttpURLConnection.HTTP_BAD_REQUEST)
               || (200 <= con.getResponseCode() && con.getResponseCode() <= 299);
         if (!ok) {
            // if 401 and retry > 0 then we can retry
            if(status == 401 && retry > 0)
            {
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
         responseContent = (String) request.read();
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
   private void loginWithSigningPrivateKey(String pemPrivateKey) throws Exception
   {

      PrivateKey privateKey = CryptoUtils.decodePrivateKey(pemPrivateKey);

      String signedToken = Helpers.makeAuthPayload(privateKey);

      String tokenResponse = this.get(Service.ACCOUNT, "login", null, new FetchOptions(signedToken, false, null));
      JsonObject tokenJson = JsonHelper.fromJson(tokenResponse, JsonObject.class);
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
   private void loginWithCredentials(String email, String password) throws TraceSdkException
   {
      // get the user salt first
      // use skipAuth = true to bypass authentication
      // GET /salt is a public route!

      Map<String, String> parameters = new HashMap<>();
      parameters.put("email", email);

      String saltResponse = this.get(Service.ACCOUNT, "salt", parameters, (new FetchOptions(null, true, 0)));
      JsonObject saltJson = JsonHelper.fromJson(saltResponse, JsonObject.class);
      String salt = saltJson.get("salt").getAsString();

      // hash the password with the salt
      String passwordHash = org.springframework.security.crypto.bcrypt.BCrypt.hashpw(password, salt);

      parameters.put("passwordHash", passwordHash);
      String bodyJson = JsonHelper.toJson(parameters);

      // post the login payload
      // use skipAuth = true to bypass authentication
      // POST /login is a public route!
      String tokenResponse = this.post(Service.ACCOUNT, "login", bodyJson, (new FetchOptions(null, true, 0)));
      JsonObject tokenJson = JsonHelper.fromJson(tokenResponse, JsonObject.class);
      // finally set the new token
      this.setToken(tokenJson.get("token").getAsString());

   }

   /**
    * Authenticates using a valid secret. Supported secret types are: -
    * CredentialSecret -> via email+password - PrivateKeySecret -> via signed
    * message
    * @throws TraceSdkException  
    */
   private synchronized void login() throws TraceSdkException
   {

      // if another concurrent execution has already
      // done the job, then release and return, nothing to do.
      if(this.token != null) return;

      // otherwise do the job...
      if(Secret.isCredentialSecret(options.getSecret()))
      {
         // the CredentialSecret case
         final String email = ((CredentialSecret) options.getSecret()).getEmail();
         final String password = ((CredentialSecret) options.getSecret()).getPassword();
         try
         {
            this.loginWithCredentials(email, password);
         }
         catch(TraceSdkException e)
         {
            throw new TraceSdkException("Login with email password failed.", e);
         }
      }
      else
         if(Secret.isPrivateKeySecret(options.getSecret()))
         {
            // the PrivateKeySecret case
            final String privateKey = ((PrivateKeySecret) options.getSecret()).getPrivateKey();
            try
            {
               this.loginWithSigningPrivateKey(privateKey);
            }
            catch(Exception e)
            {
               throw new TraceSdkException("Login with private key failed", e);
            }
         }
         else
            if(Secret.isProtectedKeySecret(options.getSecret()))
            {
               // the ProtectedKeySecret case
               // not handled yet
               throw new TraceSdkException("Authentication via password protected key is not handled");
            }
            else
            {
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
    * @return the response body object
    */
   public String post(Service service, String route, String body, FetchOptions opts) throws TraceSdkException {
      try {
         // create default fetch options.
         if (opts == null)
            opts = DefaultFetchOptions;

         // References: https://www.baeldung.com/java-http-request
         // https://juffalow.com/java/how-to-send-http-get-post-request-in-java
         String path = this.endpoints.getEndpoint(service) + '/' + route;
         URL url = new URL(path);

         HttpURLConnection con;
         if(options.getProxy() != null)
         {
            con = (HttpURLConnection) url.openConnection(options.getProxy());
         }
         else
         {
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

      }
      catch(Exception e)
      {
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
    * @return the response body object
    */
   public String get(Service service, String route, Map<String, String> params, FetchOptions opts)
         throws TraceSdkException {

      try {
         // create default fetch options.
         if (opts == null)
            opts = DefaultFetchOptions;

         String path = this.endpoints.getEndpoint(service) + '/' + route;

         if(params != null)
         {
            path += "?" + HttpHelpers.getParamsString(params);
         }

         URL url = new URL(path);

         HttpURLConnection con;
         if(options.getProxy() != null)
         {
            con = (HttpURLConnection) url.openConnection(options.getProxy());
         }
         else
         {
            con = (HttpURLConnection) url.openConnection();
         }
         con.setRequestProperty("Content-Type", "application/json; utf-8");
         con.setRequestProperty("Accept", "application/json");
         con.setRequestProperty("Authorization", this.getAuthorizationHeader(opts));
         con.setRequestMethod("GET");

         HttpHelpers request = new HttpHelpers(con);

         // delegate to fetch wrapper
         return this.fetch(request, opts.getRetry());
      }
      catch(Exception e)
      {
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
   
    */
   public <T> T graphql(GraphQl.Query query, Map<String, Object> variables, GraphQLOptions opts, Class<T> tclass) throws TraceSdkException
   {

      String queryStr;
      try
      {
         queryStr = query.loadQuery();
      }
      catch(IOException e)
      {
         throw new TraceSdkException("Error loading query", e);
      }
      if(opts == null)
      {
         opts = DefaultGraphQLOptions;
      }
      String gqlUrl = this.endpoints.getTrace() + "/graphql";
      GraphQlQuery topologyQuery = new GraphQlQuery(variables, queryStr);
      // delegate the graphql request execution
      ResponseEntity<T> response = postForEntity(gqlUrl, topologyQuery, tclass);
      if (response.getStatusCode() == HttpStatus.OK) {
         // if the response is empty, throw.
         if(!response.hasBody()) throw new TraceSdkException("The graphql response is empty.");
      }
      else
      {
         Integer retry = opts.getRetry();
         // handle errors explicitly 
         // extract the status from the error response 
         // if 401 and retry > 0 then we can retry
         if(response.getStatusCode() == HttpStatus.UNAUTHORIZED && retry > 0)
         {
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
    * @param fileWrapperList the file wrappers to upload
    * @return the array of corresponding media records
    * @throws TraceSdkException
    */
   public MediaRecord[] uploadFiles(List<FileWrapper> fileWrapperList) throws TraceSdkException {

      if (fileWrapperList.size() == 0)
         return new MediaRecord[0];

      String fileUrl = this.endpoints.getMedia() + "/files";
      MediaRecord[] mediaRecords = uploadFiles(fileUrl, fileWrapperList, MediaRecord[].class);

      return mediaRecords;
   }

   /**
    * Downloads a file corresponding to a media record.
    *
    * @param fileRecord the file record to download
    * @return the file data blob (Buffer)
    * @throws TraceSdkException 
    * @throws HttpError 
    */
   public ByteBuffer downloadFile(FileRecord fileRecord) throws TraceSdkException, HttpError {
      String tokenResponse = this.get(Service.MEDIA, "files/" + fileRecord.getDigest() + "/info", null, null);
      JsonObject tokenJson = JsonHelper.fromJson(tokenResponse, JsonObject.class);
      // finally set the new token
      String downloadURL = tokenJson.get("download_url").getAsString();

      final int BUFFER_SIZE = 4096;

      ByteBuffer byteBuffer = null;
      try
      {
         URL url = new URL(downloadURL);
         HttpURLConnection httpConn;
         if(options.getProxy() != null)
         {
            httpConn = (HttpURLConnection) url.openConnection(options.getProxy());
         }
         else
         {
            httpConn = (HttpURLConnection) url.openConnection();
         }
         // does not need authorization header
         int status = httpConn.getResponseCode();
         String statusText = httpConn.getResponseMessage();
         // always check HTTP response code first
         if(status != HttpURLConnection.HTTP_OK)
         {
            throw new HttpError(status, statusText);
         }
         // opens input stream from the HTTP connection
         InputStream inputStream = httpConn.getInputStream();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         byte[] buffer = new byte[BUFFER_SIZE];
         int bytesRead = 0;
         while((bytesRead = inputStream.read(buffer)) != -1)
         {
            baos.write(buffer, 0, bytesRead);
         }
         baos.flush();
         byteBuffer = ByteBuffer.wrap(baos.toByteArray());
         baos.close();
         inputStream.close();
      }
      catch(IOException e)
      {
         throw new TraceSdkException(e);
      }
      return byteBuffer;
   }

   /**
    * Executes the query and returns a responseEntity of type passed
    * 
    * @param url
    * @param auth
    * @param query
    * @param Variables
    * @param tClass
    * @return
    * @throws TraceSdkException
    * 
    */
   private <T, R> ResponseEntity<T> postForEntity(String url, R requestBody, Class<T> tClass) throws TraceSdkException {

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set(HttpHeaders.AUTHORIZATION, this.getAuthorizationHeader(null));

      HttpEntity<R> entity = new HttpEntity<R>(requestBody, headers);

      // System.out.println (JsonHelper.toJson(entity));
      ResponseEntity<T> resp = restTemplate.postForEntity(url, entity, tClass, this.proxy);

      return resp;

   }

   /***
    * Expects a list of fileWrappers, uploads the files encrypted and returns
    * response
    * 
    * @param filesList
    * @return
    * @throws TraceSdkException
    */
   private <T> T uploadFiles(String url, List<FileWrapper> filesList, Class<T> tClass) throws TraceSdkException {

      MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers.set(HttpHeaders.AUTHORIZATION, this.getAuthorizationHeader(null));

      for (FileWrapper file : filesList) {

         FileInfo info = file.info();
         // This nested HttpEntiy is important to create the correct
         // Content-Disposition entry with metadata "name" and "filename"
         MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
         ContentDisposition contentDisposition = ContentDisposition.builder("form-data").name(info.getName())
               .filename(info.getName()).build();
         fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
         HttpEntity<byte[]> fileEntity = new HttpEntity<>(file.encryptedData().array(), fileMap);

         bodyMap.add("files", fileEntity);
      }
      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

      ResponseEntity<T> response = restTemplate.postForEntity(url, requestEntity, tClass, this.proxy);

      return response.getBody();
   }

}
