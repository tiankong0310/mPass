package com.ibyte.common.core.data.handler;

import com.ibyte.common.core.data.IData;

/**
 * @Description: <字段处理>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-14
 */
public interface FieldHandler {

    /**
     * 是否支持
     *
     * @param entity
     * @return
     */
    boolean support(IData entity);

    /**
     * 初始化前调用
     *
     * @param entity
     */
    default void doInit(IData entity) {
    }

    /**
     * add/update(Entity)前调用
     *
     * @param entity
     * @param isAdd
     */
    default void beforeSaveOrUpdate(IData entity, boolean isAdd) {
    }

    /**
     * update(VO)前调用
     *
     * @param entity
     */
    default void beforeVOUpdate(IData entity) {
    }
}
