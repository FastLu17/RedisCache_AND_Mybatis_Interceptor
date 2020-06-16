package com.luxf.mybatis.bootmybatisdemo.service;

import com.luxf.mybatis.bootmybatisdemo.entity.BaseInfo;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

/**
 * BaseDao层实现类、 Mapper 继承 BaseDaoImpl
 *
 * @author 小66
 * @date 2020-06-16 15:56
 **/
@Repository
public class AbstractService<T extends BaseInfo<I>, I extends Serializable> implements BaseService<T, I> {
    /**
     * BaseMapper、
     */
    //@Autowired
    //private BaseDaoMapper baseDaoMapper;

    @Cacheable(value = "Abstract")
    @Override
    public T findInfoById(I id) {
        // return baseDaoMapper.selectById(id);
        return null;
    }
}
