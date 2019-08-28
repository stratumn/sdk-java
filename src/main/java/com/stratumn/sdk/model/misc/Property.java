package com.stratumn.sdk.model.misc;

import java.util.function.Function;

/***
 * 
 * A Class to hold the data identifying a specific property in an object hierarchy
 * V is the Type of the object we are searching for 
 * Id a unique identifier of the value of the property
 * Path Json like path for a property. 
 * Parent parent object of the property. 
 * @param <V>
 */
public class Property<V extends Identifiable>
{

   public Property(String id, V value, String path, Object parent)
   {
      super();
      this.id = id;
      this.value = value;
      this.path = path;
      this.parent = parent;
   }

   private String id;

   private V value;

   private String path;

   private Object parent;

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public V getValue()
   {
      return value;
   }

   public void setValue(V value)
   {
      this.value = value;
   }

   public String getPath()
   {
      return path;
   }

   public void setPath(String path)
   {
      this.path = path;
   }

   public Object getParent()
   {
      return parent;
   }

   public void setParent(Object parent)
   {
      this.parent = parent;
   }

   @Override
   public String toString()
   {
      return "Property [id=" + id + ", value=" + value + ", path=" + path + "]";
   }
   
   /***
    * Transforms this property from one type to another
    * @param valuebuilder function to build the new value from current one
    * @return
    */
   public <T extends Identifiable> Property<T> transform( Function<V,T> valuebuilder )
   {
      T value = valuebuilder.apply(this.value);
      return new Property<T>(id,value,path,parent);
      
   }

}
