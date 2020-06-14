package com.luxf.mybatis.bootmybatisdemo.service;

import com.luxf.mybatis.bootmybatisdemo.entity.User;
import com.luxf.mybatis.bootmybatisdemo.helper.ApplicationContextHelper;
import com.luxf.mybatis.bootmybatisdemo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:小66
 * @Date: 2018/9/26 0026
 * @Time: 15:23
 */
@Service
public class UserService implements BaseService<User, Integer> {
    @Autowired
    private UserMapper userMapper;
//
//    @Autowired
//    private CacheOperationSource operationSource;

    public User selectUserById(int id) {
        Map<String, Object> map = new HashMap<>(1);
        map.put("id", id);
        map.put("tableName", "user");
        Map<String, Object> resultMap = userMapper.selectMapById(map);
        //返回值的key全是小写、无法映射为user的属性、因为返回值不是User、
        System.out.println("resultMap = " + resultMap);
        return userMapper.selectUserById(map, id);
    }

    @Transactional(propagation = Propagation.NESTED, rollbackFor = Exception.class)
    public void updateUserById(int id) {
        Map<String, Object> map = new HashMap<>(1);
        map.put("id", id);
        map.put("realName", "realName：" + id);
        userMapper.updateUserById(map);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateUserByIds(List<Integer> ids) {
        ids.forEach(id -> {
            Map<String, Object> map = new HashMap<>(1);
            map.put("id", id);
            map.put("realName", "realName：" + id);
            userMapper.updateUserById(map);
            if (id % 2 == 0) {
                throw new RuntimeException("id不能是偶数");
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateUserByIdsSelf(List<Integer> ids) {
        try {
            updateUserByIds(ids);
            // 当同一个类的方法之间事务方法发生自我调用,被调用的事务方法的特性将失效、(相当于没有@Transactional注解,不会回滚)
        } catch (Exception e) {
            System.out.println("e.getMessage() = " + e.getMessage());
        }

        Map<String, Object> map = new HashMap<>(1);
        map.put("id", "4");
        map.put("realName", "mainTransactional");
        userMapper.updateUserById(map);
        // throw new RuntimeException("mainTransactional");
    }

    @Override
    @Cacheable(value = "USER_INFO")
    public User findInfoById(Integer id) {
        CacheOperationSource source = ApplicationContextHelper.getBean(CacheOperationSource.class);
        try {
            Collection<CacheOperation> operations = source.getCacheOperations(this.getClass().getMethod("findInfoById", Integer.class), this.getClass());
            System.out.println("operations.size() = " + operations.size());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return userMapper.selectUserById(new HashMap<>(), id);
    }
}
