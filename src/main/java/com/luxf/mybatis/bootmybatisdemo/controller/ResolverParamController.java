package com.luxf.mybatis.bootmybatisdemo.controller;

import com.luxf.mybatis.bootmybatisdemo.resolver.RestParameterBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author 小66
 * @date 2020-06-17 9:17
 **/
@RestController
public class ResolverParamController {

    @PostMapping("/resolver")
    public void resolver(RestParameterBean parameterBean) {
        String jsonString = parameterBean.getJsonString();
        Map<String, Object> paramMap = parameterBean.getParamMap();
        System.out.println("jsonString = " + jsonString);
    }
}
