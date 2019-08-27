package com.stratumn.sdk.graph;

import java.util.Map;

/**
 * Class to hold the query and its parameters
 */
public class GraphQlQuery
{
   public GraphQlQuery()
   { 
   }

   private Map<String, Object> variables;

   private String query;

   public Map<String, Object> getVariables()
   {
      return variables;
   }

   public void setVariables(Map<String, Object> variables)
   {
      this.variables = variables;
   }

   public String getQuery()
   {
      return query;
   }

   public void setQuery(String query)
   {
      this.query = query;
   }

   @Override
   public String toString()
   {
      return "GraphQLQuery [variables=" + variables + ", query=" + query + "]";
   }
}
