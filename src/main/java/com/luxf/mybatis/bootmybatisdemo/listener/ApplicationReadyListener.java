package com.luxf.mybatis.bootmybatisdemo.listener;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

/**
 * 监听器、监听Application准备就绪、
 * <p>
 * 注意：这时候不要修改其内部状态，因为所有初始化步骤都已完成。
 *
 * @author 小66
 * @date 2020-06-19 9:35
 **/
@Component
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Environment environment;
    private final SqlSessionFactory sessionFactory;

    /**
     * 注入自定义泛型的RedisTemplate<String, Object>时,需要手动配置RedisTemplate的Bean对象、
     * see{@link com.luxf.mybatis.bootmybatisdemo.BootMybatisDemoApplication#redisTemplate(RedisConnectionFactory)}
     *
     * @param environment    配置
     * @param redisTemplate  redisTemplate模板
     * @param sessionFactory SqlSessionFactory
     */
    @Autowired
    public ApplicationReadyListener(Environment environment, RedisTemplate<String, Object> redisTemplate, SqlSessionFactory sessionFactory) {
        this.environment = environment;
        this.redisTemplate = redisTemplate;
        this.sessionFactory = sessionFactory;
    }

    /**
     * 如果实现类没有指定泛型, 则此处可以监听ApplicationEvent下的所有实现类,
     * 此处只需要监听ApplicationReadyEvent
     * <p>
     * 可以进行部分初始化设置
     *
     * @param event 事件、
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 初始化mybatis的配置、
        updateMybatisConfiguration();
        String[] activeProfiles = environment.getActiveProfiles();
        // 不是生产环境, 就删除所有的Redis缓存、
        boolean contains = Arrays.asList(activeProfiles).contains("pro");
        if (!contains) {
            Set<String> keys = redisTemplate.keys("*");
            // redisTemplate.delete(keys);
        }
    }

    /**
     * 主要作用是覆盖{@link Configuration}的6个属性("mappedStatements", "caches", "resultMaps", "parameterMaps", "keyGenerators", "sqlFragments")的类型、
     */
    private void updateMybatisConfiguration() {
        Configuration configuration = sessionFactory.getConfiguration();
//        for (String fieldName : new String[]{"mappedStatements", "caches", "resultMaps", "parameterMaps", "keyGenerators", "sqlFragments"}) {
//            // 获取字段值、
//            Map oldMap = (Map) ReflectionUtils.getFieldValue(configuration, fieldName);
//            // 这里的MybatisStrictMap 需要重写 org.apache.ibatis.session.Configuration.StrictMap的put()方法、使重复的Key可以进行更新！
//            Map newMap = new MybatisStrictMap(fieldName + " collection", oldMap);
//            // 更新 Configuration 的字段值
//            ReflectionUtils.setFieldValue(configuration, fieldName, newMap);
//        }
    }
}
