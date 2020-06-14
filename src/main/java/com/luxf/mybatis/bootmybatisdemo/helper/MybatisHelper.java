package com.luxf.mybatis.bootmybatisdemo.helper;

import org.apache.ibatis.session.SqlSessionFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具类最好不要写成@Component、使用还需要注入、
 *
 * @author 小66  2020-02-23 19:26
 **/
public class MybatisHelper {
    private final static SqlSessionFactory SESSION_FACTORY = ApplicationContextHelper.getBean(SqlSessionFactory.class);

    /**
     * 查询表的结构：字段名和类,如果想存储其他字段,可以创建一个类(ColumnContext)来存储、不使用Map、
     *
     * @param selectEmptySql 类似'SELECT * FROM  USER LIMIT 0'的语句、
     * @return
     */
    public static List<Map<String, Object>> selectColumnContextList(String selectEmptySql) {
        //try-with-resource语句、不需要手动关闭连接、
        try (Connection connection = SESSION_FACTORY.getConfiguration()
                .getEnvironment().getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(selectEmptySql);
             ResultSet resultSet = statement.executeQuery()) {
            //注意：metaData的所有需要int参数的方法中 index 从 1 开始、
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<Map<String, Object>> columnMapList = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                Map<String, Object> map = new HashMap<>(2);
                String columnName = metaData.getColumnName(i);
                //数据库的数据类型
                String columnDataBaseTypeName = metaData.getColumnTypeName(i);
                //针对decimal类型
                int scale = metaData.getScale(i);
                Class<?> javaType = getJavaType(columnName, columnDataBaseTypeName, scale);
                map.put(columnName, javaType);
                columnMapList.add(map);
            }
            return columnMapList;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static Class<?> getJavaType(String columnName, String dataType, int scale) {
        switch (dataType.toLowerCase()) {
            case "varchar":
            case "text": // clob
            case "mediumtext": // clob
            case "longtext": // clob
            case "blob": // blob
            case "longblob": // blob
            case "char":
                return String.class;
            case "int":
            case "tinyint":
            case "smallint":
            case "bigint":
                return Long.class;
            case "float":
            case "double":
                return Double.class;
            case "decimal":
                if (scale > 0) {
                    return Double.class;
                } else {
                    return Long.class;
                }
            case "date":
                return LocalDate.class;
            case "datetime":
            case "timestamp":
                return LocalDateTime.class;
            default:
                throw new RuntimeException(columnName + "@" + dataType + "@" + scale);
        }
    }
}