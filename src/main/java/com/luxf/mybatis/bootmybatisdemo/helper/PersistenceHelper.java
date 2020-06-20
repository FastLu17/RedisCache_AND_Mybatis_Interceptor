package com.luxf.mybatis.bootmybatisdemo.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.luxf.mybatis.bootmybatisdemo.entity.BaseInfo;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 小66
 * @date 2020-06-18 20:52
 **/
public class PersistenceHelper {

    public static <T> T mapToEntity(Map<String, Object> entityMap, Class<T> persistableClass) {
        return JSONObject.parseObject(JSON.toJSONString(entityMap), persistableClass);
    }

    public static <T> List<T> mapListToEntity(List<Map<String, Object>> entityMap, Class<T> persistableClass) {
        return JSONObject.parseArray(JSON.toJSONString(entityMap), persistableClass);
    }

    /**
     * 将查询的数据结果Map中的数据赋值到指定的Bean中，如果没有对应的成员变量，赋值到Bean的extMap成员变量中
     */
    public static <T extends BaseInfo> T mapToPersistable(Map<String, Object> map, Class<T> persistableClass) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        T instance = getInstance(persistableClass);

        // 利用单例对象(枚举)的ConcurrentMap属性, 缓存一下实体类所有字段对应的List<PropertyDescriptor>, 先查Map, 没有就初始化、
        // BeanPropertyHelper.getBeanPropertyDescriptors(persistableClass);
        // List<PropertyDescriptor> propertyDescriptorList = BeanPropertyHelper.getBeanPropertyDescriptors(persistableClass);
        List<PropertyDescriptor> propertyDescriptorList = new ArrayList<>();

        map.forEach((k, v) -> {
            PropertyDescriptor propertyDescriptor = propertyDescriptorList.stream().filter(p -> p.getName().equalsIgnoreCase(k)).findFirst().orElse(null);

            // ConvertUtils.convert(v, targetClazz); --> 将数据库类型的字段转换Java类型的转换器
            if (propertyDescriptor == null) {
                // instance.setExtMapItem(k.toLowerCase(), ConvertUtils.convert(v, String.class));
                instance.setExtMapItem(k.toLowerCase(), v.toString());
            } else {
                // invokeWriteMethod(propertyDescriptor.getWriteMethod(), instance, ConvertUtils.convert(v, propertyDescriptor.getPropertyType()));
                invokeWriteMethod(propertyDescriptor.getWriteMethod(), instance, v);
            }
        });
        return instance;
    }

    private static <T extends BaseInfo> T getInstance(Class<T> persistableClass) {
        try {
            return persistableClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void invokeWriteMethod(Method method, Object target, Object... args) {
        final boolean accessible = method.isAccessible();

        try {
            if (!accessible) {
                method.setAccessible(true);
            }
            method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) {
                method.setAccessible(false);
            }
        }
    }
}
