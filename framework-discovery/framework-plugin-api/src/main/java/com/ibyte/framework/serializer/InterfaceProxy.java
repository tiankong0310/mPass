package com.ibyte.framework.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.ibyte.framework.plugin.Plugin;

import java.io.IOException;

/**
 * 通过代理对接口进行序列化与反序列化
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
public class InterfaceProxy {
	/**
	 * 反序列化
	 */
	public static class Deserializer
			extends JsonDeserializer<Object> {
		@Override
		public Object deserialize(JsonParser parser,
				DeserializationContext context)
				throws IOException, JsonProcessingException {
			return Plugin.getApi(parser.getValueAsString());
		}
	}

	/**
	 * 序列化
	 */
	public static class Serializer
			extends JsonSerializer<Object> {
		@Override
		public void serialize(Object value,
				JsonGenerator generator, SerializerProvider provider)
				throws IOException {
			String className = value.getClass().getName();
			int index = className.indexOf("$$");
			if (index > -1) {
				className = className.substring(0, index);
			}
			generator.writeObject(className);
		}
	}
}
