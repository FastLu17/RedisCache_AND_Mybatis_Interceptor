package com.luxf.mybatis.bootmybatisdemo.controller;

import com.luxf.mybatis.bootmybatisdemo.entity.StudentInfo;
import com.luxf.mybatis.bootmybatisdemo.service.GenericClassTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 小66
 * @date 2020-06-16 15:01
 **/
@RestController
public class GenericClassTypeController {
    @Autowired
    private GenericClassTypeService genericTypeService;

    @GetMapping("/getType")
    public void getGenericType(){
        Class<StudentInfo> type = genericTypeService.getType();
        System.out.println("type = " + type);
    }
}
