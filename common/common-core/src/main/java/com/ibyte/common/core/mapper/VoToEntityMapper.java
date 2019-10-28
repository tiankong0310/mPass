package com.ibyte.common.core.mapper;

import com.alibaba.fastjson.util.TypeUtils;
import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.core.util.EntityUtil;
import com.ibyte.framework.meta.MetaEntity;
import com.ibyte.common.core.data.IData;
import com.ibyte.common.core.dto.IViewObject;
import com.ibyte.common.core.dto.IdProperty;
import com.ibyte.common.core.service.IService;
import com.ibyte.common.exception.VersionConflictException;
import com.ibyte.common.util.LangUtil;
import com.ibyte.common.util.ReflectUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.meta.MetaProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

import static com.ibyte.common.util.ReflectUtil.accessable;

/**
 * VO转Entity的转换工具
 *
 * @author ibyte
 * @Date: 2019-10-27
 */
@Slf4j
public class VoToEntityMapper extends AbstractMapper {
	private static VoToEntityMapper INSTANCE = new VoToEntityMapper();

	public static VoToEntityMapper getInstance() {
		return INSTANCE;
	}

	/**
	 * VO转Entity，会根据数据字典拷贝的属性进行选择
	 * <li>readOnly=true的字段不拷贝</li>
	 * <li>值为null，没加入nullValueProps的字段不拷贝</li>
	 * <li>关联字段，级联则深度拷贝，不级联则只根据ID找到对象赋值</li>
	 * 
	 * @param source
	 * @param target
	 * @param ignoreProperties
	 */
	public void voToEntity(Object source, IEntity target,
			String... ignoreProperties) {
		if (source == null || target == null) {
			return;
		}
		MetaEntity meta = MetaEntity
				.localEntity(EntityUtil.getEntityClassName(target));
		MapperContext context = new MapperContext(meta, source, target, false,
				ignoreProperties);
		copyProperties(context);
	}

	/** 拷贝所有属性 */
	private void copyProperties(MapperContext context) {
		if (context.meta == null) {
			return;
		}
		List<String> nullValueProps = (context.source instanceof IViewObject)
				? ((IViewObject) context.source).getNullValueProps() : null;
		for (MetaProperty metaProp : context.meta.getProperties().values()) {
			// 只读属性不转换
			if (metaProp.isReadOnly()) {
				continue;
			}
			// 动态属性源必须是VO
			if (metaProp.isDynamic()
					&& !(context.source instanceof IViewObject)) {
				continue;
			}
			String propName = metaProp.getName();
			// 检查排除路径
			String path = context.path == null ? propName
					: StringHelper.join(context.path, '.', propName);
			if (context.excludes.contains(path)) {
				continue;
			}
			context.propName = propName;
			context.metaProp = metaProp;
			try {
				// 读取源值
				Object srcValue;
				if (nullValueProps != null
						&& nullValueProps.contains(propName)) {
					srcValue = null;
				} else {
					if (metaProp.isDynamic()) {
						Map<String, Object> map = ((IViewObject) context.source)
								.getDynamicProps();
						srcValue = map == null ? null : map.get(propName);
					} else {
						srcValue = context.getSourceValue(propName);
					}
					// 源值空，忽略
					if (srcValue == null) {
						continue;
					}
				}
				// 写值
				if (metaProp.isDynamic()) {
					copyDynamicProperty(context, srcValue);
				} else {
					copyFixedProperty(context, srcValue);
				}
				if (context.langSupport && metaProp.isLangSupport()) {
					copyLangDynamicProperty(context, srcValue);
				}
				logSuccess(context);
			} catch (VersionConflictException e) {
				throw e;
			} catch (Exception e) {
				handleException(context, e);
			}
		}
		if (context.source instanceof IData) {
			((IData) context.target)
					.setMechanisms(((IData) context.source).getMechanisms());
		}
	}

