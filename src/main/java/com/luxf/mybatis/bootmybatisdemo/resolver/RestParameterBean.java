package com.luxf.mybatis.bootmybatisdemo.resolver;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @author 小66
 * @date 2020-06-17 9:12
 **/
public class RestParameterBean {
    static final RestParameterBean EMPTY = new RestParameterBean(new JSONObject(), "");
    private Map<String, Object> paramMap;
    private String jsonString;

    public RestParameterBean(Map<String, Object> paramMap, String jsonString) {
        this.paramMap = paramMap;
        this.jsonString = jsonString;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }


    public String getJsonString() {
        return jsonString;
    }

}
