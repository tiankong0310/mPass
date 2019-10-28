package com.ibyte.common.core.mapper;

import com.alibaba.fastjson.util.TypeUtils;
import com.ibyte.common.core.data.IData;
import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.core.util.EntityUtil;
import com.ibyte.common.core.util.PropertyLangUtil;
import com.ibyte.common.util.LangUtil;
import com.ibyte.common.util.ReflectUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.meta.MetaConstant.ShowType;
import com.ibyte.framework.meta.MetaEntity;
import com.ibyte.framework.meta.MetaProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.Map.Entry;

import static com.ibyte.common.util.ReflectUtil.accessable;

/**
 * Entity转VO的转换工具
 *
 * @author li.Shangzhi
 * @Date: 2019-10-28
 */
@Slf4j
public class EntityToVoMapper extends AbstractMapper {
	private static EntityToVoMapper INSTANCE = new EntityToVoMapper();
	private static final String[] IGNORE_PROPS = { "mechanisms",
			"dynamicProps", "extendProps" };

	public static EntityToVoMapper getInstance() {
		return INSTANCE;
	}

	/**
	 * Entity转VO，会根据数据字典的showType进行选择
	 * <li>showType=NONE：不拷贝</li>
	 * <li>showType=DETAIL_ONLY：在列表显示时不拷贝</li>
	 * <li>showType=null and lazy=true：在列表显示时不拷贝</li>
	 * 
	 * @param source
	 * @param target
	 * @param inList
	 * @param ignoreProperties
	 */
	public void entityToVo(IEntity source, Object target, boolean inList,
			String... ignoreProperties) {
		if (source == null || target == null) {
			return;
		}
		MetaEntity meta = MetaEntity
				.localEntity(EntityUtil.getEntityClassName(source));
		copyProperties(new MapperContext(meta, source, target, inList,
				ignoreProperties));
	}

	/** 拷贝所有属性 */
	private void copyProperties(MapperContext context) {
		if (context.meta == null) {
			return;
		}
		// 处理固定属性，目标属性一般会比较少
		for (PropertyDescriptor targetDesc : BeanUtils
				.getPropertyDescriptors(context.target.getClass())) {
			String propName = targetDesc.getName();
			// 检查排除路径
			String path = context.path == null ? propName
					: StringHelper.join(context.path, '.', propName);
			if (context.excludes.contains(path)) {
				continue;
			}
			context.propName = propName;
			try {
				context.targetDesc = targetDesc;
				context.metaProp = context.meta.getProperty(propName);
				// 检查忽略条件
				if (ignoreCheck(context)) {
					continue;
				}
				Object srcValue = context.getSourceValue(propName);
				if (srcValue == null) {
					continue;
				}
				// 拷贝属性
				if (context.langSupport && context.metaProp != null
						&& context.metaProp.isLangSupport()) {
					copyLangProperty(context, srcValue);
				} else {
					copyFixedProperty(context, srcValue);
				}
				logSuccess(context);
			} catch (Exception e) {
				handleException(context, e);
			}
		}
		// 处理动态属性
		if (context.target instanceof IData) {
			Map<String, Object> srcProps = ((IData) context.source)
					.getDynamicProps();
			if (srcProps != null) {
				for (Entry<String, Object> entry : srcProps.entrySet()) {
					String key = entry.getKey();
					MetaProperty metaProp = context.meta.getProperty(key);
					if (metaProp == null || !metaProp.isDynamic()) {
						continue;
					}
					context.propName = key;
					context.metaProp = metaProp;
					try {
						// 检查忽略条件
						if (ignoreCheck(context)) {
							continue;
						}
						// 拷贝属性
						copyDynamicProperty(context, entry.getValue());
						logSuccess(context);
					} catch (Exception e) {
						handleException(context, e);
					}
				}
			}
			((IData) context.target)
					.setMechanisms(((IData) context.source).getMechanisms());
		}
	}

	/** 是否忽略 */
	private boolean ignoreCheck(MapperContext context) {
		if (context.metaProp != null) {
			ShowType show = context.metaProp.getShowType();
			if (show == ShowType.NONE) {
				return true;
			}
			if (show == ShowType.DETAIL_ONLY && context.inList) {
				return true;
			}
		}
		if (context.source instanceof IData) {
			for (String prop : IGNORE_PROPS) {
				if (prop.equals(context.propName)) {
					return true;
				}
			}
		}
		return false;
	}

