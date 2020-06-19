package com.luxf.mybatis.bootmybatisdemo.mapper;

import com.luxf.mybatis.bootmybatisdemo.entity.BaseInfo;
import com.luxf.mybatis.bootmybatisdemo.entity.EntityContextInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * BaseMapper,定义基础的单表增删改查接口、将返回值Map转换为Info、
 *
 * @author 小66
 * @date 2020-06-18 20:26
 **/
@Repository
@Mapper
public interface AbstractMapper<T extends BaseInfo<I>, I extends Serializable> {
    Map<String, Object> findEntityByPrimaryKey(String tableName, I id);

    List<Map<String, Object>> findAll(@Param("tableName") String tableName);

    void insertEntity(@Param("contextInstance") EntityContextInstance contextInstance);

    List<Map<String, Object>> findEntityListByCond(@Param("contextInstance") EntityContextInstance contextInstance,
                                                   @Nullable @Param("resultColumnList") List<String> resultColumnList);
}
