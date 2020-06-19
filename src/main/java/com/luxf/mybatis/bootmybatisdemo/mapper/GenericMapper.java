package com.luxf.mybatis.bootmybatisdemo.mapper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 通用Mapper、每个Mapper都应该继承于该接口、
 *
 * @author 小66
 * @date 2020-06-20 2:10
 **/
public interface GenericMapper<I extends Serializable> {
    List<Map<String, Object>> selectByCond();
}
