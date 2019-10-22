package com.ibyte.framework.support.loader;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.i18n.ResourceUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.support.ApplicationContextHolder;
import com.ibyte.framework.support.LocalMetaContextHolder;
import com.ibyte.framework.support.domain.MetaModuleImpl;
import com.ibyte.framework.support.persistent.DesignElementApi;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * @Description: <模块信息加载>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-15 00:54
 */
public class ModuleLoader {

    /** 模块标识文件 */
    private static final String RESOURCE_PATTERN = "ApplicationResources\\.properties";

    /**
     * 加载模块
     */
    public void load() {
        Pattern pattern = Pattern.compile(RESOURCE_PATTERN);
        Reflections reflections = new Reflections(
                NamingConstant.BASE_PACKAGE,
                new ResourcesScanner()
        );
        // 获取资源路径
        Set<String> resources = reflections.getResources(pattern);
        resources.forEach(resource->{
            loadByProperties(resource);
        });
    }

    /**
     * 根据ApplicationResources文件加载
     * @param path
     */
    public void loadByProperties(String path){
        int index = path.lastIndexOf('/');
        String moduleName = path
                .substring(NamingConstant.BASE_PACKAGE.length()+1,index)
                .replace('/','_');
        String modulePkg = moduleName.replace('-', '.');
        String messageKey = StringHelper.join(moduleName, ":module.",modulePkg);
        String label = ResourceUtil.getString(messageKey);
        if (StringUtils.isNotBlank(label)) {
            MetaModuleImpl module = (MetaModuleImpl) LocalMetaContextHolder
                    .get()
                    .getOrCreateModule(moduleName);
            module.setLabel(label);
            module.setMessageKey(messageKey);
            module.setAppName(ApplicationContextHolder.getApplicationName());
        }
    }

    /**
     * 加载完成
     */
    public void save() {
        // 保存本地模块
        LocalMetaContextHolder context = LocalMetaContextHolder.get();
        DesignElementApi.get().saveModules(context.getModules());
        // 锁定MetaContextHolder
        LocalMetaContextHolder.get().lock();
    }




}
