package com.ibyte.common.core.util;

import com.alibaba.fastjson.util.TypeUtils;
import com.ibyte.common.util.ReflectUtil;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.meta.MetaProperty;
import com.ibyte.common.core.constant.IEnum;
import com.ibyte.common.core.data.IData;
import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.core.service.IService;
import com.ibyte.common.exception.ParamsNotValidException;
import com.ibyte.common.util.StringHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 数据格式转换
 *
 * @author li.shangzhi
 */
public class TypeUtil {
	static final String[] IDKEYS = { "fdId", "id" };
	static final String CURRENT = "${current}";

	/**
	 * 转换数据格式，数组/列表会被转成List
	 */
	public static Object cast(Object value, MetaProperty prop) {
		// 空值
		if (value == null) {
			return null;
		}
		if (MetaConstant.isEnum(prop)) {
			// 枚举
			return cast(value, ReflectUtil.classForName(prop.getEnumClass()));
		} else {
			// 其他
			return cast(value, EntityUtil.getPropertyType(prop.getType()));
		}
	}

	/**
	 * 转换数据格式，数组/列表会被转成List
	 */
	public static Object cast(Object value, Class<?> clazz) {
		// 空值
		if (value == null) {
			return null;
		}
		// 数组
		if (value instanceof Object[]) {
			return castArray((Object[]) value, clazz);
		}
		// 列表
		if (value instanceof Collection) {
			return castList((Collection<?>) value, clazz);
		}
		// 单值
		return castObject(value, clazz);
	}

	/**
	 * 转换数据格式，数组/列表会取第一个值
	 */
	@SuppressWarnings("unchecked")
	public static <T> T castToSingle(Object value, Class<?> clazz) {
		// 空值
		if (value == null) {
			return null;
		}
		// 数组
		if (value instanceof Object[]) {
			Object[] array = (Object[]) value;
			if (array.length == 0) {
				return null;
			}
			return castToSingle(array[0], clazz);
		}
		// 列表
		if (value instanceof Collection) {
			Collection<?> coll = (Collection<?>) value;
			if (coll.isEmpty()) {
				return null;
			}
			return castToSingle(coll.iterator().next(), clazz);
		}
		// 单值
		return (T) castObject(value, clazz);
	}

	/**
	 * 转换数据格式
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> castToList(Object value, Class<?> clazz) {
		// 空值
		if (value == null) {
			return null;
		}
		// 以分号分隔的字符串
		if (value instanceof String) {
			String val = (String) value;
			if (StringUtils.isBlank(val)) {
				return new ArrayList<>();
			}
			if (IEntity.class.isAssignableFrom(clazz)
					|| IEnum.class.isAssignableFrom(clazz)) {
				value = val.trim().split(";");
			}
		}
		// 转换
		Object result = cast(value, clazz);
		if (result == null) {
			return null;
		}
		if (result instanceof List) {
			return (List<T>) result;
		}
		List<T> list = new ArrayList<>();
		list.add((T) result);
		return list;
	}

	/**
	 * 转换数组
	 */
	static Object castArray(Object[] value, Class<?> clazz) {
		List<Object> list = new ArrayList<>();
		for (Object val : value) {
			val = cast(val, clazz);
			if (val != null) {
				list.add(val);
			}
		}
		return list;
	}

	/**
	 * 转换列表
	 */
	static Object castList(Collection<?> value, Class<?> clazz) {
		List<Object> list = new ArrayList<>();
		for (Object val : value) {
			val = cast(val, clazz);
			if (val != null) {
				list.add(val);
			}
		}
		return list;
	}

	/**
	 * 转换单值
	 */
	static Object castObject(Object value, Class<?> clazz) {
		if (clazz.isAssignableFrom(value.getClass())) {
			return value;
		}
		if (CURRENT.equals(value)) {
			if (Date.class.isAssignableFrom(clazz)) {
				return new Date();
			}
		}
		if (IEntity.class.isAssignableFrom(clazz)) {
			return castEntity(value, clazz);
		}
		if (IEnum.class.isAssignableFrom(clazz)) {
			return castEnum(value, clazz);
		}
		if (String.class.equals(clazz) && value instanceof IData) {
			if (value instanceof IData) {
				return ((IData) value).getFdId();
			} else if (value instanceof Map) {
				for (String key : IDKEYS) {
					Object val = ((Map<?, ?>) value).get(key);
					if (val != null && val instanceof String) {
						return (String) val;
					}
				}
			}
		}
		return TypeUtils.cast(value, clazz, null);
	}

	/**
	 * 转换Entity
	 */
	static Object castEntity(Object value, Class<?> clazz) {
		String id = null;
		if (value instanceof String) {
			id = (String) value;
		} else if (value instanceof Map) {
			for (String key : IDKEYS) {
				Object val = ((Map<?, ?>) value).get(key);
				if (val != null && val instanceof String) {
					id = (String) val;
					break;
				}
			}
		}
		IService<?, ?> service = EntityUtil.getEntityService(clazz.getName());
		if (service != null) {
			return service.getOne(id);
		}
		throw new ParamsNotValidException(StringHelper.join("无法转换类型：",
				value.getClass().getName(), "->", clazz.getName()));
	}

	/**
	 * 转换枚举
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Object castEnum(Object value, Class<?> clazz) {
		Class<?> valueClass = ReflectUtil.getActualClass(clazz, IEnum.class,
				"V");
		Object enumValue = TypeUtils.cast(value, valueClass, null);
		return IEnum.valueOf((Class<IEnum>) clazz, enumValue);
	}

	/**
	 * 转换简单类型
	 */
	static Object castSimple(Object value, String type) {
		Class<?> clazz = EntityUtil.TYPEMAP.get(type);
		if (clazz == null) {
			throw new ParamsNotValidException(StringHelper.join("无法转换类型：",
					value.getClass().getName(), "->", type));
		} else {
			return TypeUtils.cast(value, clazz, null);
		}
	}
}
