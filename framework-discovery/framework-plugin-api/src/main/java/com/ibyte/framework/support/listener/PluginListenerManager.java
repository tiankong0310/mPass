package com.ibyte.framework.support.listener;

import com.ibyte.framework.plugin.annotation.ListenerConfig;
import com.ibyte.framework.plugin.spi.PluginListener;
import com.ibyte.framework.support.util.PluginReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 插件扫描监听管理器
 *
 * // TODO 插件扫描待完善
 * 
 * @author li.Shangzhi
 */
@Slf4j
public class PluginListenerManager {
	public class ListenerInfo {
		List<Class<? extends Annotation>> classAnnotations;
		List<Class<? extends Annotation>> memberAnnotations;
		PluginListener listener;
	}

	private List<ListenerInfo> listeners = new ArrayList<>();

	/** 添加监听器 */
	public void addListener(ListenerConfig config) {
		Class<? extends Annotation>[] anns = config.classAnnotation();
		PluginListener listener = PluginReflectUtil
				.newInstance(config.listener());
		if (listener == null || anns.length == 0) {
			return;
		}

		ListenerInfo info = new ListenerInfo();
		info.listener = listener;
		info.classAnnotations = Arrays.asList(anns);
		info.memberAnnotations = Arrays.asList(config.memberAnnotation());
		listeners.add(info);
	}
}
