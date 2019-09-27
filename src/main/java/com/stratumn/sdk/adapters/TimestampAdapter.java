package com.stratumn.sdk.adapters;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/***
 *  Enables serialization of path
 */
public class TimestampAdapter implements JsonDeserializer<Date> {


	@Override
	public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
		try {
            JsonObject o = element.getAsJsonObject();
			return new Gson().fromJson(o , Date.class); // default deserialization
		} catch (Exception pe) {
            try {
            return new Date(element.getAsLong());
            } catch (Exception e){
                System.err.printf("Failed to parse Date due to: %s\n %s\n", pe.getMessage(), e.getMessage());
                return null;
            }
		}
	}
}
