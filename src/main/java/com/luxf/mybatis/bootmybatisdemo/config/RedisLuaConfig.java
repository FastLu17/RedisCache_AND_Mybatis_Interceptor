package com.luxf.mybatis.bootmybatisdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * 配置Bean、只初始化一次, 不需要每次都去创建对象,解析lua脚本、
 * 使用配置的Bean对象, <B>多线程的情况下</B> 不会抛出Connection Time Out, Connection Reset 等异常的错误
 *
 * @author 小66
 */
@Configuration
public class RedisLuaConfig {

    /**
     * 如果lua脚本有返回值, 则必须设置ResultType、否则报错：java.lang.Long/String cannot be cast to [B
     *
     * @return DefaultRedisScript
     */
    @Bean("redisScript")
    public RedisScript<Long> obtainCouponScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("redis.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }
}