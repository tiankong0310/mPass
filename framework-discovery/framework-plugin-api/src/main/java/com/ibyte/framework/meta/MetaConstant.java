package com.ibyte.framework.meta;

import org.apache.commons.lang3.StringUtils;

/**
 * 元数据常量
 *
 * @author li.Shangzhi
 * @Date: 2019-10-17
 */
public interface MetaConstant {
	String TYPE_STRING = "String";
	String TYPE_RTF = "RTF";
	String TYPE_BLOB = "BLOB";
	String TYPE_BOOLEAN = "Boolean";
	String TYPE_INTEGER = "Integer";
	String TYPE_LONG = "Long";
	String TYPE_FLOAT = "Float";
	String TYPE_DOUBLE = "Double";
	String TYPE_BIGDECIMAL = "BigDecimal";
	String TYPE_DATE = "Date";
	String TYPE_DATETIME = "DateTime";
	String TYPE_TIME = "Time";

	String CASCADE_NONE = "none";

	/**
	 * 显示类型
	 * 
	 * @author 叶中奇
	 */
	public enum ShowType {
		/** 自动选择 */
		AUTO,
		/** 永不显示 */
		NONE,
		/** 仅在详情页显示 */
		DETAIL_ONLY,
		/** 总是显示 */
		ALWAYS;
	}

	/**
	 * 是否枚举
	 * 
	 * @param prop
	 * @return
	 */
	static boolean isEnum(MetaProperty prop) {
		return StringUtils.isBlank(prop.getEnumClass());
	}

	/**
	 * 是否关联字段
	 * 
	 * @param type
	 * @return
	 */
	static boolean isAssociation(String type) {
		return type.indexOf('.') > -1;
	}

	/**
	 * 是否字符串
	 * 
	 * @param type
	 * @return
	 */
	static boolean isString(String type) {
		return TYPE_STRING.equalsIgnoreCase(type)
				|| TYPE_RTF.equalsIgnoreCase(type);
	}

	/**
	 * 是否数字
	 * 
	 * @param type
	 * @return
	 */
	static boolean isNumber(String type) {
		return TYPE_INTEGER.equalsIgnoreCase(type)
				|| TYPE_LONG.equalsIgnoreCase(type)
				|| TYPE_FLOAT.equalsIgnoreCase(type)
				|| TYPE_DOUBLE.equalsIgnoreCase(type)
				|| TYPE_BIGDECIMAL.equalsIgnoreCase(type);
	}

	/**
	 * 是否日期
	 * 
	 * @param type
	 * @return
	 */
	static boolean isDate(String type) {
		return TYPE_DATE.equalsIgnoreCase(type)
				|| TYPE_DATETIME.equalsIgnoreCase(type)
				|| TYPE_TIME.equalsIgnoreCase(type);
	}

	/**
	 * 是否Lob
	 * 
	 * @param type
	 * @return
	 */
	static boolean isLob(String type) {
		return TYPE_RTF.equalsIgnoreCase(type) || TYPE_BLOB.equals(type);
	}

	/**
	 * 是否级联更新
	 * 
	 * @param cascade
	 * @return
	 */
	static boolean isCascade(String cascade) {
		return cascade != null && !cascade.equals(CASCADE_NONE);
	}
}
