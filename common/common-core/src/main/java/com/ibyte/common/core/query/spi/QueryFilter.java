package com.ibyte.common.core.query.spi;

import com.ibyte.common.core.query.support.QueryContext;
import com.ibyte.common.core.query.support.QueryFilterChain;


import javax.persistence.criteria.Predicate;

/**
 * @Description: <查下过滤器>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-28
 */
public interface QueryFilter {

    /**
     * 构造查询条件
     *
     * @param context
     * @param param
     *            过滤器参数
     * @param nextFilter
     * @return
     */
    Predicate toPredicate(QueryContext context, Object param,
                          QueryFilterChain nextFilter);

}
