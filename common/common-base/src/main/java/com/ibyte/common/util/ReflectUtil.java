package com.ibyte.common.util;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 反射常用函数，这里涉及到泛型的方法，不支持以下几种情况：
 * 
 * 1、内部类中用到外部类的泛型参数<br>
 * 2、不支持泛型的通配符，如：List<? extends IEntity><br>
 * 3、不支持泛型数组，如：Map<String, String>[]
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 *
 */
public class ReflectUtil {
	// 支持多级泛型，样例：
	// 行1、class PersonDao extends BaseDao<Person>
	// 行2、class BaseDao<T extends IEntity> extends AbstractDao<T>
	// 行3、class AbstractDao<E extends IEntity>
	// 要在AbstractDao中获取到E的实际类型Person，需要先获取到行2的T变量，再从行1找T变量的赋值
	//
	// 几个基础的reflect API：
	// BaseDao.class.getTypeParameters() = [T]
	// BaseDao.class.getGenericSuperclass() = AbstractDao<T>
	// AbstractDao<T>.getActualTypeArguments() = [T]
	// AbstractDao<T>.getRawType() = AbstractDao
	/**
	 * 获取泛型变量的实际类型，支持多级泛型
	 * 
	 * @param impClazz
	 *            最终实现类
	 * @param baseClazz
	 *            基类
	 * @param varName
	 *            在基类中定义的泛型变量
	 * @return 当无法解释时返回null
	 */
	public static Class<?> getActualClass(Class<?> impClazz,
			Class<?> baseClazz, String varName) {
		Type type = getActualType(impClazz, baseClazz, varName);
		if (type != null) {
			if (type instanceof Class) {
				return (Class<?>) type;
			} else if (type instanceof ParameterizedType) {
				type = ((ParameterizedType) type).getRawType();
				if (type instanceof Class) {
					return (Class<?>) type;
				}
			}
		}
		return null;
	}

	/**
	 * 获取泛型变量的实际类型，支持多级泛型，但返回的可能是一个泛型变量
	 */
	public static Type getActualType(Class<?> curClazz, Class<?> baseClazz,
			String varName) {
		if (baseClazz.isInterface()) {
			// 获取接口中的泛型变量，到接口中找
			Type[] extInterfaces = curClazz.getGenericInterfaces();
			if (extInterfaces != null) {
				for (Type extInterface : extInterfaces) {
					Type result = getActualType(curClazz, extInterface,
							baseClazz, varName);
					if (result != null) {
						return result;
					}
				}
			}
		}
		// 顺着父类找
		Type extClazz = curClazz.getGenericSuperclass();
		if (extClazz != null) {
			return getActualType(curClazz, extClazz, baseClazz, varName);
		}
		return null;
	}

	/**
	 * 获取泛型变量的实际类型，支持多级泛型，但返回的可能是一个泛型变量<br>
	 * 样例：class PersonDao extends BaseDao<Person><br>
	 * curClazz = PersonDao, extType = BaseDao<Person>
	 */
	private static Type getActualType(Class<?> curClazz, Type extType,
			Class<?> baseClazz, String varName) {
		if (extType instanceof ParameterizedType) {
			// 接口是参数的接口
			ParameterizedType extParamType = (ParameterizedType) extType;
			Type rawType = extParamType.getRawType();
			if (rawType instanceof Class) {
				if (rawType == baseClazz) {
					return getTypeByVarName(extParamType, varName);
				}
				Type result = getActualType((Class<?>) rawType, baseClazz,
						varName);
				if (result != null) {
					if (result instanceof Class) {
						// 直接给出了类型
						return result;
					} else if (result instanceof TypeVariable) {
						// 给出的是一个变量
						return getTypeByVarName(extParamType,
								result.getTypeName());
					} else {
						return null;
					}
				}
			}
		} else if (extType instanceof Class) {
			return getActualType((Class<?>) extType, baseClazz, varName);
		}
		return null;
	}

	/**
	 * 根据变量名获取类型
	 */
	private static Type getTypeByVarName(ParameterizedType paramType,
			String varName) {
		Class<?> defClazz = (Class<?>) paramType.getRawType();
		TypeVariable<?>[] params = defClazz.getTypeParameters();
		for (int i = 0; i < params.length; i++) {
			if (varName.equals(params[i].getName())) {
				return paramType.getActualTypeArguments()[i];
			}
		}
		return null;
	}

	/**
	 * 将type中的变量参数实例化
	 * 
	 * @param impClazz
	 *            最终实现类
	 * @param typeClazz
	 *            type所在的类
	 * @param type
	 * @return
	 */
	public static Type parameterize(Class<?> impClazz, Class<?> typeClazz,
			Type type) {
		Map<String, Class<?>> cache = new HashMap<>(4);
		return parameterize(type, var -> {
			String varName = var.getName();
			Class<?> actClass = cache.get(varName);
			if (actClass == null) {
				actClass = getActualClass(impClazz, typeClazz, varName);
				cache.put(varName, actClass);
			}
			return actClass;
		});
	}

	/**
	 * 将type中的变量参数实例化
	 * 
	 * @param type
	 * @param resolver
	 *            将变量转换成实际的类的方法
	 * @return
	 */
	public static Type parameterize(Type type,
			Function<TypeVariable<?>, Type> resolver) {
		// 类:String
		if (type instanceof Class) {
			return type;
		}
		// 变量:E
		if (type instanceof TypeVariable) {
			return resolver.apply((TypeVariable<?>) type);
		}
		// 泛型:List<E>
		if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			Type[] oriTypes = paramType.getActualTypeArguments();
			// 若参数中出现了泛型或变量则使用递归
			boolean recursion = false;
			for (Type oriType : oriTypes) {
				if (!(oriType instanceof Class)) {
					recursion = true;
					break;
				}
			}
			if (recursion) {
				Type[] actTypes = new Type[oriTypes.length];
				for (int i = 0; i < oriTypes.length; i++) {
					actTypes[i] = parameterize(oriTypes[i], resolver);
				}
				return TypeUtils.parameterize((Class<?>) paramType.getRawType(),
						actTypes);
			} else {
				return type;
			}
		}
		throw new RuntimeException("不支持的参数类型：" + type.getClass().getName());
	}

	/**
	 * name -> class，未找到返回null
	 */
	public static Class<?> classForName(String name) {
		try {
			return getClassLoader().loadClass(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * 取ClassLoader
	 */
	public static ClassLoader getClassLoader() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = ReflectUtil.class.getClassLoader();
		}
		return loader;
	}

	/**
	 * newInstance不处理异常
	 */
	public static <T> T newInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取bean的字段值
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProperty(Object bean, String prop)
			throws ReflectiveOperationException {
		if (bean == null) {
			return null;
		}
		if (bean instanceof Map<?, ?>) {
			return (T) ((Map<?, ?>) bean).get(prop);
		}
		PropertyDescriptor desc = BeanUtils
				.getPropertyDescriptor(bean.getClass(), prop);
		if (desc == null) {
			throw new NoSuchFieldException();
		}
		Method method = desc.getReadMethod();
		if (method == null) {
			throw new NoSuchMethodException();
		}
		return (T) method.invoke(bean);
	}

	/**
	 * 方法是否可访问
	 */
	public static boolean accessable(Method method) {
		return method != null
				&& Modifier.isPublic(method.getDeclaringClass().getModifiers());
	}
}
