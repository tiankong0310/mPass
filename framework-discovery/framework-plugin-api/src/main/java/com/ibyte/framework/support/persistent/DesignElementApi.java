package com.ibyte.framework.support.persistent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JavaType;
import com.ibyte.common.util.JsonUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.common.util.thread.ThreadLocalUtil;
import com.ibyte.framework.meta.MetaSummary;
import com.ibyte.framework.support.ApplicationContextHolder;
import com.ibyte.framework.support.domain.*;
import com.ibyte.framework.support.persistent.dto.DesignElementDetail;
import com.ibyte.framework.support.persistent.dto.DesignElementGroup;
import com.ibyte.framework.support.util.SerializeUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 设计持久化服务
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
@Component
public class DesignElementApi implements InitializingBean, PersistentConstant {
	private static DesignElementApi INSTANCE;

	public static DesignElementApi get() {
		return INSTANCE;
	}

	@Autowired
	private RedissonClient redisson;
	@Autowired
	private DesignElementRemoteApi designElementRemoteApi;
	// ========== 应用 ==========
	/** 保存应用信息 */
	public void saveDbId(String dbId) {
		MetaApplicationImpl app = new MetaApplicationImpl();
		app.setAppName(ApplicationContextHolder.getApplicationName());
		app.setDbId(dbId);
		Map<String, MetaApplicationImpl> map = new HashMap<>(1);
		map.put(app.getAppName(), app);
		saveAll(map, ElementType.MetaApplication);
	}

	/** 应用列表 */
	@SuppressWarnings("unchecked")
	public List<MetaApplicationImpl> findApplications() {
		String path = ElementType.MetaApplication.name();
		List<MetaApplicationImpl> value = (List<MetaApplicationImpl>) getLocalCache()
				.get(path);
		if (value == null) {
			String text = (String) redisson
					.getBucket(path, StringCodec.INSTANCE).get();
			if (text == null) {
				text = designElementRemoteApi.findApplications();
			}
			if (typeOfExtension == null) {
				typeOfExtension = listType(MetaApplicationImpl.class);
			}
			value = JsonUtil.parseObject(text, typeOfExtension);
			getLocalCache().put(path, value);
		}
		return value;
	}

	// ========== 模块 ==========
	/** 取模块信息 */
	public MetaModuleImpl getModule(String id) {
		return getOne(ElementType.MetaModule, id);
	}

	/** 保存模块信息 */
	public void saveModules(Map<String, ?> configs) {
		saveAll(configs, ElementType.MetaModule);
	}

	/** 模块摘要 */
	public List<MetaSummary> findModuleSummary() {
		return findSummary(ElementType.MetaModule.name());
	}

	// ========== Entity ==========
	/** 取Entity */
	public MetaEntityImpl getEntity(String id) {
		return getOne(ElementType.MetaEntity, id);
	}

	/** 保存Entity */
	public void saveEntities(Map<String, ?> configs) {
		saveAll(configs, ElementType.MetaEntity);
	}

	/** Entity摘要 */
	public List<MetaSummary> findEntitySummary() {
		return findSummary(ElementType.MetaEntity.name());
	}

	// ========== API ==========
	/** 取模块信息 */
	public RemoteApi getApi(String id) {
		return getOne(ElementType.RemoteApi, id);
	}

	/** 保存模块信息 */
	public void saveApis(Map<String, ?> configs) {
		saveAll(configs, ElementType.RemoteApi);
	}

	// ========== 扩展点 ==========
	// 扩展点本地有一份缓存，所以不需要获取方法
	/** 存扩展点 */
	public void saveExtensionPoints(Map<String, ?> configs) {
		saveAll(configs, ElementType.ExtensionPoint);
	}

	// ========== 扩展 ==========
	/** 取扩展 */
	public ExtensionImpl getExtension(String pointId, String extensionId) {
		return getOne(ElementType.Extension, pointId, extensionId);
	}

	/** 存扩展点 */
	public void saveExtensions(Map<String, ?> configs) {
		saveAll(configs, ElementType.Extension);
	}

	private volatile JavaType typeOfExtension;

	/** ExtensionPoint详情 */
	@SuppressWarnings("unchecked")
	public List<ExtensionImpl> findExtensions(ExtensionPointImpl point) {
		String path = PersistentConstant.toId(ElementType.Extension,
				point.getId());
		List<ExtensionImpl> value = (List<ExtensionImpl>) getLocalCache()
				.get(path);
		if (value == null) {
			String text = (String) redisson
					.getBucket(path, StringCodec.INSTANCE).get();
			if (text == null) {
				text = designElementRemoteApi.findExtensions(point.getId());
			}
			if (typeOfExtension == null) {
				typeOfExtension = listType(ExtensionImpl.class);
			}
			value = JsonUtil.parseObject(text, typeOfExtension);
			for (ExtensionImpl e : value) {
				e.setPoint(point);
			}
			getLocalCache().put(path, value);
		}
		return value;
	}

