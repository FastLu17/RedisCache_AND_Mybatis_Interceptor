package com.luxf.mybatis.bootmybatisdemo.entity;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 存储实体类信息、
 *
 * @author 小66
 * @date 2020-06-19 18:08
 **/
public class EntityContextInstance {
    private String tableName;
    private List<ColumnContextInstance> fieldList;
    private List<ColumnContextInstance> condList;

    public static <T> EntityContextInstance of(T info) {
        EntityContextInstance instance = new EntityContextInstance();
        instance.tableName = getDataBaseTableName(info);
        // TODO: 如果父类有字段, 还需要获取父类的字段、
        Field[] fields = info.getClass().getDeclaredFields();
        instance.fieldList = Stream.of(fields).map(field -> ColumnContextInstance.of(field, info)).collect(Collectors.toList());
        return instance;
    }

    public static <T> EntityContextInstance of(Class<T> clazz, Map<String, Object> cond) {
        HashMap<String, Object> finalMap = new HashMap<>();
        cond.forEach((key, value) -> finalMap.put(key == null ? "" : key.toUpperCase(), value));
        EntityContextInstance instance = new EntityContextInstance();
        instance.tableName = getDataBaseTableName(clazz);
        // TODO: 如果父类有字段, 还需要获取父类的字段、
        Field[] fields = clazz.getDeclaredFields();
        // 获取字段条件、
        instance.condList = Stream.of(fields).filter(field -> finalMap.containsKey(field.getName().toUpperCase()))
                .map(field -> ColumnContextInstance.of(field, Collections.singletonList(finalMap.get(field.getName().toUpperCase()))))
                .collect(Collectors.toList());
        return instance;
    }


    public String getTableName() {
        return tableName;
    }

    public List<ColumnContextInstance> getFieldList() {
        return fieldList;
    }

    public List<ColumnContextInstance> getCondList() {
        return condList;
    }


    private static <T> String getDataBaseTableName(T info) {
        Class<?> infoClass = info.getClass();
        return getDataBaseTableName(infoClass);
    }

    private static <T> String getDataBaseTableName(Class<T> clazz) {
        boolean present = clazz.isAnnotationPresent(Table.class);
        if (present) {
            Table annotation = clazz.getAnnotation(Table.class);
            return annotation.name();
        }
        throw new RuntimeException("Annotation @Table 不存在");
    }

    private static <T> Object getFieldValue(Field field, T info) {
        try {
            boolean accessible = field.isAccessible();
            if (!accessible) {
                field.setAccessible(true);
            }
            Object fieldValue = field.get(info);
            field.setAccessible(accessible);
            return fieldValue;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 存储字段详细信息的类,数据库字段的映射关系
     */
    public static class ColumnContextInstance {
        private String columnName;
        private Object columnValue;
        private String columnJavaType;
        /**
         * 数据库的字段类型、可以先查出数据库的表结构, 对该字段进行初始化、 暂时就不做处理
         * 主要是处理：text,mediumtext,longtext,blob,longblob等、
         */
        private String columnJdbcType;

        static <T> ColumnContextInstance of(Field field, T info) {
            ColumnContextInstance columnInstance = new ColumnContextInstance();
            columnInstance.columnName = field.getName();
            columnInstance.columnJavaType = field.getType().getSimpleName();
            columnInstance.columnValue = getFieldValue(field, info);
            return columnInstance;
        }

        static ColumnContextInstance of(Field field, List<Object> fieldValueList) {
            ColumnContextInstance columnInstance = new ColumnContextInstance();
            columnInstance.columnName = field.getName();
            columnInstance.columnJavaType = field.getType().getSimpleName();
            columnInstance.columnValue = fieldValueList.get(0);
            return columnInstance;
        }

        public String getColumnName() {
            return columnName;
        }

        public Object getColumnValue() {
            return columnValue;
        }

        public String getColumnJavaType() {
            return columnJavaType;
        }

        public String getColumnJdbcType() {
            return columnJdbcType;
        }
    }
}
