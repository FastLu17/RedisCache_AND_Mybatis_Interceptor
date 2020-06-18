package com.luxf.mybatis.bootmybatisdemo.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author 小66
 * @date 2020-06-18 20:52
 **/
public class PersistenceHelper {

    public static <T> T mapToEntity(Map<String, Object> entityMap, Class<T> clazz) {
        return JSONObject.parseObject(JSON.toJSONString(entityMap), clazz);
    }

    public static <T> List<T> mapListToEntity(List<Map<String, Object>> entityMap, Class<T> clazz) {
        return JSONObject.parseArray(JSON.toJSONString(entityMap), clazz);
    }
}
