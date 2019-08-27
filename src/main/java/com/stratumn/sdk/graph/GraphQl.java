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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.stratumn.chainscript.Constants;

/***
 * Class that defines all Graph QL objects  
 *
 */
public class GraphQl
{

   public enum Query {
      MUTATION_CREATELINK("/Mutations/CreateLink", ImmutableMap.of("${TraceStateFragment}", Fragment.FRAGMENT_TRACESTATE)),
      QUERY_CONFIG("/Queries/Config"),
      QUERY_GETHEADLINK("/Queries/GetHeadLink", ImmutableMap.of("${HeadLinkFragment}", Fragment.FRAGMENT_HEADLINK)),
      QUERY_GETTRACEDETAILS("/Queries/GetTraceDetails",
         ImmutableMap.of("${PaginationInfoOnLinksConnectionFragment}", Fragment.FRAGMENT_PAGINATIONINFO_ONLINKSCONNECTION)),
      QUERY_GETTRACESINSTAGE("/Queries/GetTracesInStage",
         ImmutableMap.of("${TraceStateFragment}", Fragment.FRAGMENT_TRACESTATE, "${PaginationInfoOnTracesConnectionFragment}",
            Fragment.FRAGMENT_PAGINATIONINFO_ONTRACESCONNECTION)),
      QUERY_GETTRACESTATE("/Queries/GetTraceState", ImmutableMap.of("${TraceStateFragment}", Fragment.FRAGMENT_TRACESTATE));

      private String filePath;
      //a map of key and frag file path
      private Map<String, String> subQueriesMap;

      public Map<String, String> getSubQueriesMap()
      {
         return subQueriesMap;
      }

      public String getFilePath()
      {
         return filePath;
      }

      Query(String filePath)
      {
         this.filePath = filePath;
      }

      Query(String filePath, Map<String, Fragment> subQueries)
      {
         this(filePath);
         this.subQueriesMap = new HashMap<String, String>();
         for(Entry<String, Fragment> frag : subQueries.entrySet())
         {
            subQueriesMap.put(frag.getKey(), frag.getValue().getFilePath());
         }

      }

      /******** Query loading ****/
      //cach queries for improved performance
      private static ConcurrentMap<Query, String> cache = new ConcurrentHashMap<Query, String>();

      /***
       * Load the query and caches it
       * @return
       * @throws IOException
       */
      public String loadQuery() throws IOException
      {
         String document;
         if((document = cache.get(this)) == null)
         {
            synchronized(this)
            {
               if((document = cache.get(this)) == null)
               {
                  document = loadDocument(this.getFilePath());
                  if(this.getSubQueriesMap() != null) for(Entry<String, String> subQuery : this.getSubQueriesMap().entrySet())
                  {
                     String subDocument = loadDocument(subQuery.getValue());
                     document = document.replace(subQuery.getKey(), subDocument);
                  }
                  cache.putIfAbsent(this, document);
               }
            }
         }
         return document;

      }

      /**
      * Load the query from the specified file
      * @param filePath
      * @return
      * @throws IOException
      */
      private String loadDocument(String filePath) throws IOException
      {
         URL url = Resources.getResource("graphql" + filePath + ".graphql");
         String sdl = Resources.toString(url, Constants.UTF8);
         return sdl;
      }

   }

   /***
    * Fragments are reusable entities  
    *
    */
   private enum Fragment {
      FRAGMENT_HEADLINK("/Fragments/HeadLink"),
      FRAGMENT_TRACESTATE("/Fragments/TraceState"),
      FRAGMENT_PAGINATIONINFO_ONTRACESCONNECTION("/Fragments/PaginationInfo/OnTracesConnection"),
      FRAGMENT_PAGINATIONINFO_ONLINKSCONNECTION("/Fragments/PaginationInfo/OnLinksConnection");

      private String filePath;

      public String getFilePath()
      {
         return filePath;
      }

      Fragment(String filePath)
      {
         this.filePath = filePath;
      }

   }

  
}
