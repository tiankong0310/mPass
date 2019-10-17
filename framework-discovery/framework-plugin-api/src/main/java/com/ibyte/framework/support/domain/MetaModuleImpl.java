package com.ibyte.framework.support.domain;

import com.ibyte.framework.meta.MetaModule;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description: <模块信息>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-17
 */
@Setter
@ToString
public class MetaModuleImpl implements MetaModule {

    /** 模块名 */
    private String name;

    /** 中文名 */
    private String label;

    /** 国际化Key */
    private String messageKey;

    /** 所在的应用名 */
    private String appName;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public String getAppName() {
        return appName;
    }
}
