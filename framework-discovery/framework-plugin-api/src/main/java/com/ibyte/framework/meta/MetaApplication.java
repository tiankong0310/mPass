package com.ibyte.framework.meta;

/**
 * @Description: <应用信息>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
public interface MetaApplication {

    /**
     * 读-应用名
     *
     * @return
     */
    String getAppName();

    /**
     * 读-数据库标识
     *
     * @return
     */
    String getDbId();
}
