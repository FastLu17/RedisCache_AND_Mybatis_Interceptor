package com.luxf.mybatis.bootmybatisdemo.support;

import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

/**
 * @author 小66
 * @date 2020-06-13 23:36
 **/
@Component
public class RedisCachingSupport extends CachingConfigurerSupport {
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                catchRedisErrorException(exception, key);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                catchRedisErrorException(exception, key);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                catchRedisErrorException(exception, key);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                catchRedisErrorException(exception, "CLEAR");
            }
        };
    }

    private void catchRedisErrorException(Exception exception, Object key) {
        System.out.println("redis异常：key=[{" + key + "}], exception={" + exception.getMessage() + "}");
    }
}
