package com.luxf.mybatis.bootmybatisdemo.mapper;

import com.luxf.mybatis.bootmybatisdemo.entity.ConcurrentInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author luxf
 * @date 2020/2/24 22:53
 */
@Repository
@Mapper
public interface ConcurrentMapper {
    ConcurrentInfo findByCond(Integer value);

    void updateByCond(@Param("stock") Integer stock, @Param("id") String id);
}
