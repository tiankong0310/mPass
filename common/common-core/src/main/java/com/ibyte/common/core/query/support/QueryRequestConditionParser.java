package com.ibyte.common.core.query.support;

import com.ibyte.common.core.constant.QueryConstant;
import com.ibyte.common.core.query.spi.QueryFilter;
import com.ibyte.common.core.constant.QueryConstant.Operator;
import com.ibyte.common.core.query.spi.QueryFilter;
import com.ibyte.common.exception.ParamsNotValidException;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * QueryRequest的查询条件解析器
 *
 * @author li.Shangzhi
 */
public class QueryRequestConditionParser implements QueryFilter {
	public static QueryRequestConditionParser INSTANCE = new QueryRequestConditionParser();

	@Override
	public Predicate toPredicate(QueryContext context, Object param,
			QueryFilterChain nextFilter) {
		// 解析查询条件
		@SuppressWarnings("unchecked")
		Map<String, Object> conditions = (Map<String, Object>) param;
		if (conditions == null) {
			return nextFilter.toPredicate(context);
		}
		List<Predicate> predicates = new ArrayList<>();
		for (Entry<String, Object> entry : conditions.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map) {
				for (Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
					Operator opt = Operator.get(e.getKey());
					if (opt == null) {
						throw new ParamsNotValidException(
								"无法识别的操作符号：" + e.getKey());
					}
					predicates.add(context.toPredicate(entry.getKey(), opt,
							e.getValue()));
				}
			} else {
				predicates.add(context.toPredicate(entry.getKey(), Operator.eq,
						value));
			}
		}
		// 加上子查询条件返回
		return nextFilter.andNextFilter(context, predicates);
	}
}
