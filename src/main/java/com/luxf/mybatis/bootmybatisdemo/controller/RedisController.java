package com.luxf.mybatis.bootmybatisdemo.controller;

import com.luxf.mybatis.bootmybatisdemo.entity.User;
import com.luxf.mybatis.bootmybatisdemo.service.RedisCacheUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author 小66
 * @date 2020-06-13 19:56
 **/
@RestController
public class RedisController {
    private final RedisCacheUserService cacheUserService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisController(RedisCacheUserService cacheUserService, RedisTemplate<String, Object> redisTemplate) {
        this.cacheUserService = cacheUserService;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/insert")
    public void insert() {
        User user = new User();
        user.setId(5);
        user.setPassWord("123456");
        user.setUserName("xiao66");
        cacheUserService.insertUser(user);
    }

    @GetMapping("/findById")
    public void findById() {
        cacheUserService.findById(2);
    }

    @GetMapping("/testRedisTemplate")
    public void testRedisTemplate() throws Exception {
//        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
//        String key = "key";
//        opsForValue.set(key, "initValue");
//        redisTemplate.execute()
        ExecutorService pool = Executors.newCachedThreadPool();
        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            tasks.add(() -> cacheUserService.getRedisTemplateExecuteResult("key", idx));
        }
        List<Future<Object>> futures = pool.invokeAll(tasks);
        for (Future<Object> f : futures) {
            Object result = f.get();
            System.out.println("result = " + result);
        }
        pool.shutdown();
        pool.awaitTermination(1000, TimeUnit.MILLISECONDS);
    }

    @GetMapping("/testLuaScript")
    public void testLuaScript() throws Exception {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // 如果lua脚本有返回值, 则必须设置ResultType、否则报错：java.lang.Long cannot be cast to [B
        script.setResultType(Long.class);
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis.lua")));
        ArrayList<String> keys = new ArrayList<>();
        keys.add("AccessCount");

        ExecutorService pool = Executors.newCachedThreadPool();
        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tasks.add(() -> {
                 Long execute = redisTemplate.execute(script, keys, 20);
                System.out.println("execute = " + execute);
                return execute;
            });
        }
        List<Future<Object>> futures = pool.invokeAll(tasks);
        for (Future<Object> f : futures) {
            Object result = f.get();
            System.out.println("result = " + result);
        }
        pool.shutdown();
        pool.awaitTermination(1000, TimeUnit.MILLISECONDS);
    }
}
