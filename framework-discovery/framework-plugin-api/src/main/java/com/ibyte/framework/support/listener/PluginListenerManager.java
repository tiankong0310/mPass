package com.ibyte.framework.support.listener;

import com.alibaba.fastjson.JSONObject;
import com.ibyte.framework.plugin.annotation.ExtendByJson;
import com.ibyte.framework.plugin.annotation.GlobalExtensionPoint;
import com.ibyte.framework.plugin.annotation.ListenerConfig;
import com.ibyte.framework.plugin.annotation.LocalExtensionPoint;
import com.ibyte.framework.plugin.spi.PluginListener;
import com.ibyte.framework.support.builder.ExtensionBuilder;
import com.ibyte.framework.support.builder.ExtensionPointBuilder;
import com.ibyte.framework.support.domain.ExtensionPointImpl;
import com.ibyte.framework.support.listener.impl.MemberExtensionScanListener;
import com.ibyte.framework.support.util.ClassMemberScaner;
import com.ibyte.framework.support.util.PluginReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 插件扫描监听管理器
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

	/** 添加监听器 */
	public void addListener(LocalExtensionPoint point,
							Class<? extends Annotation> clazz) {
		addListener(point.scanMemberFor(), clazz);
	}

	/** 添加监听器 */
	public void addListener(GlobalExtensionPoint point,
							Class<? extends Annotation> clazz) {
		addListener(point.scanMemberFor(), clazz);
	}

	/** 添加监听器 */
	private void addListener(Class<? extends Annotation>[] anns,
							 Class<? extends Annotation> clazz) {
		if (anns.length == 0) {
			return;
		}
		PluginListener listener = new MemberExtensionScanListener(
				clazz.getName());
		ListenerInfo info = new ListenerInfo();
		info.listener = listener;
		info.classAnnotations = Arrays.asList(anns);
		info.memberAnnotations = Arrays.asList(clazz);
		listeners.add(info);
	}

	/** 准备 */
	public void prepare() {
		for (ListenerInfo info : listeners) {
			info.listener.prepare();
		}
	}

	/** 完成并保存 */
	public void save() {
		for (ListenerInfo info : listeners) {
			info.listener.save();
		}
	}

	/** 扫描类 */
	public void onClass(ClassContext clazz,
						Map<String, ExtensionPointImpl> extensionPoints) {
		// 查找匹配的监听器，并触发类开始扫描事件
		boolean needScanMember = false;
		List<ListenerInfo> matchListeners = new ArrayList<>();
		for (ListenerInfo info : listeners) {
			if (matchClass(clazz, info)) {
				matchListeners.add(info);
				if (info.memberAnnotations.size() > 0) {
					needScanMember = true;
				}
				info.listener.onClassStart(clazz);
			}
		}
		// 扫描成员
		if (needScanMember) {
			ClassMemberScaner scaner = new ClassMemberScaner(
					clazz.getRefClass());
			scaner.scan(m -> {
				MemberContext member = buildMemberContext(clazz,
						extensionPoints, m);
				if (member != null) {
					for (ListenerInfo info : matchListeners) {
						if (matchMember(member, info)) {
							info.listener.onMember(clazz, member);
						}
					}
				}
			});
		}
		// 触发类结束扫描事件
		for (ListenerInfo info : matchListeners) {
			info.listener.onClassEnd(clazz);
		}
	}

	/** 构造成员上下文 */
	private MemberContext buildMemberContext(ClassContext clazz,
											 Map<String, ExtensionPointImpl> extensionPoints,
											 AnnotatedElement element) {
		Annotation[] annotations = AnnotationUtils.getAnnotations(element);
		if (annotations == null || annotations.length == 0) {
			return null;
		}
		MemberContext context = new MemberContext(element);
		for (Annotation annotation : annotations) {
			try {
				if (annotation instanceof ExtendByJson) {
					// 注解是非依赖的扩展声明
					ExtendByJson config = (ExtendByJson) annotation;
					String content = IOUtils.resourceToString(config.value(),
							StandardCharsets.UTF_8);
					JSONObject json = JSONObject.parseObject(content);
					JSONObject jPoint = json.getJSONObject("extensionPoint");
					JSONObject jExtension = json.getJSONObject("extension");
					ExtensionPointImpl point = extensionPoints
							.get(jPoint.getString("id"));
					if (point == null) {
						point = ExtensionPointBuilder
								.newVirtualExtensionPoint(jPoint);
					}
					context.addExtension(ExtensionBuilder.newExtension(point,
							clazz.getRefClass(), (Member) element, jExtension));
				} else {
					// 注解可能是扩展点
					ExtensionPointImpl point = extensionPoints
							.get(annotation.annotationType().getName());
					if (point != null) {
						context.addExtension(ExtensionBuilder.newExtension(
								point, clazz.getRefClass(), (Member) element,
								annotation));
					}
				}
			} catch (Exception e) {
				log.error("加载扩展信息时发生错误：" + clazz.getRefClass().getName(), e);
			}
		}
		return context;
	}

	/** 判断当前类是否有监听器关注的内容 */
	private boolean matchClass(ClassContext clazz, ListenerInfo info) {
		for (Class<? extends Annotation> type : info.classAnnotations) {
			if (clazz.getAnnotation(type) != null) {
				return true;
			}
			if (!clazz.getExtensions(type.getName()).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/** 判断当前成员是否有监听器关注的内容 */
	private boolean matchMember(MemberContext member, ListenerInfo info) {
		for (Class<? extends Annotation> type : info.memberAnnotations) {
			if (member.getAnnotation(type) != null) {
				return true;
			}
			if (!member.getExtensions(type.getName()).isEmpty()) {
				return true;
			}
		}
		return false;
	}
}
