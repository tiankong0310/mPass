package com.ibyte.framework.serializer;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.ibyte.framework.support.util.PluginReflectUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 适用于value.getClass==key的Map的序列化与反序列化
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
public class ClassKeyMap {
	/**
	 * 反序列化
	 */
	public static class Deserializer
			extends JsonDeserializer<Map<String, Object>> {
		@Override
		public Map<String, Object> deserialize(JsonParser parser,
				DeserializationContext context)
				throws IOException, JsonProcessingException {
			Map<String, Object> result = new HashMap<>(16);
			ObjectCodec codec = parser.getCodec();
			// 读取值
			TreeNode tree = parser.readValueAsTree();
			for (Iterator<String> it = tree.fieldNames(); it.hasNext();) {
				// 遍历key
				String field = it.next();
				Class<?> clazz = PluginReflectUtil.classForName(field);
				TreeNode treeValue = tree.get(field);
				if (clazz == null) {
					Object value = codec.treeToValue(treeValue,
							JSONObject.class);
					result.put(field, value);
				} else {
					Object value = codec.treeToValue(treeValue, clazz);
					result.put(field, value);
				}
			}
			return result;
		}
	}

	/**
	 * 序列化
	 */
	public static class Serializer
			extends JsonSerializer<Map<String, Object>> {
		@Override
		public void serialize(Map<String, Object> value,
				JsonGenerator generator, SerializerProvider provider)
				throws IOException {
			generator.writeObject(value);
		}
	}
}
