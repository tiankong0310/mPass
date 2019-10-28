package com.ibyte.common.core.query.spi;

import com.ibyte.common.core.query.support.QueryFilterManager;
import com.ibyte.framework.plugin.annotation.LocalExtensionPoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: <查询过滤器拓展点>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-28
 */
@LocalExtensionPoint(label = "查询过滤器", baseOn = QueryFilter.class, manager = QueryFilterManager.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryFilterExtension {

    /**
     * 过滤器ID
     */
    String id();
}
