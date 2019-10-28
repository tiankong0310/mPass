package com.ibyte.common.core.query;

import com.ibyte.common.core.constant.QueryConstant.Operator;
import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.core.entity.TreeEntity;
import com.ibyte.common.core.util.EntityUtil;
import com.ibyte.common.core.util.PropertyLangUtil;
import com.ibyte.common.core.util.TypeUtil;
import com.ibyte.common.exception.ParamsNotValidException;
import com.ibyte.common.util.DateUtil;
import com.ibyte.common.util.LangUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.common.util.TenantUtil;
import com.ibyte.framework.meta.Meta;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.meta.MetaEntity;
import com.ibyte.framework.meta.MetaProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.criteria.*;
import java.util.*;

/**
 * 查询简化工具
 *
 * @author li.Shangzhi
 *
 * @param <E>
 * @param <V>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class QueryHelper<E extends IEntity, V> {
	@Getter
	private CriteriaBuilder builder;
	@Getter
	private Class<E> entityClass;
	@Getter
	private Root<E> root;
	@Getter
	private CriteriaQuery<V> query;
	@Getter
	private boolean filterTenant;
	@Setter
	private boolean andCriteria = true;
	@Getter
	private MetaEntity meta;
	private Map<String, Join<?, ?>> joinMap = new HashMap<>();
	private boolean langSupport = LangUtil.isSuportEnabled();

	public QueryHelper(Class<E> entityClass,
                       CriteriaBuilder builder, CriteriaQuery<V> query,
                       boolean filterTenant) {
		super();
		this.builder = builder;
		this.entityClass = entityClass;
		this.query = query;
		this.filterTenant = filterTenant;
		this.root = query.from(entityClass);
		this.meta = Meta.getEntity(entityClass.getName());
	}

	/** 解释筛选条件 */
	public Predicate toPredicate(String property, Operator opt, Object value) {
		// 数据校验
		List<MetaProperty> props = EntityUtil.getMetaProperty(meta, property);
		MetaProperty prop = props.get(props.size() - 1);
		validateType(opt, prop.getType());

		boolean leftIsCollection = isCollection(props);
		if (opt.isPropOperator()) {
			// 两个字段的对比筛选，列表属性不支持
			List<MetaProperty> valueProps = EntityUtil.getMetaProperty(meta,
					String.valueOf(value));
			if (leftIsCollection || isCollection(valueProps)) {
				throw new ParamsNotValidException(
						StringHelper.join(opt, "操作不支持数组"));
			}
			MetaProperty valueProp = valueProps.get(valueProps.size() - 1);
			validateType(opt, valueProp.getType());
		}
		if (opt.isPropOperator()) {
			// 两个字段的对比筛选
			return propPredicate(prop, property, opt, value);
		} else {
			if (leftIsCollection) {
				// 列表属性筛选
				return listPredicate(props, property, opt, value);
			} else {
				// 单值属性筛选
				return singlePredicate(prop, property, opt, value);
			}
		}
	}

	/** 是否是列表 */
	private boolean isCollection(List<MetaProperty> props) {
		for (MetaProperty prop : props) {
			if (prop.isCollection()) {
				return true;
			}
		}
		return false;
	}

	/** 字段类型对应操作的校验 */
	private void validateType(Operator opt, String type) {
		// 类型校验
		switch (opt) {
		case gt:
		case gte:
		case lt:
		case lte:
		case gtProp:
		case gteProp:
		case ltProp:
		case lteProp:
			break;
		case startsWith:
		case contains:
		case startsWithProp:
		case containsProp:
			if (!MetaConstant.isString(type)) {
				throw new ParamsNotValidException(
						StringHelper.join(opt, "操作仅支持字符串"));
			}
			break;
		case child:
			if (!MetaConstant.isAssociation(type)) {
				throw new ParamsNotValidException(
						StringHelper.join(opt, "操作仅支持对象"));
			}
			break;
		// date的条件
		case eqDate:
		case gtDate:
		case gteDate:
		case ltDate:
		case lteDate:
			if (!MetaConstant.isDate(type)) {
				throw new ParamsNotValidException(
						StringHelper.join(opt, "操作仅支持日期/时间"));
			}
			break;
		default:
		}
	}

	/** 列表属性路径 */
	private Predicate listPredicate(List<MetaProperty> props,
			String property, Operator opt, Object value) {
		MetaProperty prop = props.get(props.size() - 1);
		Subquery<String> subquery = query.subquery(String.class);
		Root<E> subRoot = subquery.from(entityClass);
		subquery.select(subRoot.get("fdId"));
		From<?, ?> current = subRoot;
		int n = props.size() - 1;
		for (int i = 0; i < n; i++) {
			Join<?, ?> join = current.join(props.get(i).getName(),
					JoinType.INNER);
			current = join;
		}
		Path<?> path = current.get(prop.getName());
		Predicate predicate = valuePredicate(prop, property, path, opt, value);
		subquery.where(builder.and(predicate, builder.equal(root.get("fdId"),
				subRoot.get("fdId"))));
		return builder.exists(subquery);

	}

	/** 单值属性筛选 */
	private Predicate singlePredicate(MetaProperty prop, String property,
			Operator opt, Object value) {
		Path<?> path;
		if (opt == Operator.isNull) {
			path = leftJoinPath(property);
		} else {
			path = innerJoinPath(property);
		}
		return valuePredicate(prop, property, path, opt, value);
	}

	/** 字段vs值的筛选（含多语言） */
	private Predicate valuePredicate(MetaProperty prop, String property,
			Path left, Operator opt, Object value) {
		if (langSupport && prop.isLangSupport()) {
			String langName = PropertyLangUtil
					.getPropertyNameByLanguage(prop.getName());
			if (langName != null) {
				Path langPath = left.getParentPath().get(langName);
				return builder.or(
						simpleValuePredicate(prop, property, left, opt, value),
						simpleValuePredicate(prop, property, langPath, opt,
								value));
			}
		}
		return simpleValuePredicate(prop, property, left, opt, value);
	}

	/** 字段vs值的筛选（不含多语言） */
	private Predicate simpleValuePredicate(MetaProperty prop, String property,
			Path left, Operator opt, Object value) {
		// 无值操作：isNull/isNotNull
		if (opt == Operator.isNull) {
			return builder.isNull(left);
		} else if (opt == Operator.isNotNull) {
			return builder.isNotNull(left);
		}

		value = TypeUtil.cast(value, prop);
		if (value == null) {
			throw new ParamsNotValidException(
					StringHelper.join(property, "的条件值不能为空"));
		}
		// eq和in的转换
		if (opt == Operator.eq && value instanceof List) {
			opt = Operator.in;
		} else if (opt == Operator.in && !(value instanceof List)) {
			opt = Operator.in;
		}
		// 解析条件
		switch (opt) {
		case eq:
			return builder.equal(left, value);
		case neq:
			return builder.notEqual(left, value);
		case in:
			return builder.in(left).value((List<?>) value);
		case notIn:
			return builder.in(left).value((List<?>) value).not();
		case gt:
			return builder.greaterThan(left, (Comparable<Object>) value);
		case gte:
			return builder.greaterThanOrEqualTo(left,
					(Comparable<Object>) value);
		case lt:
			return builder.lessThan(left, (Comparable<Object>) value);
		case lte:
			return builder.lessThanOrEqualTo(left, (Comparable<Object>) value);
		case startsWith:
			return builder.like(left, StringHelper.join(value, "%"));
		case contains:
			return builder.like(builder.lower(left),
					StringHelper.join("%", value, "%").toLowerCase());
		case child:
			if (value instanceof TreeEntity) {
				return builder.like(left, StringHelper
						.join(((TreeEntity) value).getFdHierarchyId(), "%"));
			} else {
				throw new ParamsNotValidException("child操作仅支持TreeEntity");
			}
			// date的条件
		case eqDate:
			return builder.and(
					builder.greaterThanOrEqualTo(left,
							DateUtil.dateBegin((Date) value)),
					builder.lessThan(left, DateUtil.dateEnd((Date) value)));
		case gtDate:
			return builder.greaterThanOrEqualTo(left,
					DateUtil.dateEnd((Date) value));
		case gteDate:
			return builder.greaterThanOrEqualTo(left,
					DateUtil.dateBegin((Date) value));
		case ltDate:
			return builder.lessThan(left,
					DateUtil.dateBegin((Date) value));
		case lteDate:
			return builder.lessThan(left,
					DateUtil.dateEnd((Date) value));
		default:
		}
		throw new ParamsNotValidException("未知的操作类型:" + opt);
	}

	/** 字段vs字段的筛选 */
	private Predicate propPredicate(MetaProperty prop,
			String property, Operator opt, Object value) {
		Path left;
		Path right;
		if (opt == Operator.eqProp) {
			left = leftJoinPath(property);
			right = leftJoinPath(String.valueOf(value));
		} else {
			left = innerJoinPath(property);
			right = innerJoinPath(String.valueOf(value));
		}
		switch (opt) {
		case eqProp:
			return builder.equal(left, right);
		case gtProp:
			return builder.greaterThan(left, right);
		case gteProp:
			return builder.greaterThanOrEqualTo(left, right);
		case ltProp:
			return builder.lessThan(left, right);
		case lteProp:
			return builder.lessThanOrEqualTo(left, right);
		case startsWithProp:
			return builder.like(left, builder.concat(right, "%"));
		case containsProp:
			return builder.like(builder.lower(left),
					builder.concat(
							builder.concat("%", builder.lower(right)),
							"%"));
		default:
		}
		throw new ParamsNotValidException("未知的操作类型:" + opt);
	}

	/** 把a.b.c解析成Path对象 */
	public Path<?> getPath(String property, JoinType joinType) {
		List<MetaProperty> props = EntityUtil.getMetaProperty(meta, property);
		return getPath(props, joinType);
	}

	/** 把a.b.c解析成Path对象 */
	private Path<?> getPath(List<MetaProperty> props, JoinType joinType) {
		StringBuilder pathName = new StringBuilder();
		From<?, ?> current = root;
		int n = props.size() - 1;
		// a.b.c，需要join a join b，有join过就不需要再join了
		for (int i = 0; i < n; i++) {
			MetaProperty prop = props.get(i);
			if (i == 0) {
				pathName.append(prop.getName());
			} else {
				pathName.append('.').append(prop.getName());
			}
			String key = pathName.toString();
			Join<?, ?> join = joinMap.get(key);
			if (join == null) {
				join = current.join(prop.getName(), joinType);
				if (filterTenant) {
					join.on(builder.equal(join.get("fdTenantId"),
							TenantUtil.getTenantId()));
				}
				joinMap.put(key, join);
			}
			current = join;
		}
		return current.get(props.get(n).getName());
	}

	/** 简单属性的Path，如fdName */
	public Path<?> simplePath(String property) {
		return root.get(property);
	}

	/** 把a.b.c解析成Path对象 */
	public Path<?> leftJoinPath(String property) {
		return getPath(property, JoinType.LEFT);
	}

	/** 把a.b.c解析成Path对象 */
	public Path<?> innerJoinPath(String property) {
		return getPath(property, andCriteria ? JoinType.INNER : JoinType.LEFT);
	}

	/** 获取所有的join */
	public Collection<Join<?, ?>> getJoins() {
		return joinMap.values();
	}

	/** 当前的筛选属于and条件筛选，可以使用inner join提升性能 */
	public boolean isAndCriteria() {
		return andCriteria;
	}
}
