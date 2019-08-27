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
package com.stratumn.sdk.graph;

import java.net.Proxy;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/***
 * A wrapper class for executing the GraphQL request and returning the response.
 *
 */
public class GraphQlRequest {

   /**
    * Executes the query and returns a responseEntity of type passed
    * 
    * @param url
    * @param auth
    * @param query
    * @param Variables
    * @param tClass
    * @return
    */
   public static <T> ResponseEntity<T> request(String url, String auth, String query, Map<String, Object> Variables,
         Class<T> tClass, Proxy proxy) {

      GraphQlQuery topologyQuery = new GraphQlQuery();

      // Use a singletonMap to retain the object name
      topologyQuery.setVariables(Variables);
      topologyQuery.setQuery(query);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", auth);

      HttpEntity<GraphQlQuery> entity = new HttpEntity<GraphQlQuery>(topologyQuery, headers);

      SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

      if (proxy != null) {
         requestFactory.setProxy(proxy);
      }
      RestTemplate restTemplate = new RestTemplate(requestFactory);

      ResponseEntity<T> resp = restTemplate.postForEntity(url, entity, tClass);
      // System.out.println(resp.getBody());
      return resp;

   }

   /***
    * Expects a map of filename, file buffer data
    * 
    * @param filesMap
    * @return
    */
   public static <T> ResponseEntity<T> uploadFiles(String url, String auth, Map<String, ByteBuffer> filesMap,
         Class<T> tClass) {
      MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      for (Entry<String, ByteBuffer> file : filesMap.entrySet())
         bodyMap.add(file.getKey(), new ByteArrayResource(file.getValue().array(), file.getKey()));

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<T> response = restTemplate.postForEntity(url, requestEntity, tClass);

      return response;
   }

}
