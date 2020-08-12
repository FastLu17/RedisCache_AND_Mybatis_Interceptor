package com.luxf.mybatis.bootmybatisdemo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.cache.interceptor.AbstractFallbackCacheOperationSource;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.reflect.Method;

/**
 * @author 小66
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class BootMybatisDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootMybatisDemoApplication.class, args);
    }

    /**
     * 该接口下的实现类有个"attributeCache"属性{@link AbstractFallbackCacheOperationSource#attributeCache}、用于缓存 CacheOperation--> @Cacheable,@CachePut等具体属于、
     * 如果再已经加载完后还想重新加载、则需要利用反射 获取此Map、重新push。
     * 利用反射时,需要先反射调用 {@link AbstractFallbackCacheOperationSource#computeCacheOperations(Method method, Class targetClass)}方法、
     * 获取最新的Collection<CacheOperation>, 然后push。
     * getCacheOperations()的源码中, 已存在的Key直接返回,没有进行覆盖、因此需要反射处理
     * 也可以主动优先加载需要动态生成的@Cacheable,@CachePut等注解、
     * <p>
     * 不需要手动注入该Bean、Spring底层已经配置过、see{@link ProxyCachingConfiguration#cacheOperationSource()}
     *
     * @return
     */
    @Bean
    @Lazy
    public CacheOperationSource cacheOperationSource() {
        return new AnnotationCacheOperationSource();
    }

    /**
     * 自定义RedisTemplate, 指定明确的泛型类型、
     *
     * @param connectionFactory 链接工厂
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Autowired RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 必须设置RedisConnectionFactory、
        redisTemplate.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        // 解决jackson2无法反序列化LocalDateTime的问题
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        // 解决com.fasterxml.jackson.databind.exc.MismatchedInputException: Expected array or string.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // 设置Key的序列化、
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);

        // 设置值的序列化、
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        /*
         * 在Spring的 RedisTemplate 类 redisTemplate.setEnableTransactionSupport(true); 中启用 Redis 事务时得到一个惨痛的教训：
         *
         * 1、Redis 会在运行几天后开始返回垃圾数据，导致数据严重损坏。StackOverflow上也报道了类似情况。
         * 在运行一个 monitor 命令后，我的团队发现，在进行 Redis 操作或 RedisCallback 后，Spring 并没有自动关闭 Redis 连接，而事实上它是应该关闭的。
         * 如果再次使用未关闭的连接，可能会从意想不到的 Redis 密钥返回垃圾数据。有意思的是，如果在 RedisTemplate 中把事务支持设为 false, 这一问题就不会出现了。
         *
         * 可以先在 Spring 语境里配置一个 PlatformTransactionManager（例如 DataSourceTransactionManager），然后再用 @Transactional 注释来声明 Redis 事务的范围，让 Spring 自动关闭 Redis 连接。
         * 因此：一定要使用spring提供的注解式事务时，建议初始化两个RedisTemplate Bean，分别设置enableTransactionSupport属性为true和false。
         * 针对需要事务和不需要事务的操作使用不同的template。
         * */

        // 开启Redis事务, 这个应该是支持 '数据库的事务成功才执行' 的意思
        // TODO: 如果开启了事务支持, 需要在方法上配置@Transactional、否则Redis连接不会正常关闭、
        // redisTemplate.setEnableTransactionSupport(true);
        return redisTemplate;
    }
}
