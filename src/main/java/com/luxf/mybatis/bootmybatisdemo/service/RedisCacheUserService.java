package com.luxf.mybatis.bootmybatisdemo.service;

import com.luxf.mybatis.bootmybatisdemo.entity.User;
import com.luxf.mybatis.bootmybatisdemo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

/**
 * @author 小66
 * @date 2020-06-13 20:03
 **/
@Service
public class RedisCacheUserService {

    @Autowired
    private UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    // @CachePut(value = "redisCache", key = "'redis_user'+#user.id")
    @CachePut(value = "redisCache", key = "'redis_user'+#result.id")
    public User insertUser(User user) {
        userMapper.insertUser(user);
        return user;
    }

    @Cacheable(value = "redisCache", key = "'redis_user'+#id")
    public User findById(Integer id) {
        return userMapper.selectUserById(new HashMap<>(), id);
    }
}