	// ========== 通用实现 ==========
	/** 读取配置 */
	@SuppressWarnings("unchecked")
	private <T> T getOne(ElementType type, String... paths) {
		String id = PersistentConstant.toId(type, paths);
		Object value = getLocalCache().get(id);
		if (value != null) {
			return (T) value;
		}
		String text = (String) redisson.getBucket(id, StringCodec.INSTANCE)
				.get();
		if (text == null) {
			text = designElementRemoteApi.get(id);
		}
		if (text == null || JSON_EMPTY.equals(text)) {
			return null;
		}
		value = SerializeUtil.parseObject(text, type.getImpl());
		getLocalCache().put(id, value);
		return (T) value;
	}

	/** 批量保存 */
	private void saveAll(Map<String, ?> configs, ElementType type) {
		// 加载已有的数据
		DesignElementDetail detail = new DesignElementDetail();
		detail.setFdId(type.name());
		detail.setFdAppName(ApplicationContextHolder.getApplicationName());
		List<MetaSummary> summaries = textToSummaryList(
				designElementRemoteApi.findSummaryByApp(detail), false);

		// 计算需要更新的列表
		List<DesignElementDetail> saveList = new ArrayList<>();
		outloop: for (Entry<String, ?> entry : configs.entrySet()) {
			// 通过MD5进行版本校验
			String id = PersistentConstant.toId(type, entry.getKey());
			String content = SerializeUtil.toString(entry.getValue());
			String md5 = buildMd5(content);
			for (int i = 0; i < summaries.size(); i++) {
				MetaSummary summary = summaries.get(i);
				if (id.equals(summary.getId())) {
					summaries.remove(i);
					if (md5.equals(summary.getMd5())) {
						// md5相同则不需要保存
						continue outloop;
					}
					break;
				}
			}
			// 加到更新列表
			detail = new DesignElementDetail();
			detail.setFdId(id);
			detail.setFdContent(content);
			detail.setFdMd5(md5);
			saveList.add(detail);
		}
		// 计算需要移除的列表
		List<String> deleteList = new ArrayList<>();
		for (MetaSummary summary : summaries) {
			deleteList.add(summary.getId());
		}
		// 提交
		if (saveList.isEmpty() && deleteList.isEmpty()) {
			return;
		}
		DesignElementGroup group = new DesignElementGroup();
		group.setFdAppName(ApplicationContextHolder.getApplicationName());
		group.setSaveList(saveList);
		group.setDeleteList(deleteList);
		designElementRemoteApi.saveAll(group);
		// 清理本地缓存
		getLocalCache().clear();
	}

	/** 读取摘要信息，注意：扩展点、扩展不走这里 */
	@SuppressWarnings("unchecked")
	private List<MetaSummary> findSummary(String path) {
		List<MetaSummary> value = (List<MetaSummary>) getLocalCache()
				.get(path);
		if (value != null) {
			return value;
		}
		String text = (String) redisson.getBucket(path, StringCodec.INSTANCE)
				.get();
		if (text == null) {
			text = designElementRemoteApi.findSummary(path);
		}
		value = textToSummaryList(text, true);
		getLocalCache().put(path, value);
		return value;
	}

	/** 计算MD5 */
	private String buildMd5(String content) {
		String md5 = DigestUtils.md5DigestAsHex(
				content.getBytes(StandardCharsets.UTF_8));
		return StringHelper.join(md5,
				Integer.toHexString(content.hashCode()), ':',
				Integer.toHexString(content.length()));
	}

	/** text转summary */
	private List<MetaSummary> textToSummaryList(String text, boolean tranId) {
		if (StringUtils.isBlank(text)) {
			return null;
		}
		JSONArray array = JSONArray.parseArray(text);
		List<MetaSummary> list = new ArrayList<>(array.size());
		for (int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			String id = tranId
					? PersistentConstant.fromId(json.getString(PROP_ID))
					: json.getString(PROP_ID);
			MetaSummary summary = new MetaSummary();
			summary.setId(id);
			summary.setLabel(json.getString(PROP_LABEL));
			summary.setMd5(json.getString(PROP_MD5));
			summary.setMessageKey(json.getString(PROP_MESSAGEKEY));
			summary.setModule(json.getString(PROP_MODULE));
			list.add(summary);
		}
		return list;
	}

	private JavaType listType(Class<?> clazz) {
		return JsonUtil.getMapper().getTypeFactory()
				.constructParametricType(List.class, clazz);
	}

	/** 取线程缓存 */
	private Map<String, Object> getLocalCache() {
		String key = DesignElementApi.class.getName();
		Map<String, Object> cache = ThreadLocalUtil.getLocalVar(key);
		if (cache == null) {
			cache = new HashMap<>(16);
			ThreadLocalUtil.setLocalVar(key, cache);
		}
		return cache;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;
	}
}
