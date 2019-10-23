package com.ibyte.framework.config;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.util.StringHelper;
import com.ibyte.common.util.TenantUtil;
import com.ibyte.common.util.thread.ThreadLocalUtil;
import com.ibyte.framework.support.persistent.ApplicationConfigRemoteApi;
import com.ibyte.framework.support.persistent.PersistentConstant;
import com.ibyte.framework.support.persistent.dto.ApplicationConfigVO;
import com.ibyte.framework.support.util.SerializeUtil;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: <配置服务持久化处理>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-23
 */
@Component
public class ApplicationConfigApi {

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private ApplicationConfigRemoteApi applicationConfigRemoteApi;


    /**
     * 读取配置，未配置时返回null
     */
    public <T> T get(Class<T> clazz) {
        return get(clazz, TenantUtil.getTenantId());
    }

    /**
     * 读取配置，未配置时返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz, int tenantId) {
        String id = StringHelper.join(clazz.getName(),
                PersistentConstant.PATH_SPLIT, tenantId);
        String key = cacheKey(id);
        // 先从本地缓存取
        Object value = ThreadLocalUtil.getLocalVar(key);
        if (value != null) {
            return (T) value;
        }
        // 从redis获取
        String text = (String) redisson.getBucket(key, StringCodec.INSTANCE)
                .get();
        if (text == null) {
            // 调用远程接口获取
            text = applicationConfigRemoteApi.get(id);
        }
        if (text == null || PersistentConstant.JSON_EMPTY.equals(text)) {
            return null;
        }
        // 反序列化
        value = SerializeUtil.parseObject(text, clazz);
        ThreadLocalUtil.setLocalVar(key, value);
        return (T) value;
    }

    /**
     * 保存配置
     */
    public void save(Object config) {
        save(config, TenantUtil.getTenantId());
    }

    /**
     * 保存配置
     */
    public void save(Object config, int tenantId) {
        String id = StringHelper.join(config.getClass().getName(),
                PersistentConstant.PATH_SPLIT, tenantId);
        ApplicationConfigVO vo = new ApplicationConfigVO();
        vo.setFdId(id);
        vo.setFdTenantId(tenantId);
        vo.setFdContent(SerializeUtil.toString(config));

        applicationConfigRemoteApi.save(vo);
        ThreadLocalUtil.removeLocalVar(cacheKey(id));
    }

    private String cacheKey(String id) {
        return StringHelper.join(PersistentConstant.CONFIG_PREFIX,
                NamingConstant.shortName(id));
    }
}
