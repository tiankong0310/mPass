package com.ibyte.common.core.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ibyte.common.util.ReflectUtil;
import org.springframework.beans.BeanUtils;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 枚举基类
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 *
 * @param <V>
 */
@JsonDeserialize(using = IEnum.EnumJsonDeserializer.class)
public interface IEnum<V> {
	/**
	 * 存储值
	 * 
	 * @return
	 */
	@JsonValue
	V getValue();

	/**
	 * 国际化的key
	 * 
	 * @return
	 */
	String getMessageKey();

	/**
	 * 等于
	 * 
	 * @param value
	 * @return
	 */
	default boolean is(Object value) {
		return this.getValue().equals(value);
	}

	/**
	 * 值转换
	 * 
	 * @param enumType
	 * @param value
	 * @return
	 */
	static <V, E extends IEnum<V>> E valueOf(Class<E> enumType, Object value) {
		for (E en : enumType.getEnumConstants()) {
			if (en.is(value)) {
				return en;
			}
		}
		return null;
	}

	/**
	 * 枚举类型转换器
	 * 
	 * @author 叶中奇
	 *
	 * @param <V>
	 * @param <E>
	 */
	public class Converter<V, E extends IEnum<V>>
			implements AttributeConverter<E, V> {
		@SuppressWarnings("unchecked")
		private Class<E> enumType = (Class<E>) ReflectUtil.getActualClass(
				this.getClass(), Converter.class, "E");

		@Override
		public V convertToDatabaseColumn(E value) {
            if (value == null) {
                return null;
            }
			return value.getValue();
		}

		@Override
		public E convertToEntityAttribute(V value) {
            if (value == null) {
                return null;
            }
			return IEnum.valueOf(enumType, value);
		}
	}

	/**
	 * 枚举类型转JSON
	 * 
	 * @author 叶中奇
	 */
	public class EnumJsonDeserializer extends JsonDeserializer<IEnum<?>> {
		private static final Map<Class<?>, Class<?>> TYPE_CACHE = new ConcurrentHashMap<>();

		@SuppressWarnings("unchecked")
		@Override
		public IEnum<?> deserialize(JsonParser parser,
				DeserializationContext context)
				throws IOException, JsonProcessingException {
			// 目标对象
			Object currentValue = parser.getCurrentValue();
			// 目标对象的字段
			String currentName = parser.currentName();
			// 枚举类型以及值类型
			Class<IEnum<?>> enumType = (Class<IEnum<?>>) BeanUtils
					.findPropertyType(
							currentName, currentValue.getClass());
			Class<?> valueType = TYPE_CACHE.get(enumType);
			if (valueType == null) {
				valueType = ReflectUtil.getActualClass(enumType, IEnum.class,
						"V");
				TYPE_CACHE.put(enumType, valueType);
			}
			// 获取值并转换
			Object value = parser.readValueAs(valueType);
			if (value != null) {
				for (IEnum<?> e : enumType.getEnumConstants()) {
					if (e.getValue().equals(value)) {
						return e;
					}
				}
			}
			return null;
		}
	}
}
