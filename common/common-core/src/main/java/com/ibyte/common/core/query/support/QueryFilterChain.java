package com.ibyte.common.core.query.support;

import com.ibyte.common.core.query.spi.QueryFilter;
import com.ibyte.common.util.ArrayUtil;

import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @Description: <查询链路过滤器>
 *
 * @author li.Shangzhi
 */
public class QueryFilterChain {

    private List<FilterInfo> filters;

    private int index = -1;

    public QueryFilterChain(List<FilterInfo> filters) {
        super();
        this.filters = filters;
    }

    /**
     * 执行下一个过滤器
     */
    public Predicate toPredicate(QueryContext context) {
        index++;
        if (index < filters.size()) {
            FilterInfo info = filters.get(index);
            if (info.filter == null) {
                return info.func.apply(context, this);
            } else {
                return info.filter.toPredicate(context, info.param, this);
            }
        } else {
            return null;
        }
    }

    /**
     * 加上下一个过滤器的条件一起返回
     */
    public Predicate andNextFilter(QueryContext context,
                                   List<Predicate> predicates) {
        Predicate next = toPredicate(context);
        if (ArrayUtil.isEmpty(predicates)) {
            return next;
        }
        if (next != null) {
            predicates.add(next);
        }
        return context.getBuilder().and(predicates.toArray(new Predicate[] {}));
    }

    /**
     * 加上下一个过滤器的条件一起返回
     */
    public Predicate andNextFilter(QueryContext context, Predicate predicate) {
        Predicate next = toPredicate(context);
        if (predicate == null) {
            return next;
        }
        if (next == null) {
            return predicate;
        }
        return context.getBuilder().and(predicate, next);
    }

    /**
     * 过滤器+参数
     */
    public static class FilterInfo {
        QueryFilter filter;
        BiFunction<QueryContext, QueryFilterChain, Predicate> func;
        Object param;

        public FilterInfo(QueryFilter filter, Object param) {
            super();
            this.filter = filter;
            this.param = param;
        }

        public FilterInfo(
                BiFunction<QueryContext, QueryFilterChain, Predicate> func) {
            super();
            this.func = func;
        }
    }
}
