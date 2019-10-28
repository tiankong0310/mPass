package com.ibyte.common.core.query.support;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import com.ibyte.common.core.dto.QueryRequest;
import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.core.query.QueryHelper;

/**
 * 查询上下文实现类
 *
 * @author li.Shangzhi
 *
 * @param <E>
 * @param <V>
 */
public class QueryContextImpl<E extends IEntity, V> extends QueryHelper<E, V>
        implements QueryContext {
    private QueryRequest request;

    public QueryContextImpl(QueryRequest request, Class<E> entityClass,
                            CriteriaBuilder builder, CriteriaQuery<V> query,
                            boolean filterTenant) {
        super(entityClass, builder, query, filterTenant);
        this.request = request;
    }

    @Override
    public QueryRequest getRequest() {
        return request;
    }
}
