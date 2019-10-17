package com.ibyte.component.redis;

import com.ibyte.common.util.IDGenerator;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: <Redisson客户端配置>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 22:18
 */
@Configuration
@ConditionalOnClass({Redisson.class, RedisOperations.class})
@AutoConfigureBefore(org.redisson.spring.starter.RedissonAutoConfiguration.class)
@EnableConfigurationProperties({RedissonProperties.class, RedisProperties.class})
public class RedissonAutoConfiguration {
    /**
     * redis配置
     */
    @Resource
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private RedisProperties redisProperties;

    /**
     * ression配置
     */
    @Resource
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private RedissonProperties redissonProperties;

    /**
     * 上下文
     */
    @Resource
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private ApplicationContext ctx;

    /**
     * 默认的redisson配置
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(Config.class)
    public Config defaultConfig() {
        Config config = null;
        Method clusterMethod = ReflectionUtils.findMethod(RedisProperties.class, "getCluster");
        Method timeoutMethod = ReflectionUtils.findMethod(RedisProperties.class, "getTimeout");
        Object timeoutValue = ReflectionUtils.invokeMethod(timeoutMethod, redisProperties);
        int timeout;
        if (null == timeoutValue) {
            timeout = 0;
        } else if (!(timeoutValue instanceof Integer)) {
            Method millisMethod = ReflectionUtils.findMethod(timeoutValue.getClass(), "toMillis");
            timeout = ((Long) ReflectionUtils.invokeMethod(millisMethod, timeoutValue)).intValue();
        } else {
            timeout = (Integer) timeoutValue;
        }

        if (redissonProperties.getConfig() != null) {
            try {
                InputStream is = getConfigStream();
                config = Config.fromJSON(is);
            } catch (IOException e) {
                // trying next format
                try {
                    InputStream is = getConfigStream();
                    config = Config.fromYAML(is);
                } catch (IOException e1) {
                    throw new IllegalArgumentException("Can't parse config", e1);
                }
            }
        } else if (redisProperties.getSentinel() != null) {
            //哨兵
            Method nodesMethod = ReflectionUtils.findMethod(RedisProperties.Sentinel.class, "getNodes");
            Object nodesValue = ReflectionUtils.invokeMethod(nodesMethod, redisProperties.getSentinel());
            String[] nodes;
            if (nodesValue instanceof String) {
                nodes = convert(Arrays.asList(((String) nodesValue).split(",")));
            } else {
                nodes = convert((List<String>) nodesValue);
            }
            config = new Config();
            config.useSentinelServers()
                    .setMasterName(redisProperties.getSentinel().getMaster())
                    .addSentinelAddress(nodes)
                    .setDatabase(redisProperties.getDatabase())
                    .setConnectTimeout(timeout)
                    .setPassword(redisProperties.getPassword());
        } else if (clusterMethod != null && ReflectionUtils.invokeMethod(clusterMethod, redisProperties) != null) {
            //集群
            Object clusterObject = ReflectionUtils.invokeMethod(clusterMethod, redisProperties);
            Method nodesMethod = ReflectionUtils.findMethod(clusterObject.getClass(), "getNodes");
            List<String> nodesObject = (List) ReflectionUtils.invokeMethod(nodesMethod, clusterObject);
            String[] nodes = convert(nodesObject);
            config = new Config();
            config.useClusterServers()
                    .addNodeAddress(nodes)
                    .setConnectTimeout(timeout)
                    .setPassword(redisProperties.getPassword());
        } else {
            //单机
            config = new Config();
            Method urlMethod = ReflectionUtils.findMethod(RedisProperties.class, "getUrl");
            String url = (String) ReflectionUtils.invokeMethod(urlMethod, redisProperties);
            if (StringUtils.isEmpty(url)) {
                String prefix = "redis://";
                Method method = ReflectionUtils.findMethod(RedisProperties.class, "isSsl");
                if (method != null && (Boolean) ReflectionUtils.invokeMethod(method, redisProperties)) {
                    prefix = "rediss://";
                }
                url = prefix + redisProperties.getHost() + ":" + redisProperties.getPort();
            }
            config.useSingleServer()
                    .setAddress(url)
                    .setConnectTimeout(timeout)
                    .setDatabase(redisProperties.getDatabase())
                    .setPassword(redisProperties.getPassword());
        }
        config.setCodec(JsonJacksonCodec.INSTANCE);
        return config;
    }

    /**
     * 转换处理
     *
     * @param nodesObject
     * @return
     */
    private String[] convert(List<String> nodesObject) {
        List<String> nodes = new ArrayList<String>(nodesObject.size());
        for (String node : nodesObject) {
            if (!node.startsWith("redis://") && !node.startsWith("rediss://")) {
                nodes.add("redis://" + node);
            } else {
                nodes.add(node);
            }
        }
        return nodes.toArray(new String[nodes.size()]);
    }

    /**
     * 自定义redission配置
     *
     * @return
     * @throws IOException
     */
    private InputStream getConfigStream() throws IOException {
        org.springframework.core.io.Resource resource = ctx.getResource(redissonProperties.getConfig());
        InputStream is = resource.getInputStream();
        return is;
    }

    /**
     * redisson客户端
     *
     * @param config
     * @return
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(Config config) {
        RedissonClient client = Redisson.create(config);
        IDGenerator.setJvmId(client.getAtomicLong(IDGenerator.class.getName())
                .incrementAndGet());
        return client;
    }
}
