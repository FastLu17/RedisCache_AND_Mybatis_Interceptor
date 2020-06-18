package com.luxf.mybatis.bootmybatisdemo.support;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.AbstractCachingConfiguration;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.cache.interceptor.*;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * see{@link AbstractCachingConfiguration#setConfigurers(Collection)}、
 * 此处会注入自定义的CachingConfigurerSupport, 为 cacheManager、cacheResolver、keyGenerator、errorHandler 初始化值、
 * <p>
 * see{@link ProxyCachingConfiguration#cacheInterceptor()} 注入{@link CacheInterceptor}拦截器时,
 * 使用到{@link CachingConfigurerSupport}的4个属性初始化{@link CacheAspectSupport}的属性、
 * <p>
 * 在Application启动后,{@link AbstractCacheManager}就会加载Cache(多个实现,这里是RedisCacheManager)、
 * see{@link AbstractCacheManager#afterPropertiesSet()} 生命周期方法、
 * see{@link RedisCacheManager#loadCaches()}
 * Spring Cache 通过拦截器CacheInterceptor来实现缓存拦截、
 * {@link CacheInterceptor#invoke(MethodInvocation)}方法中执行父类{@link CacheAspectSupport#execute(CacheOperationInvoker, Method, CacheAspectSupport.CacheOperationContexts)}方法、
 * <p>
 * 该方法内部：处理@CachePut、@Cacheable、@CacheEvict
 * 1、processCacheEvicts(contexts.get(CacheEvictOperation.class), true, CacheOperationExpressionEvaluator.NO_RESULT); // true与 @CacheEvict注解的isBeforeInvocation属性比较、
 * 2、findCachedItem(contexts.get(CacheableOperation.class));
 * 3、collectPutRequests(contexts.get(CachePutOperation.class), cacheValue, cachePutRequests);
 * 4、processCacheEvicts(contexts.get(CacheEvictOperation.class), false, cacheValue);
 * <p>
 * 这4个方法内部主要调用 CacheAspectSupport父类{@link AbstractCacheInvoker}的 doGet()、doClear()、doPut()、doEvict() 4个方法, 完成Redis Cache的存储、
 *
 * @author 小66
 * @date 2020-06-13 23:36
 **/
@Component
public class RedisCachingSupport extends CachingConfigurerSupport {
    /**
     * 使用配置文件注入的Bean、会有此警告
     */
    private final AbstractCacheManager abstractCacheManager;
    private final RedisCacheManager redisCacheManager;

    @Autowired
    public RedisCachingSupport(AbstractCacheManager abstractCacheManager, RedisCacheManager redisCacheManager) {
        this.abstractCacheManager = abstractCacheManager;
        this.redisCacheManager = redisCacheManager;
    }

    /**
     * SpringBoot会自动装配 cacheManager、无需实现cacheManager()方法, 指定注入的RedisCacheManager、
     *
     * @return CacheManager
     */
    @Override
    public CacheManager cacheManager() {
        return redisCacheManager;
    }

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

    @PostConstruct
    public void postConstruct() {
        Collection<String> cacheNames = abstractCacheManager.getCacheNames();
        Collection<String> redisCacheManagerCacheNames = redisCacheManager.getCacheNames();
        System.out.println("cacheNames = " + cacheNames);
    }
}
