package com.ibyte.common.core.query;

import com.alibaba.fastjson.util.TypeUtils;
import com.ibyte.common.core.constant.QueryConstant.Direction;
import com.ibyte.common.core.constant.QueryConstant.Operator;
import com.ibyte.common.core.dto.IViewObject;
import com.ibyte.common.core.dto.QueryRequest;
import com.ibyte.common.core.dto.QueryResult;
import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.core.query.spi.QueryFilter;
import com.ibyte.common.core.query.support.*;
import com.ibyte.common.core.query.support.QueryFilterChain.FilterInfo;
import com.ibyte.common.core.util.EntityUtil;
import com.ibyte.common.core.util.PropertyLangUtil;
import com.ibyte.common.exception.ParamsNotValidException;
import com.ibyte.common.util.*;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.meta.MetaConstant.ShowType;
import com.ibyte.framework.meta.MetaEntity;
import com.ibyte.framework.meta.MetaProperty;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.springframework.beans.BeanUtils;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;

/**
 * 通用查询模板
 *
 * @author li.Shangzhi
 *
 * @param <E>
 * @param <V>
 */
public class PageQueryTemplate<E extends IEntity, V> {
	private EntityManager entityManager;
	private Class<E> entityClass;
	private Class<V> viewClass;
	private boolean isViewObject;
	private List<FilterInfo> filters = new ArrayList<>();
	private boolean cacheable;
	private boolean filterTenant = true;
	private boolean langSupport = LangUtil.isSuportEnabled();

	public PageQueryTemplate(EntityManager entityManager, Class<E> entityClass,
			Class<V> viewClass) {
		super();
		this.entityManager = entityManager;
		this.entityClass = entityClass;
		this.viewClass = viewClass;
		this.isViewObject = IViewObject.class.isAssignableFrom(viewClass);
	}

	/**
	 * 若viewClass未实现IViewObject的接口，但又想按自动接收参数，请设置为true
	 */
	public PageQueryTemplate<E, V> setIsViewObject(boolean isViewObject) {
		this.isViewObject = isViewObject;
		return this;
	}

	/**
	 * 自定义过滤器
	 */
	public PageQueryTemplate<E, V> addFilter(QueryFilter filter, Object param) {
		filters.add(new FilterInfo(filter, param));
		return this;
	}

	/**
	 * 自定义过滤器
	 */
	public PageQueryTemplate<E, V> addFilter(
			BiFunction<QueryContext, QueryFilterChain, Predicate> filter) {
		filters.add(new FilterInfo(filter));
		return this;
	}

	/**
	 * 是否使用二级缓存
	 */
	public PageQueryTemplate<E, V> setCacheable(boolean cacheable) {
		this.cacheable = cacheable;
		return this;
	}

	/**
	 * 默认为true：自动过滤租户
	 */
	public PageQueryTemplate<E, V> setFilterTenant(boolean filterTenant) {
		this.filterTenant = filterTenant;
		return this;
	}

	/**
	 * 执行查询
	 */
	public QueryResult<V> findAll(QueryRequest request) {
		boolean filterTenant = this.filterTenant
				&& TenantUtil.getTenantId() != TenantUtil.SYSTEM_TENANT;
		request.format();
		// 获取总数
		long count = 0;
		List<V> content = null;
		if (request.isCount()) {
			count = queryCount(request, filterTenant);
			if (count == 0) {
				return QueryResult.empty();
			}
		}
		// 获取详情
		if (request.isContent()) {
			if (isViewObject) {
				content = queryContentAndToVO(request, filterTenant);
			} else {
				content = queryContent(request, filterTenant);
			}
		}
		// 返回数据
		QueryResult<V> queryResult = new QueryResult<>(content, request.getOffset(),
				request.getPageSize(), count);
		//TODO 日志处理
		return queryResult;
	}

