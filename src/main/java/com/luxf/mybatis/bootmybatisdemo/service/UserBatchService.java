package com.luxf.mybatis.bootmybatisdemo.service;

import com.luxf.mybatis.bootmybatisdemo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: 小66
 * @Date: 2018/9/26 0026
 * @Time: 15:23
 */
@Service
public class UserBatchService {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateUserByIds(List<Integer> ids) {
        // updateUserByIds()的传播行为是REQUIRES_NEW、则主事务(主方法)抛出异常,REQUIRES_NEW的事务(子方法)不会回滚、如果主方法没有抛异常,但是子方法抛异常,子方法会回滚、 如果子方法抛出异常被try.catch、则主方法不会回滚
        // updateUserByIds()的传播行为是NESTED、则主事务(主方法)抛出异常,NESTED的事务(子方法)会回滚、如果主方法没有抛异常,但是子方法抛异常,子方法会回滚、 如果子方法抛出异常被try.catch、则主方法不会回滚
        // updateUserByIds()的传播行为是REQUIRED、则主事务(主方法)抛出异常,REQUIRED的事务(子方法) 会回滚、如果主方法没有抛异常,但是子方法抛异常,子方法会回滚、如果子方法抛出异常被try.catch、 则主方法会回滚
        // userService.updateUserByIds(ids); // 如果子方法没有被try.catch,子方法抛出异常时,不论子方法是哪种传播行为、主方法都会回滚、

        try {
            userService.updateUserByIds(ids);
        } catch (Exception e) {
            System.out.println("e.getMessage() = " + e.getMessage());
        }
        Map<String, Object> map = new HashMap<>(1);
        map.put("id", "4");
        map.put("realName", "mainTransactional");
        userMapper.updateUserById(map);
        // throw new RuntimeException("mainTransactional");
    }
}
