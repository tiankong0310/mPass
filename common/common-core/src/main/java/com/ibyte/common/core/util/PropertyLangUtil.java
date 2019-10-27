package com.ibyte.common.core.util;

import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.i18n.ResourceUtil;
import com.ibyte.common.core.constant.IEnum;
import com.ibyte.common.util.DateUtil;
import com.ibyte.common.util.LangUtil;
import com.ibyte.common.util.ReflectUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.meta.Meta;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.meta.MetaEntity;
import com.ibyte.framework.meta.MetaProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 多语言工具类
 *
 * @author li.shangzhi
 */
@Slf4j
public class PropertyLangUtil {

	/**
	 * 系统支持的多语言（国家简称，首字母大写）
	 */
	private static Set<String> langs = null;

	/**
	 * 获取当前语言的属性值（用于数据多语言）
	 */
	public static String getPropertyByLanguage(IEntity entity, String name) {
		return getPropertyByLanguage(entity, name,
				ResourceUtil.currentLocale());
	}

	/**
	 * 获取指定语言的属性值（用于数据多语言）
	 */
	public static String getPropertyByLanguage(IEntity entity, String name,
			Locale locale) {
		MetaEntity metaEntity = Meta
				.getEntity(EntityUtil.getEntityClassName(entity));
		MetaProperty metaProp = metaEntity.getProperty(name);
		if (metaProp == null || !metaProp.isLangSupport()) {
			try {
				return format(ReflectUtil.getProperty(entity, name), metaProp,
						locale);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		} else {
			String langName = getPropertyNameByLanguage(name, locale);
			Map<String, Object> props = entity.getDynamicProps();
			String text = props == null || langName == null ? null
					: (String) props.get(langName);
			if (StringUtils.isBlank(text)) {
				try {
					return ReflectUtil.getProperty(entity, name);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			} else {
				return text;
			}
		}
	}

	/**
	 * 格式化成显示字符
	 */
	public static String format(Object value, MetaProperty metaProp,
			Locale locale) {
		if (value == null) {
			return null;
		}
		if (value instanceof Collection) {
			Collection<?> list = (Collection<?>) value;
			if (list.isEmpty()) {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			for (Object v : list) {
				if (sb.length() > 0) {
					sb.append(";");
				}
				sb.append(format(v, metaProp, locale));
			}
			return sb.toString();
		} else if (value instanceof IEntity) {
			IEntity entity = (IEntity) value;
			MetaEntity metaEntity = Meta
					.getEntity(EntityUtil.getEntityClassName(entity));
			String name = metaEntity.getDisplayProperty();
			if (name == null) {
				return entity.getFdId();
			} else {
				return getPropertyByLanguage(entity, name, locale);
			}
		} else if (value instanceof Date) {
			String type = metaProp == null ? MetaConstant.TYPE_DATETIME
					: metaProp.getType();
			return DateUtil.convertDateToString((Date) value, type, locale);
		} else if (value instanceof IEnum) {
			return ResourceUtil.getString(((IEnum<?>) value).getMessageKey(),
					null, locale);
		}
		return String.valueOf(value);
	}

	/**
	 * 取当前语言的字段属性名，不支持多语言或当前语言环境不在支持范围内的时候，返回null
	 */
	public static String getPropertyNameByLanguage(String name) {
		return getPropertyNameByLanguage(name, ResourceUtil.currentLocale());
	}

	/**
	 * 取指定语言的字段属性名，不支持多语言或当前语言环境不在支持范围内的时候，返回null
	 */
	public static String getPropertyNameByLanguage(String name, Locale locale) {
		String country = StringHelper
				.toFirstUpperCase(locale.getCountry().toLowerCase());
		Set<String> supportLangs = LangUtil.getSupportCountries();
		if (supportLangs.contains(country)) {
			return StringHelper.join(name, country);
		}
		return null;
	}

}