	private <R> TypedQuery<R> createQuery(CriteriaQuery<R> query) {
		TypedQuery<R> typedQuery = entityManager.createQuery(query);
		if (cacheable && typedQuery instanceof Query) {
			((Query<R>) typedQuery).setCacheable(cacheable);
		}
		return typedQuery;
	}

	/**
	 * 查询总数
	 */
	private long queryCount(QueryRequest request, boolean filterTenant) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		QueryContextImpl<E, Long> context = new QueryContextImpl<>(request,
				entityClass, builder, query, filterTenant);
		// where
		applyRequestToWhere(context);
		// select
		if (query.isDistinct()) {
			query.select(builder.countDistinct(context.getRoot()));
		} else {
			query.select(builder.count(context.getRoot()));
		}
		// query
		return createQuery(query).getSingleResult();
	}

	/**
	 * 查询内容
	 */
	private List<V> queryContent(QueryRequest request, boolean filterTenant) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<V> query = builder.createQuery(viewClass);
		QueryContextImpl<E, V> context = new QueryContextImpl<>(request,
				entityClass, builder, query, filterTenant);
		// where
		applyRequestToWhere(context);
		// order by
		applyRequestToOrderby(context);
		// select
		if (viewClass != entityClass) {
			List<String> columns = request.getColumns();
			if (ArrayUtil.isEmpty(columns)) {
				throw new ParamsNotValidException("QueryRequest的columns参数不能为空");
			}
			List<Selection<?>> list = new ArrayList<>(columns.size());
			for (String column : columns) {
				list.add(context.leftJoinPath(column));
			}
			query.multiselect(list);
		}
		// query
		return executeQuery(request, query);
	}

	/**
	 * 查询内容并转换成ViewClass
	 */
	private List<V> queryContentAndToVO(QueryRequest request,
			boolean filterTenant) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
		QueryContextImpl<E, Tuple> context = new QueryContextImpl<>(request,
				entityClass, builder, query, filterTenant);
		// where
		applyRequestToWhere(context);
		// order by
		applyRequestToOrderby(context);
		// select
		ViewInfo root = new ViewInfo();
		Map<String, List<MetaProperty>> collectionProps = new HashMap<>(16);
		applyRequestToSelect4VO(context, root, collectionProps);

		// 查询并转换VO对象
		List<Tuple> tuples = executeQuery(request, query);
		List<V> result = new ArrayList<>(tuples.size());
		for (Tuple tuple : tuples) {
			result.add(toViewObject(tuple, root, viewClass, null));
		}
		// 若需要查询列表属性，则再次查询
		if (!collectionProps.isEmpty() && !result.isEmpty()) {
			List<String> ids = new ArrayList<>(tuples.size());
			for (Tuple tuple : tuples) {
				ids.add((String) tuple.get(0));
			}
			for (Entry<String, List<MetaProperty>> entry : collectionProps
					.entrySet()) {
				queryCollectionPropAndToVO(entry.getKey(), entry.getValue(),
						result, ids, filterTenant);
			}
		}
		return result;
	}

	/**
	 * 处理where条件
	 */
	private <T> void applyRequestToWhere(QueryContextImpl<E, T> context) {
		List<FilterInfo> filters = new ArrayList<>();
		// 多租户过滤优先级最高
		if (filterTenant) {
			filters.add(new FilterInfo(TenantQueryFilter.getInstance(), null));
		}
		// 优先request定义的通用过滤器
		Map<String, Object> requestFilters = context.getRequest().getFilters();
		if (requestFilters != null) {
			for (Entry<String, Object> entry : requestFilters.entrySet()) {
				QueryFilter filter = QueryFilterManager
						.getFilter(entry.getKey());
				if (filter == null) {
					throw new ParamsNotValidException(
							"未知的过滤器：" + entry.getKey());
				}
				filters.add(new FilterInfo(filter, entry.getValue()));
			}
		}
		// 然后是自定义的过滤器
		filters.addAll(this.filters);
		// 最后是request的condition解析器
		filters.add(new FilterInfo(QueryRequestConditionParser.INSTANCE,
				context.getRequest().getConditions()));
		// 执行过滤链
		QueryFilterChain chain = new QueryFilterChain(filters);
		Predicate predicate = chain.toPredicate(context);
		if (predicate != null) {
			context.getQuery().where(predicate);
		}
	}

	/**
	 * 处理order by
	 */
	private <T> void applyRequestToOrderby(QueryContextImpl<E, T> context) {
		Map<String, String> sorts = context.getRequest().getSorts();
		if (sorts != null) {
			List<Order> orders = new ArrayList<>(sorts.size());
			for (Entry<String, String> entry : sorts.entrySet()) {
				Expression<?> expression = context.leftJoinPath(entry.getKey());
				Class<?> type = expression.getJavaType();
				if (type != null && IEntity.class.isAssignableFrom(type)) {
					// 对象类型，用displayProperty排序
					MetaEntity entity = MetaEntity.localEntity(type.getName());
					if (entity != null && StringUtils
							.isNotBlank(entity.getDisplayProperty())) {
						String displayName = StringHelper.join(entry.getKey(),
								'.', entity.getDisplayProperty());
						expression = context.leftJoinPath(displayName);
					}
				}
				if (Direction.DESC.name().equalsIgnoreCase(entry.getValue())) {
					orders.add(context.getBuilder().desc(expression));
				} else {
					orders.add(context.getBuilder().asc(expression));
				}
			}
			context.getQuery().orderBy(orders);
		}
	}

	/**
	 * 处理VO查询中的select
	 */
	private void applyRequestToSelect4VO(QueryContextImpl<E, Tuple> context,
			ViewInfo root, Map<String, List<MetaProperty>> collectionProps) {
		List<String> columns = context.getRequest().getColumns();
		List<Selection<?>> selects = new ArrayList<>();
		MetaEntity meta = MetaEntity.localEntity(entityClass.getName());

		// fdId必须加在第一列
		selects.add(context.getRoot().get("fdId"));

		if (ArrayUtil.isEmpty(columns)) {
			// 未指定列，查询所有列表可显示的列
			for (MetaProperty prop : meta.getProperties().values()) {
				if (prop.getShowType() == ShowType.ALWAYS
						&& !prop.isCollection()) {
					List<MetaProperty> props = new ArrayList<>();
					props.add(prop);
					appendSelectColumn4VO(context, prop.getName(), props,
							selects, root);
				}
			}
		} else {
			// 指定列，查询所有列表可显示的列，支持a.b.c的格式，但不能是双重数组
			outloop: for (String column : columns) {
				List<MetaProperty> props = EntityUtil.getMetaProperty(meta,
						column);
				boolean isCollection = false;
				for (MetaProperty prop : props) {
					if (prop.isCollection()) {
						if (isCollection) {
							throw new ParamsNotValidException("查询列不支持双重数组");
						}
						isCollection = true;
						if (prop.getShowType() == ShowType.NONE) {
							continue outloop;
						}
					}
				}
				if (isCollection) {
					collectionProps.put(column, props);
				} else {
					appendSelectColumn4VO(context, column, props, selects,
							root);
				}
			}
		}
		context.getQuery().multiselect(selects);
	}

	/**
	 * 添加选择列，支持a.b.c，仅用于转vo的情况
	 */
	private void appendSelectColumn4VO(QueryContextImpl<E, Tuple> context,
			String propName, List<MetaProperty> props,
			List<Selection<?>> selects, ViewInfo root) {
		Class<?> clazz = viewClass;
		ViewInfo current = root;
		int n = props.size() - 1;
		// 层级遍历
		for (int i = 0; i < props.size(); i++) {
			MetaProperty prop = props.get(i);
			PropertyDescriptor desc = BeanUtils.getPropertyDescriptor(clazz,
					prop.getName());
			if (desc == null || desc.getWriteMethod() == null
					|| desc.getReadMethod() == null) {
				throw new ParamsNotValidException(
						StringHelper.join(propName, "对应的VO的属性不支持读/写"));
			}
			ViewInfo child = current.getOrAddChild(desc);
			current = child;
			if (prop.isCollection()) {
				// 列表对象
				child.arrayElementType = getListActualType(clazz, desc);
				if (child.arrayElementType == null) {
					throw new ParamsNotValidException(
							StringHelper.join(propName, "无法获取列表的实际类型"));
				}
				clazz = child.arrayElementType;
			} else {
				// 单值对象
				clazz = desc.getPropertyType();
			}
			if (i == n) {
				// 最后一级
				if (MetaConstant.isAssociation(prop.getType())) {
					// c是子对象，遍历对象下的属性
					MetaEntity meta = MetaEntity.localEntity(prop.getType());
					if (meta == null) {
						return;
					}
					PropertyDescriptor[] childDescs = BeanUtils
							.getPropertyDescriptors(clazz);
					for (PropertyDescriptor childDesc : childDescs) {
						if (childDesc.getWriteMethod() == null) {
							continue;
						}
						// 属性必须有在数据库中，并且不能是外键对象，显示类型允许在列表展现
						String name = childDesc.getName();
						MetaProperty childProp = meta.getProperty(name);
						if (childProp == null
								|| childProp.getShowType() != ShowType.ALWAYS
								|| MetaConstant
										.isAssociation(childProp.getType())) {
							continue;
						}
						// 追加子对象属性
						child = current.getOrAddChild(childDesc);
						appendSelectAndLangColumn(context, childProp,
								StringHelper.join(propName, '.', name), selects,
								child);
					}
				} else {
					// c是普通属性，直接追加
					if ("fdId".equals(propName)) {
						current.index = 0;
					} else {
						appendSelectAndLangColumn(context, prop, propName,
								selects, current);
					}
				}
			}
		}
	}

	/** 添加选择字段，包括多语言 */
	private void appendSelectAndLangColumn(QueryContextImpl<E, Tuple> context,
			MetaProperty prop, String propName, List<Selection<?>> selects,
			ViewInfo current) {
		current.index = selects.size();
		Path<?> path = context.leftJoinPath(propName);
		selects.add(path);
		if (langSupport && prop.isLangSupport()) {
			String langName = PropertyLangUtil
					.getPropertyNameByLanguage(propName);
			if (langName != null) {
				if (langName.contains(".")) {
					langName = langName.substring(langName.lastIndexOf(".") + 1);
				}
				current.langIndex = selects.size();
				selects.add(path.getParentPath().get(langName));
			}
		}
	}

	/**
	 * 转VO对象
	 */
	private <T> T toViewObject(Tuple tuple, ViewInfo viewInfo, Class<T> clazz,
			T vo) {
		for (Entry<String, ViewInfo> entry : viewInfo.children.entrySet()) {
			ViewInfo child = entry.getValue();
			Object value = null;
			if (child.arrayElementType != null && child.children != null) {
				// 数组，先从VO中获取列表，若列表为空则创建，然后往列表中追加子
				List<Object> list = null;
				if (vo == null) {
					vo = ReflectUtil.newInstance(clazz);
				} else {
					list = readProperty(vo, child.desc);
				}
				if (list == null) {
					list = new ArrayList<>();
					writeProperty(vo, child.desc, list);
				}
				value = toViewObject(tuple, child, child.arrayElementType,
						null);
				if(value != null) {
					list.add(value);
				}
			} else {
				if (child.children != null) {
					// 子对象
					value = toViewObject(tuple, child,
							child.desc.getPropertyType(), null);
				} else if (child.index > -1) {
					// 普通字段
					value = tuple.get(child.index);
					value = TypeUtils.cast(value, child.desc.getPropertyType(),
							null);
					if (child.langIndex > -1) {
						Object langValue = tuple.get(child.langIndex);
						langValue = TypeUtils.cast(langValue,
								child.desc.getPropertyType(), null);
						if (StringUtils.isNotBlank((String) langValue)) {
							value = langValue;
						}
					}
				}
				if (value != null) {
					if (vo == null) {
						vo = ReflectUtil.newInstance(clazz);
					}
					writeProperty(vo, child.desc, value);
				}
			}
		}
		return vo;
	}

	/**
	 * 查询列表属性，并追加到结果中
	 */
	private void queryCollectionPropAndToVO(String column,
			List<MetaProperty> props, List<V> result, List<String> ids,
			boolean filterTenant) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
		QueryContextImpl<E, Tuple> context = new QueryContextImpl<>(null,
				entityClass, builder, query, filterTenant);
		query.where(context.toPredicate("fdId", Operator.eq, ids));
		// select
		ViewInfo root = new ViewInfo();
		List<Selection<?>> selects = new ArrayList<>();
		selects.add(context.getRoot().get("fdId"));
		appendSelectColumn4VO(context, column, props, selects, root);
		query.multiselect(selects);
		// 查询并转换VO对象
		List<Tuple> tuples = createQuery(query).getResultList();
		for (Tuple tuple : tuples) {
			String id = (String) tuple.get(0);
			for (int i = 0; i < ids.size(); i++) {
				if (id.equals(ids.get(i))) {
					toViewObject(tuple, root, viewClass, result.get(i));
					break;
				}
			}
		}
	}

	/**
	 * 执行查询
	 */
	private <T> List<T> executeQuery(QueryRequest request,
			CriteriaQuery<T> query) {
		TypedQuery<T> typedQuery = createQuery(query);
		typedQuery.setFirstResult(request.getOffset());
		typedQuery.setMaxResults(request.getPageSize());
		return typedQuery.getResultList();
	}

	/**
	 * 读取列表元素类型
	 */
	private Class<?> getListActualType(Class<?> clazz,
			PropertyDescriptor desc) {
		Method read = desc.getReadMethod();
		Type type = read.getGenericReturnType();
		if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			Type[] types = paramType.getActualTypeArguments();
			if (Collection.class
					.isAssignableFrom((Class<?>) paramType.getRawType())
					&& types != null && types.length > 0) {
				Type elemType = types[0];
				if (elemType instanceof Class) {
					return (Class<?>) elemType;
				} else if (elemType instanceof TypeVariable) {
					return ReflectUtil.getActualClass(clazz,
							read.getDeclaringClass(),
							((TypeVariable<?>) elemType).getName());
				}
			}
		}
		return null;
	}

	/**
	 * 读取bean中的值
	 */
	@SuppressWarnings("unchecked")
	private <T> T readProperty(Object bean, PropertyDescriptor desc) {
		try {
			return (T) desc.getReadMethod().invoke(bean);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 往bean中写值
	 */
	private void writeProperty(Object bean, PropertyDescriptor desc,
			Object value) {
		try {
			desc.getWriteMethod().invoke(bean, new Object[] { value });
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * VO对象信息
	 *
	 * @author Li.Shangzhi
	 */
	static class ViewInfo {
		/** 在select语句的第几个返回值 */
		int index = -1;
		/** 在select语句的第几个返回值(多语言列) */
		int langIndex = -1;
		/** VO对象对应的属性描述 */
		PropertyDescriptor desc;
		/** 数组元素类型 */
		Class<?> arrayElementType;
		/** 若属性是一个自对象，则children就是子对象属性的信息 */
		Map<String, ViewInfo> children;

		ViewInfo getOrAddChild(PropertyDescriptor desc) {
			String name = desc.getName();
			ViewInfo child = null;
			if (children == null) {
				children = new HashMap<>(16);
			} else {
				child = children.get(name);
			}
			if (child == null) {
				child = new ViewInfo();
				child.desc = desc;
				children.put(name, child);
			}
			return child;
		}
	}
}
