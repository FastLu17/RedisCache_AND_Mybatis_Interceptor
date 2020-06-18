package com.luxf.mybatis.bootmybatisdemo.config;

import com.luxf.mybatis.bootmybatisdemo.intercept.MyMethodInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 在spring boot下有两种方式设置AOP（实现织入weave）：
 * <p>
 * 1. 使用@Aspect注解、see{@link com.luxf.mybatis.bootmybatisdemo.aop.CacheAspect}
 * <p>
 * 2. 使用@Configuration注入{@link DefaultPointcutAdvisor}、只能添加一个pointcut和advice。
 * <p>
 * 实际使用中, @Aspect注解方式 更常见。 注意：无法通过AOP拦截<B>非 Spring Bean </B>对象的方法、
 * <p>
 * 注意：Spring Mvc Interceptor({@link HandlerInterceptor}) 和Spring Interceptor({@link MethodInterceptor})不同、
 *
 * @author 小66
 * @date 2020-06-18 11:31
 **/
@Configuration
public class MethodInterceptorConfig {
    @Bean
    public DefaultPointcutAdvisor defaultPointcutAdvisor() {
        DefaultPointcutAdvisor pointcutAdvisor = new DefaultPointcutAdvisor();
        // 设置自定义的拦截器、(通知)
        MethodInterceptor interceptor = new MyMethodInterceptor();
        pointcutAdvisor.setAdvice(interceptor);

        // expression表达式的切入点方式更通用、
        AspectJExpressionPointcut expressionPointcut = new AspectJExpressionPointcut();
        expressionPointcut.setExpression("@annotation(org.springframework.cache.annotation.CachePut) || @annotation(org.springframework.cache.annotation.Cacheable)");
        pointcutAdvisor.setPointcut(expressionPointcut);
        /*
        // 第一个参数、classAnnotationType：类上的注解、
        // 第二个参数、methodAnnotationType：方法上的注解、
        // 第三个参数、checkInherited：检查继承
        // TODO：不支持只有methodAnnotationType的切入点
        AnnotationMatchingPointcut annotationPointcut = new AnnotationMatchingPointcut(Service.class, Nullable.class, true);
        pointcutAdvisor.setPointcut(annotationPointcut);*/
        return pointcutAdvisor;
    }
}
