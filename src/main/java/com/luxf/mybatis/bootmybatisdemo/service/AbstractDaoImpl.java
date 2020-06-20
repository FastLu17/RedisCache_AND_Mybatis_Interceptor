package com.luxf.mybatis.bootmybatisdemo.service;

import com.luxf.mybatis.bootmybatisdemo.entity.BaseInfo;
import com.luxf.mybatis.bootmybatisdemo.entity.EntityContextInstance;
import com.luxf.mybatis.bootmybatisdemo.helper.IdWorker;
import com.luxf.mybatis.bootmybatisdemo.helper.PersistenceHelper;
import com.luxf.mybatis.bootmybatisdemo.mapper.AbstractMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Table;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 模拟 通用Dao层、
 *
 * @author 小66
 * @date 2020-06-18 15:51
 **/
public abstract class AbstractDaoImpl<T extends BaseInfo<I>, I extends Serializable> implements BaseService<T, I> {
    @Autowired
    private AbstractMapper abstractMapper;

    @Cacheable("ABSTRACT_FIND_INFO")
    @Override
    public T findInfoById(I id) {
        Class<T> type = getType();
        if (type.isAnnotationPresent(Table.class)) {
            Table table = type.getAnnotation(Table.class);
            String tableName = table.name();
            @SuppressWarnings("unchecked")
            Map<String, Object> infoByPrimaryKey = abstractMapper.findEntityByPrimaryKey(tableName, id);
            return PersistenceHelper.mapToPersistable(infoByPrimaryKey, type);
        }
        throw new RuntimeException("数据异常！");
    }

    @Override
    public List<T> findAll() {
        Class<T> type = getType();
        if (type.isAnnotationPresent(Table.class)) {
            Table table = type.getAnnotation(Table.class);
            String tableName = table.name();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> mapList = abstractMapper.findAll(tableName);
            return PersistenceHelper.mapListToEntity(mapList, type);
        }
        throw new RuntimeException("数据异常！");
    }

    @Override
    public List<T> findEntityListByCond(Map<String, Object> cond, @Nullable String... resultColumnList) {
        Class<T> type = getType();
        EntityContextInstance instance = EntityContextInstance.of(type, cond);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mapList = abstractMapper.findEntityListByCond(instance,
                resultColumnList == null ? null : Arrays.asList(resultColumnList));
        return PersistenceHelper.mapListToEntity(mapList, type);
    }

    @CachePut(value = "ABSTRACT_INSERT_INFO")
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public T insertEntity(T info) {
        I id = info.getId();
        if (id == null) {
            info.setId(IdWorker.nextId(getIdType()));
        }
        EntityContextInstance contextInstance = EntityContextInstance.of(info);
        abstractMapper.insertEntity(contextInstance);
        return info;
    }
}
