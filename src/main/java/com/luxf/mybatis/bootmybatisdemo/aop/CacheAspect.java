package com.luxf.mybatis.bootmybatisdemo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;

/**
 * @author 小66
 * @date 2020-06-13 20:37
 **/
@Component
@Aspect
public class CacheAspect {
    // 无法通过AOP拦截 非Spring Bean 对象的方法、
    @Pointcut("execution(public * org.springframework.cache.annotation.SpringCacheAnnotationParser.parseCacheAnnotations(..))")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Exception {
        //可以获取注解的相关信息、对注解进行相应的操作
        Signature signature = pjp.getSignature();
        //public java.lang.String com.luxf.aop.demo.controller.AopController.methodName(java.lang.String)
        String longString = signature.toLongString();
        String className = longString.split(" ")[1];
        Class<?> clazz = Class.forName(className);
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            String tableName = table.name();

            // 动态修改注解参数、 改了不生效、容器初始化完成后,spring就已经把Cacheable注解的属性提前解析、
            Cacheable cacheable = Cacheable.class.newInstance();
            InvocationHandler handler = Proxy.getInvocationHandler(cacheable);
            Field memberValues = handler.getClass().getDeclaredField("memberValues");
            memberValues.setAccessible(true);
            Map<String, Object> map = (Map<String, Object>) memberValues.get(handler);
            map.put("value", tableName);
            //AOP拦到的方法参数的值
            String UID = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
            Object[] args = pjp.getArgs();
            map.put("key", "REDIS_" + tableName + "_" + args[0]);
        }
        Object obj;
        try {
            // 执行被AOP拦截的方法、 修改了参数,必须将args传进去、否则还是以前的参数
            // obj = pjp.proceed(args);
            obj = pjp.proceed();
        } catch (Throwable throwable) {
            obj = throwable.toString();
        }
        return obj;
    }
}
