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
