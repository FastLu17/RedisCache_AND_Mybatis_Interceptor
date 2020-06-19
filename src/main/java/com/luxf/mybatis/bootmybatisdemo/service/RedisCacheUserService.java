package com.luxf.mybatis.bootmybatisdemo.service;

import com.luxf.mybatis.bootmybatisdemo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 小66
 * @date 2020-06-13 20:03
 **/
@Service
public class RedisCacheUserService {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    // @CachePut(value = "redisCache", key = "'redis_user'+#user.id")
    @CachePut(value = "redisCache", key = "'redis_user'+#result.id")
    public User insertUser(User user) {
        userService.insertEntity(user);
        return user;
    }

    @Cacheable(value = "redisCache", key = "'redis_user'+#id")
    public User findById(Integer id) {
        return userService.findInfoById(id);
    }

    @Transactional
    public Object getRedisTemplateExecuteResult(String key, int idx) {

        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                Object before = operations.opsForValue().get(key);
                System.out.println("before = " + before);
                operations.watch(key);
                operations.multi();
                operations.opsForValue().increment(key, 1);
                Object after = operations.opsForValue().get(key);
                System.out.println("after = " + after);
                return operations.exec();
            }
        });
        return obj;
    }
}
