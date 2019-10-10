package com.ibyte.common.web;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 过滤器定义虚拟类，
 *  继承自接口的过滤器
 *      如果声明为springbean，他自动加载到请求过滤链（因为继承了GenericFilterBean接口，spring默认把继承此类的过滤器bean加到web过滤链）中，
 *              通过注解@order定义其优先级
 *      如果不申明为springbean，又要加入到过滤链中，可以通过FilterRegistrationBean定义，并指定优先级
 * @author li.Shangzhi
 */
public abstract class BaseWebFilter extends OncePerRequestFilter {

    /**
     * 返回类名，避免filter不被执行
     *
     * @return
     */
    @Override
    protected String getFilterName() {
        return null;
    }
}
