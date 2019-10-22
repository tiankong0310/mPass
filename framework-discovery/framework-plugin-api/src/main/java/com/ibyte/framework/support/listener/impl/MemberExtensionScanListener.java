package com.ibyte.framework.support.listener.impl;

import com.ibyte.framework.plugin.spi.PluginListener;
import com.ibyte.framework.support.domain.ExtensionImpl;
import com.ibyte.framework.support.listener.ClassContext;
import com.ibyte.framework.support.listener.MemberContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: <将成员的扩展写入到类的扩展中，使成员扩展正式生效>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-22
 */
@Slf4j
public class MemberExtensionScanListener implements PluginListener {

    private String pointId;

    public MemberExtensionScanListener(String pointId) {
        super();
        this.pointId = pointId;
    }

    @Override
    public void onMember(ClassContext clazz, MemberContext member) {
        for (ExtensionImpl extension : member.getExtensions(pointId)) {
            clazz.addExtension(extension);
        }
    }
}
