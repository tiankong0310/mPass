package com.ibyte.component.jpa;

import com.ibyte.framework.support.ApplicationContextHolder;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SnappyCodec;
import org.redisson.config.Config;
import org.redisson.hibernate.RedissonRegionFactory;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * @Description: <Hibernate 二级缓存>
 *
 * @author li.Shangzhi
 * @Date: 2019年10月28日 23:57:13
 */
public class RedissonCacheRegionFactory extends RedissonRegionFactory {

    /**
     * 上下文
     */
    private ApplicationContext context;
    /**
     * redisson配置
     */
    private Config defaultConfig;

    /**
     * 准备阶段
     */
    @Override
    protected void prepareForUse(SessionFactoryOptions settings, Map properties) throws CacheException {
        this.context = ApplicationContextHolder.getApplicationContext();
        this.defaultConfig = context.getBean(Config.class);
        super.prepareForUse(settings, properties);
    }

    /**
     * 创建redission客户端
     *
     * @param properties
     * @return
     */
    @Override
    protected RedissonClient createRedissonClient(Map properties) {
        Config customConfig = new Config(defaultConfig);
        customConfig.setCodec(new SnappyCodec());
        return Redisson.create(customConfig);
    }
}