	/** 拷贝单个属性 */
	@SuppressWarnings("unchecked")
	private void copyFixedProperty(MapperContext context, Object srcValue)
			throws Exception {
		// 目标可读/写
		context.targetDesc = BeanUtils.getPropertyDescriptor(
				context.target.getClass(), context.propName);
		if (context.targetDesc == null) {
			return;
		}
		Method read = context.targetDesc.getReadMethod();
		if (!accessable(read)) {
			return;
		}
		Method write = context.targetDesc.getWriteMethod();
		if (!accessable(write)) {
			return;
		}
		// 读取目标值
		Object targetValue = read.invoke(context.target);
		Class<?> targetClass = getTargetClass(context);
		if (context.metaProp.isCollection()) {
			// 数组
			if (targetValue == null) {
				targetValue = new ArrayList<>();
				write.invoke(context.target, new Object[] { targetValue });
			}
			copyListValue(context, (Collection<?>) srcValue,
					(Collection<Object>) targetValue, targetClass);
		} else {
			// 单值
			Object value = castSingleValue(context, srcValue, targetValue,
					targetClass);
			if (value != targetValue) {
				checkVersion(value, targetValue, context);
				write.invoke(context.target, value);
			}
		}
	}

	/** 拷贝多语言的动态字段 */
	private void copyLangDynamicProperty(MapperContext context, Object srcValue)
			throws Exception {
		if (context.source instanceof IData
				&& context.target instanceof IData) {
			Map<String, Object> source = ((IData) context.source)
					.getDynamicProps();
			Map<String, Object> target = ((IData) context.target)
					.getDynamicProps();
			if (source == null) {
				return;
			}
			if (target == null) {
				target = new HashMap<String, Object>(16);
				((IData) context.target).setDynamicProps(target);
			}
			for (String lang : LangUtil.getSupportCountries()) {
				String propName = StringHelper.join(context.propName, lang);
				String value = srcValue == null ? null
						: TypeUtils.castToString(source.get(propName));
				target.put(propName, value);
			}
		}
	}

	/** 列表数据类型转换 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void copyListValue(MapperContext context, Collection<?> srcList,
			Collection<Object> targetList, Class<?> elemClass)
			throws Exception {
		if (srcList == null) {
			targetList.clear();
			return;
		}
		if (IEntity.class.isAssignableFrom(elemClass)) {
			// 目标是Entity，目标值拷贝一份后清空
			Collection<IEntity> listCopy = new ArrayList(targetList);
			targetList.clear();

			boolean cascade = MetaConstant
					.isCascade(context.metaProp.getCascade());
			MetaEntity meta = MetaEntity.localEntity(elemClass.getName());
			for (Object srcValue : srcList) {
				// 查找已经存在的目标值
				String id = getFdIdValue(srcValue);
				IEntity targetValue = null;
				if (id != null) {
					for (IEntity value : listCopy) {
						if (id.equals(value.getFdId())) {
							targetValue = value;
							break;
						}
					}
				}
				if (cascade) {
					// 级联，目标不存在则创建一个，并且将复制值
					if (targetValue == null) {
						targetValue = (IEntity) newCascadeChild(elemClass,
								context);
					}
					MapperContext child = new MapperContext(context, meta,
							srcValue, targetValue);
					copyProperties(child);
				} else {
					if (id == null) {
						throw new Exception("无法从源值中获取fdId");
					}
					// 非级联，通过service查找目标
					if (targetValue == null) {
						IService<?, ?> service = EntityUtil
								.getEntityService(meta);
						if (service == null) {
							throw new Exception(StringHelper.join("无法获取",
									elemClass.getName(), "对应的Service"));
						}
						targetValue = service.getOne(id);
					}
				}
				targetList.add(targetValue);
			}
		} else {
			targetList.clear();
			// 其他，直接创建一个列表
			for (Object srcValue : srcList) {
				targetList.add(TypeUtils.cast(srcValue, elemClass, null));
			}
		}
	}

	/** 创建一对多级联的子项 */
	private Object newCascadeChild(Class<?> clazz, MapperContext context)
			throws Exception {
		Object result = ReflectUtil.newInstance(clazz);
		if (StringUtils.isNotBlank(context.metaProp.getMappedBy())) {
			// 设置反向关系
			String mappedBy = context.metaProp.getMappedBy();
			PropertyDescriptor desc = BeanUtils.getPropertyDescriptor(clazz,
					mappedBy);
			if (desc == null) {
				return result;
			}
			Method method = desc.getWriteMethod();
			if (method == null) {
				return result;
			}
			method.invoke(result, context.target);
		}
		return result;
	}

