package com.ibyte.framework.support.listener;

import com.ibyte.framework.support.domain.ExtensionImpl;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: <上下文基类>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
@Getter
public abstract class AbstractContext {
    protected List<ExtensionImpl> extensions = new ArrayList<>();

    /** 添加扩展 */
    public void addExtension(ExtensionImpl extension) {
        if (!extensions.contains(extension)) {
            extensions.add(extension);
        }
    }

    /** 获取扩展并从上下文中移除 */
    public List<ExtensionImpl> getAndRemoveExtensions(String pointId) {
        List<ExtensionImpl> result = new ArrayList<>();
        for (int i = extensions.size() - 1; i >= 0; i--) {
            ExtensionImpl extension = extensions.get(i);
            if (extension.getPoint().getAnnotationTypes().contains(pointId)) {
                result.add(extension);
                extensions.remove(i);
            }
        }
        return result;
    }

    /** 获取扩展 */
    public List<ExtensionImpl> getExtensions(String pointId) {
        List<ExtensionImpl> result = new ArrayList<>();
        for (ExtensionImpl extension : extensions) {
            if (extension.getPoint().getAnnotationTypes().contains(pointId)) {
                result.add(extension);
            }
        }
        return result;
    }
}
