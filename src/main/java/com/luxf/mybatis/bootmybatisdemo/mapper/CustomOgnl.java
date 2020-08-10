package com.luxf.mybatis.bootmybatisdemo.mapper;

import org.apache.commons.lang3.ObjectUtils;

/**
 * @author 小66
 * @date 2020-08-10 21:38
 **/
public class CustomOgnl {

    /**
     * 必须要静态方法、否则报错：NoSuchMethodException.
     *
     * @param obj 被进行校验的参数、
     * @return Mapper.xml中if标签中的test属性值的校验结果、
     */
    public static boolean isNotEmpty(Object obj) {
        return ObjectUtils.isNotEmpty(obj);
    }
}
