package com.ibyte.component.spring;

import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于资源配置的加载，主要配合注解PropertySource使用，支持yml及properties文件的加载，如：
 * PropertySource(value = {"classpath:config/auth.yml","classpath:config/feign.properties"}, factory = ResourceLoadFactory.class,ignoreResourceNotFound = true, encoding = "UTF-8")
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 11:07
 */
public class ResourceLoadFactory  implements PropertySourceFactory {

    private static List<PropertySourceLoader> loaders = new ArrayList<PropertySourceLoader>(2);

    /**
     * 暂时只支持两种配置文件加载 properties 、yml
     */
    static {
        loaders.add(new YamlPropertySourceLoader());
        loaders.add(new PropertiesPropertySourceLoader());
    }

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        PropertySource<?> propSource = null;
        String propName = name;
        if (StringUtils.isEmpty(propName)) {
            propName = getNameForResource(resource.getResource());
        }
        if (resource.getResource().exists()) {
            String fileName = resource.getResource().getFilename();
            for (PropertySourceLoader loader : loaders) {
                if (checkFileType(fileName, loader.getFileExtensions())) {
                    List<PropertySource<?>> propertySources = loader.load(propName, resource.getResource());
                    if (!propertySources.isEmpty()) {
                        propSource = propertySources.get(0);
                    }
                }
            }
        } else {
            throw new FileNotFoundException(propName + "对应文件'" + resource.getResource().getFilename() + "'不存在");
        }
        return propSource;
    }

    private boolean checkFileType(String fileName, final String[] fileTypes) {
        if (!StringUtils.isEmpty(fileName)) {
            for (String tmpFileType : fileTypes) {
                if (fileName.toLowerCase().endsWith(tmpFileType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getNameForResource(Resource resource) {
        String name = resource.getDescription();
        if (!StringUtils.hasText(name)) {
            name = resource.getClass().getSimpleName() + "@" + System.identityHashCode(resource);
        }
        return name;
    }
}
