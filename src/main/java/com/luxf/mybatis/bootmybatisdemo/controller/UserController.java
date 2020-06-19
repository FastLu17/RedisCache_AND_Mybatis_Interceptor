package com.luxf.mybatis.bootmybatisdemo.controller;

import com.alibaba.fastjson.JSONObject;
import com.luxf.mybatis.bootmybatisdemo.entity.SecurityUser;
import com.luxf.mybatis.bootmybatisdemo.entity.User;
import com.luxf.mybatis.bootmybatisdemo.helper.ApplicationContextHelper;
import com.luxf.mybatis.bootmybatisdemo.mapper.UserMapper;
import com.luxf.mybatis.bootmybatisdemo.security.UserRoleService;
import com.luxf.mybatis.bootmybatisdemo.service.UserBatchService;
import com.luxf.mybatis.bootmybatisdemo.service.UserService;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBatchService batchService;

    @Autowired
    private UserRoleService userRoleService;

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
        User infoById = userService.findInfoById(3);
        System.out.println("infoById = " + infoById);
    }

    @RequestMapping("/findSecurityInfoById")
    public void findSecurityInfoById() {
        SecurityUser infoById = userRoleService.findInfoById(1);
        System.out.println("infoById = " + infoById);
    }

    @RequestMapping("/findAll")
    public void findAll() {
        List<User> userList = userService.findAll();
        System.out.println("userList = " + userList);
    }

    @RequestMapping("/insertUser")
    public void insertUser() {
        User info = new User();
        info.setUserName("HUAWEI");
        info.setPassWord("123456");
        User user = userService.insertEntity(info);
        System.out.println("user = " + user);
    }

    @GetMapping("/findInfoListByCond")
    public String findInfoListByCond() {
        Map<String, Object> cond = new HashMap<>(2);
        cond.put("passWord", "123456");
        cond.put("realName", null);
        List<User> entityListByCond = userService.findEntityListByCond(cond, "userName", "ID");
        return JSONObject.toJSONString(entityListByCond);
    }
}
