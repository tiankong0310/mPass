package com.ibyte.common.constant;

import com.ibyte.common.util.StringHelper;

/**
 * @Description: <系统常量>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-08
 */
public interface NamingConstant {

    /**
     * m-pass基本包路径
     */
    String BASE_PACKAGE = "com.ibyte";

    /**
     * 包层级标示
     */
    String DOT = ".";

    /**
     * 路径层级标示
     */
    String SLASH = "/";

    /**
     * 中划线
     */
    String STRIKE = "-";

    /**
     * 下滑线
     */
    String UNDERLINE = "_";

    /**
     * 基本包路径前缀
     */
    String BASE_PACKAGE_PREFIX = BASE_PACKAGE + DOT;

    /**
     * m-pass基本包层数
     */
    int BASE_PACKAGE_DEEP = 2;

    /**
     * 类名路径连接串
     */
    String PATH_PREFIX_ENTITY =  DOT + "core.entity" + DOT;

    /**
     * 模块名前缀
     */
    String PATH_PREFIX_MODULE =  "sys";

    /**
     * 内部调用请求
     */
    String PATH_PREFIX_API = "/api";

    /**
     * rest请求
     */
    String PATH_PREFIX_DATA = "/data";

    /**
     * 前端资源请求
     */
    String PATH_PREFIX_WEB = "/web";

    /**
     * 初始数据文件名后缀
     */
    String FILE_EXT_INITDATA = ".json";

    /**
     * 头信息前缀，规则：自定义头都必须以"X-"开头
     */
    String HEADER_PREFIX = "X-";

    /**
     * 头部信息关键字_调用服务名
     */
    String HEADER_KEY_SERVICE = HEADER_PREFIX + "SERVICE-NAME";

    /**
     * 头部信息关键字_当前请求ID
     */
    String HEADER_KEY_REQUEST = HEADER_PREFIX + "REQUEST-ID";

    /**
     * 头部信息关键字_当前用户会话key,默认关键字，可通过设定kmss.auth.token.key调整
     */
    String HEADER_KEY_TOKEN = HEADER_PREFIX + "AUTH-TOKEN";

    /**
     * 配置刷新事件名
     */
    String TOPIC_APP_CONFIG_REFRESH = "app-config-refresh-topic";


    /**
     * 更加简短的名称
     *
     * @param name
     * @return
     */
    static String shortName(String name) {
        if (name.startsWith(BASE_PACKAGE_PREFIX)) {
            return name.substring(NamingConstant.BASE_PACKAGE.length());
        } else {
            return name;
        }
    }

    /**
     * 根据shortName还原
     *
     * @param name
     * @return
     */
    static String oriName(String name) {
        if (name.startsWith(DOT)) {
            return StringHelper.join(NamingConstant.BASE_PACKAGE, name);
        } else {
            return name;
        }
    }
}
