package com.ibyte.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * JSON工具类
 * 
 * @author li.Shangzhi
 *
 */
public class JsonUtil {
	private static ObjectMapper MAPPER = new ObjectMapper();

	static {
		MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
	}

	public static ObjectMapper getMapper() {
		return MAPPER;
	}

	public static void setMapper(ObjectMapper mapper) {
		JsonUtil.MAPPER = mapper;
	}

	/**
	 * 序列化成String
	 */
	public static String toJsonString(Object object) {
		try {
			return MAPPER.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("JSON序列化失败", e);
		}
	}

	/**
	 * String -> 指定类型
	 */
	public static <T> T parseObject(String json, Class<T> clz) {
		try {
			return MAPPER.readValue(json, clz);
		} catch (IOException e) {
			throw new RuntimeException("JSON反序列化失败", e);
		}
	}

	/**
	 * String -> 指定类型（常用于带泛型的类型）<br>
	 * JavaType可以由JsonUtil.getMapper().getTypeFactory()构造
	 */
	public static <T> T parseObject(String json, JavaType type) {
		try {
			return MAPPER.readValue(json, type);
		} catch (IOException e) {
			throw new RuntimeException("JSON反序列化失败", e);
		}
	}

	/**
	 * 类型转换
	 */
	public static Object convert(Object srcValue, Type type) {
		return convert(srcValue,
				type == null ? null : MAPPER.constructType(type));
	}

	/**
	 * 类型转换
	 */
	public static Object convert(Object srcValue, JavaType type) {
		if (type == null) {
			return srcValue;
		}
		// 解Optional
		Object tarValue = srcValue;
		if (srcValue != null && srcValue instanceof Optional) {
			Optional<?> o = (Optional<?>) srcValue;
			if (o.isPresent()) {
				tarValue = o.get();
			} else {
				tarValue = null;
			}
		}
		// 转换
		Class<?> raw = type.getRawClass();
		if (raw == Optional.class) {
			if (tarValue == null) {
				return Optional.empty();
			}
			tarValue = MAPPER.convertValue(tarValue,
					type.containedType(0));
			if (tarValue == null) {
				return Optional.empty();
			} else {
				return Optional.of(tarValue);
			}
		} else {
			if (tarValue == null) {
				return null;
			}
			return MAPPER.convertValue(tarValue, type);
		}
	}
}
