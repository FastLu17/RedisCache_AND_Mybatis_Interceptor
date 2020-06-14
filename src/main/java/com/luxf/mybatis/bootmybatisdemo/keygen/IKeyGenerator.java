package com.luxf.mybatis.bootmybatisdemo.keygen;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author 小66
 * @date 2020-06-13 23:10
 **/
@Component(value = "iKeyGenerator")
public class IKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params.length > 0) {
            return params[0];
        } else {
            return UUID.randomUUID().toString();
        }
    }
}
