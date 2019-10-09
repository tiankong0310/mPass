package com.ibyte.common.constant;

import org.springframework.core.Ordered;

/**
 * 系统启动事件为ApplicationReadyEvent，监听器可实现Ordered接口来调整启动顺序。
 *
 * 样例：若需要在数据字典加载前做事情，则可将启动顺序调整为：FRAMEWORK - 1
 * 若需要使用数据字典的功能，则启动顺序必须在FRAMEWORK之后，如：FRAMEWORK + 1
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 22:16
 */
public interface ReadyEventOrder {

    /** FRAMEWORK（含插件工厂、数据字典等）的启动顺序 */
    int FRAMEWORK = Ordered.HIGHEST_PRECEDENCE + 1000;
}
