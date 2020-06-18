package com.luxf.mybatis.bootmybatisdemo.intercept;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 需要根据指定的拦截规则,拦截到具体的方法、
 * <p>
 * see{@link com.luxf.mybatis.bootmybatisdemo.config.MethodInterceptorConfig}
 *
 * @author 小66
 * @date 2020-06-18 11:23
 **/
public class MyMethodInterceptor implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        System.out.println("methodName = " + methodInvocation.getMethod().getName());
        return methodInvocation.proceed();
    }
}
