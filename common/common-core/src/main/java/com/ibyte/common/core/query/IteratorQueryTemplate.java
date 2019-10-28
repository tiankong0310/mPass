package com.ibyte.common.core.query;

import com.ibyte.common.core.constant.QueryConstant.Direction;
import com.ibyte.common.core.dto.QueryRequest;
import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.core.query.spi.QueryFilter;
import com.ibyte.common.core.query.support.*;
import com.ibyte.common.core.query.support.QueryFilterChain.FilterInfo;
import com.ibyte.common.exception.ParamsNotValidException;
import com.ibyte.common.util.StringHelper;
import com.ibyte.common.util.TenantUtil;
import com.ibyte.framework.meta.MetaEntity;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

/**
 * 采用迭代器的方式进行查询
 *
 * @author li.Shangzhi
 * @param <E>
 */
public class IteratorQueryTemplate<E extends IEntity> {
	private static final int DEFAULT_BATCH = 100;

	private EntityManager entityManager;
	private Class<E> entityClass;
	private List<FilterInfo> filters = new ArrayList<>();
	private int fetchSize = DEFAULT_BATCH;
	private boolean autoFlush = true;
	private boolean filterTenant = true;

	public IteratorQueryTemplate(EntityManager entityManager,
			Class<E> entityClass) {
		super();
		this.entityManager = entityManager;
		this.entityClass = entityClass;
	}

	/**
	 * 默认值100，即每次从加载100个Entity<br>
	 * 当设置为1时采用延迟加载的方式，对于delete等不需要entity其他数据的时候能很好地提升性能
	 */
	public IteratorQueryTemplate<E> setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
		return this;
	}

	/**
	 * 默认为true：每处理1批次的数据后，执行一次flush
	 */
	public IteratorQueryTemplate<E> setAutoFlush(boolean autoFlush) {
		this.autoFlush = autoFlush;
		return this;
	}

	/**
	 * 自定义过滤器
	 */
	public IteratorQueryTemplate<E> addFilter(QueryFilter filter,
			Object param) {
		filters.add(new FilterInfo(filter, param));
		return this;
	}

	/**
	 * 自定义过滤器
	 */
	public IteratorQueryTemplate<E> addFilter(
			BiFunction<QueryContext, QueryFilterChain, Predicate> filter) {
		filters.add(new FilterInfo(filter));
		return this;
	}

	/**
	 * 默认为true：自动过滤租户
	 */
	public IteratorQueryTemplate<E> setFilterTenant(boolean filterTenant) {
		this.filterTenant = filterTenant;
		return this;
	}

	/**
	 * 查询
	 */
	@SuppressWarnings("unchecked")
	public QueryIterator iterator(QueryRequest request) {
		boolean filterTenant = this.filterTenant
				&& TenantUtil.getTenantId() != TenantUtil.SYSTEM_TENANT;
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<String> query = builder.createQuery(String.class);
		QueryContextImpl<E, String> context = new QueryContextImpl<>(request,
				entityClass, builder, query, filterTenant);
		// where
		applyRequestToWhere(context);
		// order by
		applyRequestToOrderby(context);
		// select
		query.select((Selection<? extends String>) context.simplePath("fdId"));
		// query
		TypedQuery<String> typedQuery = entityManager.createQuery(query);
		return new QueryIterator(typedQuery.getResultList());
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
	 * 查询迭代器
	 * 
	 * @author Li.Shangzhi
	 */
	public class QueryIterator implements Iterator<E> {
		@Getter
		private List<String> ids;
		private int index = -1;

		public boolean isEmpty() {
			return ids.isEmpty();
		}

		public int size() {
			return ids.size();
		}

		public QueryIterator(List<String> ids) {
			super();
			this.ids = ids;
		}

		@Override
		public boolean hasNext() {
			return index + 1 < ids.size();
		}

		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			index++;
			if (index >= ids.size()) {
				return null;
			}
			if (fetchSize == 1) {
				return (E) nextReference();
			} else {
				return (E) nextBatch();
			}
		}

		/**
		 * 采用延迟加载的方式获取当前index的entity
		 */
		private Object nextReference() {
			if (autoFlush && index > 0 && index % DEFAULT_BATCH == 0) {
				entityManager.flush();
				entityManager.clear();
			}
			return entityManager.getReference(entityClass, ids.get(index));
		}

		private List<E> batch;

		/**
		 * 采用批量查询的方式获取当前index的entity
		 */
		private Object nextBatch() {
			int i = index % fetchSize;
			if (autoFlush && index > 0 && i == 0) {
				entityManager.flush();
				entityManager.clear();
			}
			if (i == 0) {
				queryBatch();
			}
			String id = ids.get(index);
			for (E e : batch) {
				if (id.equals(e.getFdId())) {
					return e;
				}
			}
			return null;
		}

		/**
		 * 查询下一批
		 */
		private void queryBatch() {
			List<String> batchIds = ids.subList(index,
					Math.min(ids.size(), index + fetchSize));
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<E> query = builder.createQuery(entityClass);
			query.where(query.from(entityClass).get("fdId").in(batchIds));
			batch = entityManager.createQuery(query).getResultList();
		}
	}
}
