package com.ibyte.framework.meta;

import com.ibyte.framework.support.LocalMetaContextHolder;

/**
 * @Description: <模块信息>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-17
 */
public interface MetaModule {

    /**
     * 取本地模块
     *
     * @param moduleName
     * @return
     */
    public static MetaModule localModule(String moduleName) {
        return LocalMetaContextHolder.get().getModule(moduleName);
    }

    /**
     * 读-取字段名
     *
     * @return
     */
    String getName();

    /**
     * 读-中文名
     *
     * @return
     */
    String getLabel();

    /**
     * 读-MessageKey
     *
     * @return
     */
    String getMessageKey();

    /**
     * 读-所在的应用名
     *
     * @return
     */
    String getAppName();
}
