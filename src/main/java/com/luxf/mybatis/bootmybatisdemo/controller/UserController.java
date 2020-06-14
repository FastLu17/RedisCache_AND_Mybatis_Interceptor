package com.luxf.mybatis.bootmybatisdemo.controller;

import com.luxf.mybatis.bootmybatisdemo.entity.User;
import com.luxf.mybatis.bootmybatisdemo.helper.ApplicationContextHelper;
import com.luxf.mybatis.bootmybatisdemo.mapper.UserMapper;
import com.luxf.mybatis.bootmybatisdemo.service.UserBatchService;
import com.luxf.mybatis.bootmybatisdemo.service.UserService;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBatchService batchService;

    @RequestMapping("user/{id}")
    public String selectUserById(@PathVariable int id) {
        User user = userService.selectUserById(id);
        return user.toString();
    }

    @RequestMapping("/test")
    public void method() {
        /*SqlSessionFactory sqlSessionFactory = ApplicationContextHelper.getBean(SqlSessionFactory.class);
        UserMapper mapper = sqlSessionFactory.openSession().getMapper(UserMapper.class);
        System.out.println("mapper.getClass().getName() = " + mapper.getClass().getName());
        List<Map<String, Object>> columnContextList = MybatisHelper.selectColumnContextList("SELECT * FROM  USER LIMIT 0");
        System.out.println("columnContextList = " + columnContextList);*/
        Collection<ResultMap> resultMaps = ApplicationContextHelper.getBean(SqlSessionFactory.class).getConfiguration().getResultMaps();

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", 123);
        userMapper.updateUserById(map);
    }

    @RequestMapping("/bathUpdate")
    public void bathUpdate() {
        batchService.updateUserByIds(Arrays.asList(1, 2, 3));
    }

    @RequestMapping("/bathUpdateBySelf")
    public void bathUpdateBySelf() {
        userService.updateUserByIdsSelf(Arrays.asList(1, 2, 3));
    }

    @RequestMapping("/findInfoById")
    public void findInfoById() {
        userService.findInfoById(3);
    }
}
