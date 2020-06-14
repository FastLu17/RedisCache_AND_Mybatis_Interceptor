package com.luxf.mybatis.bootmybatisdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableCaching
public class BootMybatisDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootMybatisDemoApplication.class, args);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     *  该接口下的实现类有个"attributeCache"属性、用于缓存 CacheOperation--> @Cacheable,@CachePut等具体属于、
     *  private final Map<Object, Collection<CacheOperation>> attributeCache = new ConcurrentHashMap<>(1024);
     *  如果再已经加载完后还想重新加载、则需要利用反射 获取此Map、重新PUSH。
     *  getCacheOperations()的源码中, 已存在的Key直接返回,没有进行覆盖、因此需要反射处理
     * @return
     */
    @Bean
    @Lazy
    public CacheOperationSource cacheOperationSource() {
        return new AnnotationCacheOperationSource();
    }

    @PostConstruct
    public void init() {
        RedisSerializer stringSerializer = redisTemplate.getStringSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
    }
}
