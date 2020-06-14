package com.luxf.mybatis.bootmybatisdemo.controller;

import com.luxf.mybatis.bootmybatisdemo.entity.User;
import com.luxf.mybatis.bootmybatisdemo.service.RedisCacheUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 小66
 * @date 2020-06-13 19:56
 **/
@RestController
public class RedisController {
    @Autowired
    private RedisCacheUserService cacheUserService;

    @GetMapping("/insert")
    public void insert(){
        User user = new User();
        user.setId(5);
        user.setPassWord("123456");
        user.setUserName("xiao66");
        cacheUserService.insertUser(user);
    }

    @GetMapping("/findById")
    public void findById(){
        cacheUserService.findById(2);
    }
}
