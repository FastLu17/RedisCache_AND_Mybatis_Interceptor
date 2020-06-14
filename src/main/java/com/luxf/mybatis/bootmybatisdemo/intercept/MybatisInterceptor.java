package com.luxf.mybatis.bootmybatisdemo.intercept;

import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * springboot拦截mybatis：依赖 mybatis-spring-boot-starter的时候、不需要配置
 * mybatis.configuration.interceptions = com.xx.MybatisInterceptor、
 * <p>
 * 直接添加@Component注解即可,没有添加@Component注解时、拦截器不生效
 *
 * @author 小66  2020-02-23 14:39
 **/
//没有添加@Component注解时、拦截器不生效
@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class MybatisInterceptor implements Interceptor {
    /**
     * 主要拦截的逻辑、必须要执行invocation.proceed()、
     * <p>
     * 进入这个intercept()方法之前, 在{@link org.apache.ibatis.plugin.Plugin#invoke(Object proxy, Method method, Object[] args)}方法中判断的、
     * <p>
     * intercept()方法中的两个if判断的条件原因；
     * 1、{@link RoutingStatementHandler#delegate}属性：处理器委托对象,是{@link BaseStatementHandler}的子类
     * 2、{@link CachingExecutor#delegate}属性：执行器委托对象,是{@link org.apache.ibatis.executor.BaseExecutor}的子类,可以配置 mybatis.configuration.default-executor-type=simple 指定委托执行器、
     *
     * @param invocation 拦截器对象
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        if (target instanceof RoutingStatementHandler) {
            RoutingStatementHandler statementHandler = (RoutingStatementHandler) target;
            /*
              SimpleStatementHandler: 管理 Statement 对象并向数据库中推送不需要预编译的SQL语句、
              PreparedStatementHandler: 管理 Statement 对象并向数据中推送需要预编译的SQL语句、
              CallableStatementHandler：管理 Statement 对象并调用数据库中的存储过程、
            */
            //一般情况下是PreparedStatementHandler,可以在强制转换的之前判断一下、
            BaseStatementHandler baseHandler = (BaseStatementHandler) getFieldValue(statementHandler, "delegate");
            MappedStatement mappedStatement = (MappedStatement) getFieldValue(baseHandler, "mappedStatement");
            String statementId = mappedStatement.getId();
            BoundSql boundSql = baseHandler.getBoundSql();
            //ParameterMapping：xml文件中对应的#{id}、#{userName}的映射
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            //User selectUserByCondition(Map<String,Object> map); -->parameterObject 对应Mapper接口中的参数 Map
            Object parameterObject = boundSql.getParameterObject();

            //会添加到 BoundSql的private final Map<String, Object> additionalParameters集合中去、
//            boundSql.setAdditionalParameter("userName", "jack");
//            boundSql.setAdditionalParameter("realName", "JACK");
            String sql = boundSql.getSql();
            //为sql赋值的时候,DefaultParameterHandler类中的setParameters()方法、会处理additionalParameters和parameterObject中的值
                /*
                  String propertyName = parameterMapping.getProperty();
                  //首先寻找additionalParameters中的参数
                  if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                  } else if (parameterObject == null) {
                    value = null;
                  } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                  } else {
                    //最后寻找parameterObject中的参数
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                  }
                */
        } else if (Executor.class.isAssignableFrom(target.getClass())) {//粗粒度校验、
            //如果需要细粒度的校验,则需要根据配置文件来判断即可、
            /*
             * mybatis.configuration.default-executor-type=simple
             * mybatis.configuration.cache-enabled=false
             * 如果不禁用二级缓存,此处的Executor是CachingExecutor,
             * 如果禁用二级缓存,则此处的Executor是指定的SimpleExecutor、
             * */
            //CachingExecutor：如果mybatis开启了二级缓存、就会使用这个执行器、否则就会委托BaseExecutor的实现类来作为执行器、
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            String statementId = mappedStatement.getId();
            //拦截的查询语句、BaseExecutor类中的createCacheKey()会处理parameterMappings、如果对应的没有值,会抛出异常
            //ParameterMapping：xml文件中对应的#{id}、#{userName}的映射
            //List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

            if (invocation.getArgs().length > 1) {
                //此处的parameter对应BoundSql中的parameterObject属性、
                Object parameter = invocation.getArgs()[1];
                BoundSql boundSql = mappedStatement.getBoundSql(parameter);
                if (parameter != null && Map.class.isAssignableFrom(parameter.getClass())) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) parameter;
                    // map.put("realName", "JACK");
                    // map.put("userName", "jack");
                }
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        //此处不可返回null,会出现NullPointException！ 如果直接返回target,拦截器不生效、
        //return target;
        //target是 Executor、ParameterHandler、ResultSetHandler、StatementHandler 的实现类
        /*
          String name = target.getClass().getName();
          name = org.apache.ibatis.executor.CachingExecutor
          name = org.apache.ibatis.scripting.defaults.DefaultParameterHandler
          name = org.apache.ibatis.executor.resultset.DefaultResultSetHandler
          name = org.apache.ibatis.executor.statement.RoutingStatementHandler
        */
        //如果只想拦截Executor、则类型直接返回target即可、
        //if (!Executor.class.isAssignableFrom(target.getClass())) {
        //    return target;
        //}
        return Plugin.wrap(target, this);
    }

    /**
     * 目前没有发现properties的用处,不知道是不是配置mybatis的properties、
     *
     * @param properties properties
     */
    @Override
    public void setProperties(Properties properties) {
    }

    private static Field findField(Class<?> clazz, String name, Class<?> type) {
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            final Field field = Arrays.stream(searchType.getDeclaredFields())
                    .filter(f -> f.getName().equalsIgnoreCase(name) && (type == null || f.getType().equals(type)))
                    .findFirst().orElse(null);
            if (field != null) {
                return field;
            }
            searchType = searchType.getSuperclass();
        }
        throw new RuntimeException("Field not found :" + name);
    }

    private static Object getFieldValue(Object target, String propertyName) {
        Field property = findField(target.getClass(), propertyName, null);
        Object value;
        try {
            if (property.isAccessible()) {
                value = property.get(target);
            } else {
                property.setAccessible(true);
                value = property.get(target);
                property.setAccessible(false);
            }
            return value;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