	/** 单值数据类型转换 */
	private Object castSingleValue(MapperContext context, Object srcValue,
			Object targetValue, Class<?> targetClass) throws Exception {
		if (srcValue == null) {
			return null;
		}
		if (IEntity.class.isAssignableFrom(targetClass)) {
			// 目标是Entity，根据级联风格处理
			if (MetaConstant.isCascade(context.metaProp.getCascade())) {
				// 如果级联实体已存在，则直接返回关联实体
				String id = null;
				try {
					id = getFdIdValue(srcValue);
				} catch (Exception e) {
					log.info("级联查找id为空");
				}
				if(StringUtils.isNotBlank(id)) {
					IService<?, ?> service = EntityUtil
							.getEntityService(targetClass.getName());
					Optional<?> optional = service.findById(id);
					if(optional.isPresent()) {
						return optional.get();
					}
				}
				// 级联
				if (targetValue == null) {
					targetValue = targetClass.newInstance();
				}
				MetaEntity meta = MetaEntity
						.localEntity(EntityUtil.getEntityClassName(targetValue));
				MapperContext child = new MapperContext(context, meta, srcValue,
						targetValue);
				copyProperties(child);
				return targetValue;
			} else {
				// 非级联，通过service查找目标
				IService<?, ?> service = EntityUtil
						.getEntityService(targetClass.getName());
				if (service == null) {
					throw new Exception(StringHelper.join("无法获取",
							targetClass.getName(), "对应的Service"));
				}
				String id = getFdIdValue(srcValue);
				if (id == null) {
					throw new Exception("无法从源值中获取fdId");
				}
				return service.getOne(id);
			}
		}
		// 其他，直接转
		return TypeUtils.cast(srcValue, targetClass, null);
	}

	/** 取fdId字段值 */
	private String getFdIdValue(Object bean) throws Exception {
		if (bean instanceof IEntity) {
			return ((IEntity) bean).getFdId();
		}
		if (bean instanceof IViewObject) {
			return ((IViewObject) bean).getFdId();
		}
		if (bean instanceof IdProperty) {
			return ((IdProperty) bean).getFdId();
		}
		if (bean instanceof Map) {
			return (String) ((Map<?, ?>) bean).get("fdId");
		}
		if (bean instanceof String) {
			return (String) bean;
		}
		return ReflectUtil.getProperty(bean, "fdId");
	}

	/** 拷贝动态属性 */
	@SuppressWarnings("unchecked")
	private void copyDynamicProperty(MapperContext context, Object srcValue)
			throws Exception {
		// 读取目标值
		Map<String, Object> props = ((IEntity) context.target)
				.getDynamicProps();
		if (props == null) {
			props = new HashMap<>(16);
			((IEntity) context.target).setDynamicProps(props);
		}
		Object targetValue = props.get(context.propName);
		Class<?> targetClass = getTargetClass(context);
		if (context.metaProp.isCollection()) {
			// 数组
			if (targetValue == null) {
				targetValue = new ArrayList<>();
				props.put(context.propName, targetValue);
			}
			copyListValue(context, (Collection<?>) srcValue,
					(Collection<Object>) targetValue, targetClass);
		} else {
			// 单值
			Object value = castSingleValue(context, srcValue, targetValue,
					targetClass);
			if (value != targetValue) {
				checkVersion(value, targetValue, context);
				props.put(context.propName, value);
			}
		}
	}

	private Class<?> getTargetClass(MapperContext context) {
		// 先取枚举类型
		String type = context.metaProp.getEnumClass();
		if (StringUtils.isEmpty(type)) {
			// 如果没有枚举类型，才取最终的类型
			type = context.metaProp.getType();
		}
		return EntityUtil.getPropertyType(type);
	}

	/** 检查版本信息 */
	private void checkVersion(Object srcValue, Object tarValue,
			MapperContext context) {
		if (srcValue != null && Objects.equals(context.propName,
				context.meta.getVersionProperty())
				&& !srcValue.equals(tarValue)) {
			throw new VersionConflictException();
		}
	}
}
