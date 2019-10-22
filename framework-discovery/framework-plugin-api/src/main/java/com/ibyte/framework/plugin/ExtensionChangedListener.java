package com.ibyte.framework.plugin;

/**
 * @Description: <拓展点监听器管理>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-22
 */
public interface ExtensionChangedListener {

    /**
     * 扩展变更时触发
     *
     * @throws Exception
     */
    void onExtensionChanged() throws Exception;
}