	/** 拷贝固定属性 */
	private void copyFixedProperty(MapperContext context, Object srcValue)
			throws Exception {
		// 源可读，目标可写
		Method write = context.targetDesc.getWriteMethod();
		if (!accessable(write)) {
			return;
		}
		// 转换目标值
		Object value;
		Type targetType = write.getGenericParameterTypes()[0];
		if (targetType instanceof ParameterizedType) {
			// 泛型，仅支持数组
			ParameterizedType pType = (ParameterizedType) targetType;
			Class<?> rawType = (Class<?>) pType.getRawType();
			if (Collection.class.isAssignableFrom(rawType)) {
				Type elemType = pType.getActualTypeArguments()[0];
				// 读取元素类型
				Class<?> elemClass = null;
				if (elemType instanceof Class) {
					elemClass = (Class<?>) elemType;
				} else if (elemType instanceof TypeVariable) {
					elemClass = ReflectUtil.getActualClass(
							context.target.getClass(),
							write.getDeclaringClass(),
							((TypeVariable<?>) elemType).getName());
				}
				if (elemClass == null) {
					throw new Exception("未识别的数组元素类型");
				}
				value = castListValue(context, (Collection<?>) srcValue,
						elemClass);
			} else {
				throw new Exception("泛型仅支持Collection");
			}
		} else {
			// 非泛型，读取目标类型
			Class<?> targetClass = null;
			if (targetType instanceof Class) {
				targetClass = (Class<?>) targetType;
			} else if (targetType instanceof TypeVariable) {
				targetClass = ReflectUtil.getActualClass(
						context.target.getClass(),
						write.getDeclaringClass(),
						((TypeVariable<?>) targetType).getName());
			}
			if (targetClass == null) {
				throw new Exception("未知的字段类型");
			}
			value = castSingleValue(context, srcValue, targetClass);
		}
		// 写值
		write.invoke(context.target, new Object[] { value });
	}

	/** 拷贝多语言字段 */
	private void copyLangProperty(MapperContext context, Object srcValue)
			throws Exception {
		Map<String, Object> source = ((IData) context.source)
				.getDynamicProps();
		if (source == null) {
			return;
		}
		if (context.target instanceof IData) {
			// 拷贝基本字段
			copyFixedProperty(context, srcValue);
			// 拷贝多语言的动态字段
			Map<String, Object> target = ((IData) context.target)
					.getDynamicProps();
			if (target == null) {
				target = new HashMap<String, Object>(16);
				((IData) context.target).setDynamicProps(target);
			}
			for (String lang : LangUtil.getSupportCountries()) {
				String propName = StringHelper.join(context.propName, lang);
				String value = TypeUtils.castToString(source.get(propName));
				target.put(propName, value);
			}
		} else {
			// 从动态属性中读取当前多语言字段
			String value = null;
			String propName = PropertyLangUtil
					.getPropertyNameByLanguage(context.propName);
			if (propName != null) {
				value = TypeUtils.castToString(source.get(propName));
			}
			// 当前多语言有，则用多言字段，没有则用默认值
			if (StringUtils.isNotBlank(value)) {
				copyFixedProperty(context, value);
			} else {
				copyFixedProperty(context, srcValue);
			}
		}
	}

	/** 列表数据类型转换 */
	private Object castListValue(MapperContext context, Collection<?> srcList,
			Class<?> elemClass)
			throws Exception {
		// 其他，直接创建一个列表
		List<Object> targetList = new ArrayList<>();
		for (Object srcValue : srcList) {
			targetList.add(castSingleValue(context, srcValue, elemClass));
		}
		return targetList;
	}

	/** 单值数据类型转换 */
	private Object castSingleValue(MapperContext context, Object srcValue,
			Class<?> targetClass) throws Exception {
		if (srcValue instanceof IEntity) {
			// 源值是Entity，拷贝一份
			Object targetValue = targetClass.newInstance();
			String entityClass = EntityUtil.getEntityClassName(srcValue);
			MetaEntity meta = MetaEntity.localEntity(entityClass);
			if (meta == null) {
				log.warn(entityClass + "的数据字典信息为空.");
			}
			MapperContext child = new MapperContext(context, meta, srcValue,
					targetValue);
			copyProperties(child);
			return targetValue;
		} else {
			// 其他，直接转
			return TypeUtils.cast(srcValue, targetClass, null);
		}
	}

	/** 拷贝动态属性 */
	private void copyDynamicProperty(MapperContext context, Object srcValue)
			throws Exception {
		Object value;
		if (context.metaProp.isCollection()) {
			List<Object> list = new ArrayList<>();
			for (Object val : (Collection<?>) srcValue) {
				list.add(castSingleValue(context.metaProp, val));
			}
			value = list;
		} else {
			value = castSingleValue(context.metaProp, srcValue);
		}
		Map<String, Object> map = ((IData) context.target).getDynamicProps();
		if (map == null) {
			map = new HashMap<>(16);
			((IData) context.target).setDynamicProps(map);
		}
		map.put(context.propName, value);
	}

	/** 单值数据类型转换，对象转换成Map */
	private Object castSingleValue(MetaProperty property, Object srcValue)
			throws ReflectiveOperationException {
		if (srcValue instanceof IEntity) {
			Map<String, Object> value = new HashMap<>(2);
			// fdId
			value.put("fdId", ((IEntity) srcValue).getFdId());
			MetaEntity entity = MetaEntity.localEntity(property.getType());
			// displayProperty
			String display = entity.getDisplayProperty();
			if (StringUtils.isBlank(display)) {
				return value;
			}
			PropertyDescriptor desc = BeanUtils
					.getPropertyDescriptor(srcValue.getClass(), display);
			if (desc != null) {
				Method read = desc.getReadMethod();
				if (read != null) {
					value.put(display, read.invoke(srcValue));
				}
			}
			return value;
		}
		return srcValue;
	}
}
