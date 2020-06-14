package com.luxf.mybatis.bootmybatisdemo.service;

import com.luxf.mybatis.bootmybatisdemo.entity.BaseInfo;
import org.springframework.cache.annotation.Cacheable;

import java.io.Serializable;

/**
 * @author 小66
 * @date 2020-06-13 20:45
 **/
public interface BaseService<T extends BaseInfo<I>, I extends Serializable> {
    @Cacheable(value = "BASE_INFO")
    T findInfoById(I id);
}
