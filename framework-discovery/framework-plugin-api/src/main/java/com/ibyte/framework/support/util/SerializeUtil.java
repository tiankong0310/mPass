package com.ibyte.framework.support.util;


import com.ibyte.common.util.JsonUtil;

/**
 * 序列化操作小工具
 *
 * @author li.Shangzhi
 * @Date: 2019-10-17
 */
public class SerializeUtil {
	/**
	 * Object转String
	 */
	public static String toString(Object object) {
		if (object instanceof String) {
			return (String) object;
		}
		return JsonUtil.toJsonString(object);
	}

	/**
	 * String转Object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseObject(String text, Class<T> type) {
		if (type == String.class) {
			return (T) text;
		}
		return JsonUtil.parseObject(text, type);
	}
}
