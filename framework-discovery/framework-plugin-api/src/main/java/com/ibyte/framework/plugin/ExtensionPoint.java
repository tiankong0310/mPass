package com.ibyte.framework.plugin;

/**
 * @Description: <拓展点>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
public interface ExtensionPoint {

    /**
     * ID，取注解类名
     *
     * @return
     */
    String getId();

    /**
     * 中文名
     *
     * @return
     */
    String getLabel();

    /**
     * 模块
     *
     * @return
     */
    String getModule();

    /**
     * 是否可配置
     *
     * @return
     */
    boolean isConfigurable();

    /**
     * 是否有序
     *
     * @return
     */
    boolean isOrdered();

    /**
     * 是否单例
     *
     * @return
     */
    boolean isSingleton();

    /**
     * 配置类名
     *
     * @return
     */
    String getConfig();

    /**
     * 注解所在类接口名
     *
     * @return
     */
    String getBaseOn();
}
