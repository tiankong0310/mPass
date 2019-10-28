package com.ibyte.common.core.query.support;

import com.ibyte.common.core.query.spi.QueryFilter;
import com.ibyte.framework.plugin.annotation.ProviderProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: <通过查询器过滤注册>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-28
 */
public class QueryFilterManager {

    @ProviderProperty
    private static Map<String, QueryFilter> filters = new HashMap<>();

    /**
     * @param key
     * @return
     */
    public static QueryFilter getFilter(String key) {
        return filters.get(key);
    }


}
