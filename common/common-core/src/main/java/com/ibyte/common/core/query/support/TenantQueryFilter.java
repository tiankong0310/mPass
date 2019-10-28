package com.ibyte.common.core.query.support;

import com.ibyte.common.core.constant.QueryConstant.Operator;
import com.ibyte.common.core.query.spi.QueryFilter;
import com.ibyte.common.util.TenantUtil;

import javax.persistence.criteria.Predicate;

/**
 * 多租户过滤器
 *
 * @author li.Shangzhi
 */
public class TenantQueryFilter implements QueryFilter {
	private static final TenantQueryFilter INSTANCE = new TenantQueryFilter();

	public static final QueryFilter getInstance() {
		return INSTANCE;
	}

	private TenantQueryFilter() {
	}

	@Override
	public Predicate toPredicate(QueryContext context, Object param,
			QueryFilterChain nextFilter) {
		int tenantId = TenantUtil.getTenantId();
		if (tenantId == TenantUtil.SYSTEM_TENANT) {
			return nextFilter.toPredicate(context);
		}
		return nextFilter.andNextFilter(context,
				context.toPredicate("fdTenantId", Operator.eq, tenantId));
	}
}
