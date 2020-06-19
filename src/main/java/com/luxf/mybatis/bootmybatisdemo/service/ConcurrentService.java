package com.luxf.mybatis.bootmybatisdemo.service;

import com.luxf.mybatis.bootmybatisdemo.entity.ConcurrentInfo;
import com.luxf.mybatis.bootmybatisdemo.entity.User;
import com.luxf.mybatis.bootmybatisdemo.mapper.ConcurrentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * @author 小66
 * @date 2020-06-19 19:18
 **/
@Service
public class ConcurrentService extends AbstractDaoImpl<ConcurrentInfo, String> {
    @Autowired
    private ConcurrentMapper concurrentMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisScript<Long> redisScript;

    public ConcurrentInfo findByCond(Integer value) {
        return concurrentMapper.findByCond(value);
    }

    /**
     *  实际开发中, 不允许在Service中定义非final变量
     */
    private int counter = 0;

    @Transactional(rollbackFor = Exception.class)
    @Async
    public ConcurrentInfo runWithDataBase() {
        ConcurrentInfo byCond = findByCond(1);
        Integer stock = byCond.getStock();
        counter++;
        System.out.println("counter = " + counter);
        System.out.println("stock = " + stock);
        if (stock <= 0) {
            return null;
        }
        int decrement = stock - 1;
        concurrentMapper.updateByCond(decrement,byCond.getId());
        User user = new User();
        user.setPassWord(String.valueOf(decrement));
        userService.insertEntity(user);
        return null;
    }

    /**
     * 使用Lua脚本、不会出现超发, 避免了高并发带来的问题
     */
    @Async
    public void runWithLua() {
//        使用配置的Bean对象,不需要每次创建对象,解析lua脚本。--> 不会抛出Connection Time Out, Connection Reset 等异常的错误

//        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
//        // 如果lua脚本有返回值, 则必须设置ResultType、否则报错：java.lang.Long cannot be cast to [B
//        script.setResultType(Long.class);
//        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis.lua")));

        ArrayList<String> keys = new ArrayList<>();
        keys.add("STOCK");
        keys.add("COUNT");
        Long execute = redisTemplate.execute(redisScript, keys, 1);
        if ("null".equals(String.valueOf(execute)) || "0".equals(String.valueOf(execute))) {
            return;
        }
        System.out.println("execute = " + execute);
    }
}
