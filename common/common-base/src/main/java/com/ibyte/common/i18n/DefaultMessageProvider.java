package com.ibyte.common.i18n;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.util.StringHelper;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @Description: <默认的MessageProvider实现：从本地文件读取>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 21:53
 */
public class DefaultMessageProvider implements MessageProvider{


    public final static String APPLICATION_RESOURCE_NAME = "resource.ApplicationResources";

    private final static String GLOBAL_APPLICATION_RESOURCE_NAME = NamingConstant.BASE_PACKAGE
            + ".resource.ApplicationResources";

    @Override
    public String getString(String key, String bundle, Locale locale) {
        String baseName = getBaseName(bundle);
        try {
            return ResourceBundle.getBundle(baseName, locale).getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    @Override
    public Map<String, String> getStrings(String prefixKey, String bundle, Locale locale) {
        String baseName = getBaseName(bundle);
        try {
            Map<String, String> rtnMap = new HashMap<>(1);
            ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName);
            for (String key : resourceBundle.keySet()) {
                if (key.startsWith(prefixKey)) {
                    rtnMap.put(key, resourceBundle.getString(key));
                }
            }
            return rtnMap;
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private String getBaseName(String bundle) {
        return StringUtils.isEmpty(bundle) ? GLOBAL_APPLICATION_RESOURCE_NAME
                : StringHelper.join(NamingConstant.BASE_PACKAGE, ".",
                bundle.replaceAll("-", "."), ".",
                APPLICATION_RESOURCE_NAME);
    }
}
