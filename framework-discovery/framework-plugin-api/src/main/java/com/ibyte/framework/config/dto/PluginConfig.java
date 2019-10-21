package com.ibyte.framework.config.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: <扩展点配置>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
public class PluginConfig {

    /** 单例扩展点中，被选中的扩展 */
    private List<String> selectedExtensions;

    public List<String> getSelectedExtensions() {
        if (selectedExtensions == null) {
            selectedExtensions = new ArrayList<>();
        }
        return selectedExtensions;
    }

    /** 被禁用的扩展 */
    private List<String> disabledExtensions;

    public List<String> getDisabledExtensions() {
        if (disabledExtensions == null) {
            disabledExtensions = new ArrayList<>();
        }
        return disabledExtensions;
    }
}
