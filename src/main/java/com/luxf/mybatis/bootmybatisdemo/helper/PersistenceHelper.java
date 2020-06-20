package com.luxf.mybatis.bootmybatisdemo.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.luxf.mybatis.bootmybatisdemo.entity.BaseInfo;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 小66
 * @date 2020-06-18 20:52
 **/
public class PersistenceHelper {

    private static final String TIME_PACKAGE = "java.time";

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

        List<PropertyDescriptor> propertyDescriptorList = getBeanPropertyDescriptors(persistableClass);
        map.forEach((k, v) -> {
            PropertyDescriptor descriptor = propertyDescriptorList.stream().filter(p -> p.getName().equalsIgnoreCase(k)).findFirst().orElse(null);
            // ConvertUtils.convert(v, targetClazz); --> 将数据库类型的字段转换Java类型的转换器, ConvertUtils可以处理大部分的数据类型、
            // 注意：org.apache.commons.beanutils.ConvertUtils 不能将数据库的时间类型直接转换为 LocalDateTime、LocalDate、LocalTime, 只能转换为Date
            // TODO: 如果Entity的属性有LocalDateTime、LocalDate、LocalTime类型, 需要单独实现转换方式
            if (descriptor == null) {
                instance.setExtMapItem(k.toLowerCase(), ConvertUtils.convert(v, String.class).toString().trim());
            } else {
                Class<?> propertyType = descriptor.getPropertyType();
                String packageName = propertyType.getPackage().getName();
                // 自定义 Convert 转换为 LocalDateTime、LocalDate、LocalTime
                // TODO: 底层是使用WeakFastHashMap<Class<?>, Converter> converters = new WeakFastHashMap() 存储Converter、
                if (TIME_PACKAGE.equals(packageName)) {
                    // BeanUtilsBean是以当前线程的classloader作为key获取的单例、这里每次都注册,避免多线程情况下自定义的Convert获取不到、
                    registerTimeConvert();
                }
                invokeWriteMethod(descriptor.getWriteMethod(), instance, ConvertUtils.convert(v, propertyType));
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

    /**
     * 可以利用单例对象(枚举)的ConcurrentMap属性, 缓存一下实体类所有字段对应的List<PropertyDescriptor>
     * BeanPropertyHelper.getBeanPropertyDescriptors(persistableClass);
     * List<PropertyDescriptor> propertyDescriptorList = BeanPropertyHelper.getBeanPropertyDescriptors(persistableClass);
     *
     * @param persistableClass 实体类Class、
     */
    private static List<PropertyDescriptor> getBeanPropertyDescriptors(Class<? extends BaseInfo> persistableClass) {
        // 先查询缓存ConcurrentMap、是否有persistableClass对应的List<PropertyDescriptor>存在、
        Field[] declaredFields = persistableClass.getDeclaredFields();
        return Stream.of(declaredFields).map(field -> {
            try {
                return new PropertyDescriptor(field.getName(), persistableClass);
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    /**
     * 由于ConvertUtils不支持将数据库时间类型转换为java.time下的LocalTime、LocalDate、LocalDateTime
     * 自定义Convert、
     */
    private static void registerTimeConvert() {
        registerLocalDateTimeConvert();
        registerLocalDateConvert();
        registerLocalTimeConvert();
    }

    private static void registerLocalDateTimeConvert() {
        ConvertUtils.register(LocalDateTimeConvert.of(), LocalDateTime.class);
    }

    private static void registerLocalDateConvert() {
        ConvertUtils.register(LocalDateTimeConvert.of(), LocalDate.class);
    }

    private static void registerLocalTimeConvert() {
        ConvertUtils.register(LocalDateTimeConvert.of(), LocalTime.class);
    }

    /**
     * 内部类、实现{@link Converter}
     * 可以自定义ConvertUtils, 实现{@link Converter}并继承{@link ConvertUtils}
     */
    public static class LocalDateTimeConvert implements Converter {
        static LocalDateTimeConvert of() {
            return new LocalDateTimeConvert();
        }

        @Override
        public <T> T convert(Class<T> aClass, Object o) {
            Class<?> oClass = o.getClass();
            if (java.util.Date.class.isAssignableFrom(oClass)) {
                if (Timestamp.class == oClass) {
                    Timestamp timestamp = (Timestamp) o;
                    String simpleName = aClass.getSimpleName();
                    switch (simpleName) {
                        case "LocalDateTime":
                            @SuppressWarnings("unchecked")
                            T localDateTime = (T) timestamp.toLocalDateTime();
                            return localDateTime;
                        case "LocalDate":
                            @SuppressWarnings("unchecked")
                            T localDate = (T) timestamp.toLocalDateTime().toLocalDate();
                            return localDate;
                        case "LocalTime":
                            @SuppressWarnings("unchecked")
                            T localTime = (T) timestamp.toLocalDateTime().toLocalTime();
                            return localTime;
                        default:
                    }
                } else if (Date.class == oClass) {
                    Date date = (Date) o;
                    String simpleName = aClass.getSimpleName();
                    switch (simpleName) {
                        case "LocalDateTime":
                            @SuppressWarnings("unchecked")
                            T localDateTime = (T) date.toLocalDate().atStartOfDay();
                            return localDateTime;
                        case "LocalDate":
                            @SuppressWarnings("unchecked")
                            T localDate = (T) date.toLocalDate();
                            return localDate;
                        default:
                    }
                } else if (Time.class == oClass) {
                    Time time = (Time) o;
                    String simpleName = aClass.getSimpleName();
                    if ("LocalTime".equals(simpleName)) {
                        @SuppressWarnings("unchecked")
                        T localDate = (T) time.toLocalTime();
                        return localDate;
                    }
                }
            }
            return null;
        }
    }
}
