package com.ibyte.framework.support;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.framework.meta.MetaEntity;
import com.ibyte.framework.meta.MetaModule;
import com.ibyte.framework.support.domain.MetaEntityImpl;
import com.ibyte.framework.support.domain.MetaModuleImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author li.Shangzhi
 * @Description: <元数据上下文>
 * @Date: 2019-10-17
 */
public class LocalMetaContextHolder {

    private final static LocalMetaContextHolder INSTANCE = new LocalMetaContextHolder();

    public final static LocalMetaContextHolder get() {
        return INSTANCE;
    }

    private volatile boolean locked = false;

    private Map<String, MetaModule> modules = new HashMap<>();

    /** 锁定前,获取模块列表 */
    public Map<String, MetaModule> getModules() {
        if (locked) {
            return null;
        }
        return modules;
    }

    /** 获取模块 */
    public MetaModule getModule(String name) {
      return modules.get(name);
    }

    /** 获取模块，在锁定前没有该模块则创建 */
    public MetaModule getOrCreateModule(String name) {
        MetaModuleImpl module = (MetaModuleImpl) modules.get(name);
        if (module == null && !locked) {
            module = new MetaModuleImpl();
            module.setName(name);
            modules.put(name, module);
        }
        return module;
    }

    /** 获取某个类所属的模块 */
    public String matchModule(String className) {
        if (!className.startsWith(NamingConstant.BASE_PACKAGE)) {
            return null;
        }
        String[] paths = className.toLowerCase().split("\\.");
        StringBuilder sb = new StringBuilder();
        String moduleName = null;
        for (int i = NamingConstant.BASE_PACKAGE_DEEP; i < paths.length
                - 1; i++) {
            if (sb.length() > 0) {
                sb.append('-');
            }
            sb.append(paths[i]);
            String name = sb.toString();
            if (modules.containsKey(name)) {
                moduleName = name;
            } else {
                if (moduleName != null) {
                    return moduleName;
                }
            }

        }
        return moduleName;
    }

    private Map<String, MetaEntity> entities = new HashMap<>();

    /** 锁定前 获取表列表 */
    public Map<String, MetaEntity> getEntities() {
        if (locked) {
            return null;
        }
        return entities;
    }

    /** 获取表 */
    public MetaEntity getEntity(String name) {
        return entities.get(name);
    }

    /** 获取表，在锁定前没有该表则创建 */
    public MetaEntity getOrCreateEntity(String name) {
        MetaEntityImpl entity = (MetaEntityImpl) entities.get(name);
        if (entity == null && !locked) {
            entity = new MetaEntityImpl();
            entity.setEntityName(name);
            entities.put(name, entity);
        }
        return entity;
    }

    /** 锁定 */
    public void lock() {
        if (locked) {
            return;
        }
        locked = true;
        for (MetaEntity e : entities.values()) {
            MetaEntityImpl entity = (MetaEntityImpl) e;
            entity.setProperties(
                    Collections.unmodifiableMap(entity.getProperties()));
        }
    }


}
