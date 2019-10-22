package com.ibyte.framework.plugin;

import com.ibyte.framework.support.ApplicationContextHolder;
import com.ibyte.framework.support.PluginContextHolder;
import com.ibyte.framework.support.domain.ExtensionImpl;
import com.ibyte.framework.support.domain.ExtensionPointImpl;
import com.ibyte.framework.support.persistent.DesignElementApi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 插件实现获取接口
 *
 * @author li.Shangzhi
 * @Date: 2019-10-22
 */
public class Plugin {
	// ========== 单例扩展 ==========
	/** 根据Provider类型获取Provider，用于单实现的扩展点 */
	public static <T> T getProvider(Class<T> clazz) {
		ExtensionPointImpl point = PluginContextHolder
				.getExtensionPointByProvider(clazz);
		Extension extension = getExtension(point);
		return extension == null ? null : extension.getProvider();
	}

	/** 根据扩展点获取扩展，用于单实现的扩展点 */
	public static Extension
			getExtension(Class<? extends Annotation> pointClass) {
		ExtensionPointImpl point = PluginContextHolder
				.getExtensionPoint(pointClass.getName());
		return getExtension(point);
	}

	private static Extension getExtension(ExtensionPointImpl point) {
		if (point == null) {
			return null;
		}
		List<ExtensionImpl> extensions = point.isGlobal()
				? DesignElementApi.get().findExtensions(point)
				: PluginContextHolder.getLocalExtension(point.getId());
		if (extensions != null && !extensions.isEmpty()) {
			extensions.get(0);
		}
		return null;
	}

	// ========== 根据ID获取扩展 ==========
	/** 根据Provider类型和扩展ID获取Provider */
	public static <T> T getProvider(Class<T> clazz, String extensionId) {
		ExtensionPointImpl point = PluginContextHolder
				.getExtensionPointByProvider(clazz);
		Extension extension = getExtension(point, extensionId);
		return extension == null ? null : extension.getProvider();
	}

	/** 根据扩展点和扩展ID获取扩展 */
	public static Extension getExtension(Class<? extends Annotation> pointClass,
			String extensionId) {
		ExtensionPointImpl point = PluginContextHolder
				.getExtensionPoint(pointClass.getName());
		return getExtension(point, extensionId);
	}

	private static Extension getExtension(ExtensionPointImpl point,
			String extensionId) {
		if (point == null) {
			return null;
		}
		if (point.isGlobal()) {
			return DesignElementApi.get().getExtension(point.getId(),
					extensionId);
		} else {
			List<ExtensionImpl> extensions = PluginContextHolder
					.getLocalExtension(point.getId());
			if (extensions != null) {
				for (ExtensionImpl extension : extensions) {
					if (extensionId.equals(extension.getId())) {
						return extension;
					}
				}
			}
			return null;
		}
	}

	// ========== 所有扩展 ==========
	/** 根据Provider类型获取Provider */
	public static <T> List<T> getProviders(Class<T> clazz) {
		ExtensionPointImpl point = PluginContextHolder
				.getExtensionPointByProvider(clazz);
		List<Extension> extensions = getExtensions(point);
		List<T> providers = new ArrayList<>();
		for (Extension extension : extensions) {
			providers.add(extension.getProvider());
		}
		return providers;
	}

	/** 根据扩展点获取扩展 */
	public static List<Extension>
			getExtensions(Class<? extends Annotation> pointClass) {
		ExtensionPointImpl point = PluginContextHolder
				.getExtensionPoint(pointClass.getName());
		return getExtensions(point);
	}

	@SuppressWarnings("unchecked")
	private static List<Extension> getExtensions(ExtensionPointImpl point) {
		if (point == null) {
			return Collections.emptyList();
		}
		List<?> extensions = point.isGlobal()
				? DesignElementApi.get().findExtensions(point)
				: PluginContextHolder.getLocalExtension(point.getId());
		return extensions == null ? Collections.emptyList()
				: (List<Extension>) extensions;
	}

	// ========== API构造 ==========
	/** 根据API类名，获取API */
	public static <T> T getApi(String apiName) {
		return ApplicationContextHolder.getApi(apiName);
	}

	/** 使用指定接口访问API */
	public static <T> T getApi(String apiName, Class<T> iface) {
		if (iface == null) {
			return ApplicationContextHolder.getApi(apiName);
		}
		return ApplicationContextHolder.getApi(apiName, iface);
	}
}
