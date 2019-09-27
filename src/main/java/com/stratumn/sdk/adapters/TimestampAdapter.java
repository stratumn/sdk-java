package com.stratumn.sdk.adapters;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/***
 *  Enables serialization of path
 */
public class TimestampAdapter implements JsonDeserializer<Date> {

	@Override
	public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
		String date = element.getAsString();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		try {
			return formatter.parse(date);
		} catch (ParseException pe) {
            try {
            return new Date(element.getAsLong());
            } catch (Exception e){
                System.err.printf("Failed to parse Date due to: %s\n", e.getMessage());
                return null;
            }
		}
	}
}
