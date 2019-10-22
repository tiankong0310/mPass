package com.ibyte.framework.plugin.spi;

import com.ibyte.framework.support.listener.ClassContext;
import com.ibyte.framework.support.listener.MemberContext;

/**
 * @Description: <插件扫描监听>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
public interface PluginListener {

    /**
     * 准备
     */
    default void prepare() {
    }

    /**
     * 当开始扫描类时触发
     *
     * @param clazz
     */
    default void onClassStart(ClassContext clazz) {
    }

    /**
     * 当扫描成员时触发
     *
     * @param clazz
     * @param member
     */
    default void onMember(ClassContext clazz, MemberContext member) {
    }

    /**
     * 当结束扫描类时触发
     *
     * @param clazz
     */
    default void onClassEnd(ClassContext clazz) {
    }

    /**
     * 完成并保存
     */
    default void save() {
    }

}
