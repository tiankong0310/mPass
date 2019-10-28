package com.ibyte.common.core.query;

import com.ibyte.common.core.dto.IViewObject;
import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.util.LangUtil;
import sun.misc.ObjectInputFilter;

import javax.persistence.EntityManager;

/**
 * @Description: <通用查下模块>
 *
 * @param <E>
 * @param <V>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-28
 */
public class PageQueryTemplate<E extends IEntity, V> {

    private EntityManager entityManager;
    private Class<E> entityClass;
    private Class<V> viewClass;
    private boolean isViewObject;
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

}
