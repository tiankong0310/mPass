package com.ibyte.common.web;

import javax.servlet.Filter;

/**
 * web filter定义接口
 * 可以通过此接口定义filter接口，这样做对应的filter可以在WebThreadFilter之后执行，
 * 并且在springsecurity之前运行。
 *
 * @author li.Shangzhi
 * @Date: 2019-10-10
 */
public interface IWebFilterDef {

    /**
     * 默认排序号
     */
    int FILTER_ORDER_DEFAULT = 0;

    /**
     * 获取过滤器实例
     *
     * @return
     */
    Filter getFilterInstance();

    /**
     * 适用场景
     *
     * @param clazz 使用该filter对应类
     * @return
     */
    default boolean support(Class<?> clazz) {
        return WebThreadFilter.class.equals(clazz);
    }

}
